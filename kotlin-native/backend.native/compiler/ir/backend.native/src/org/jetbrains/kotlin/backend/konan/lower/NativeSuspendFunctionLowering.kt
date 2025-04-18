package org.jetbrains.kotlin.backend.konan.lower

import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.backend.common.collectTailSuspendCalls
import org.jetbrains.kotlin.backend.common.descriptors.synthesizedName
import org.jetbrains.kotlin.backend.common.lower.*
import org.jetbrains.kotlin.backend.konan.Context
import org.jetbrains.kotlin.backend.konan.NativeGenerationState
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.impl.IrVariableImpl
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrSuspendableExpressionImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrSuspensionPointImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.impl.IrVariableSymbolImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.isUnit
import org.jetbrains.kotlin.ir.types.makeNotNull
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.*
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.calls.checkers.isRestrictedSuspendFunction

internal class NativeSuspendFunctionsLowering(
        generationState: NativeGenerationState
) : AbstractSuspendFunctionsLowering<Context>(generationState.context), FileLoweringPass {
    private val symbols = context.symbols
    private val fileLowerState = generationState.fileLowerState
    private val saveCoroutineState = symbols.saveCoroutineState
    private val restoreCoroutineState = symbols.restoreCoroutineState

    override val stateMachineMethodName = Name.identifier("invokeSuspend")

    override fun lower(irFile: IrFile) {
        irFile.transformDeclarationsFlat(::tryTransformSuspendFunction)
        irFile.acceptVoid(object : IrVisitorVoid() {
            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }

            override fun visitClass(declaration: IrClass) {
                declaration.acceptChildrenVoid(this)
                if (declaration.origin != DECLARATION_ORIGIN_COROUTINE_IMPL)
                    declaration.transformDeclarationsFlat(::tryTransformSuspendFunction)
            }
        })
    }

    private fun tryTransformSuspendFunction(element: IrElement): List<IrDeclaration>? {
        val function = (element as? IrSimpleFunction) ?: return null
        if (!function.isSuspend || function.modality == Modality.ABSTRACT) return null

        val (tailSuspendCalls, hasNotTailSuspendCalls) = collectTailSuspendCalls(context, function)
        return if (hasNotTailSuspendCalls) {
            listOf<IrDeclaration>(buildCoroutine(function, isSuspendLambdaInvokeMethod = false), function)
        } else {
            // Otherwise, no suspend calls at all or all of them are tail calls - no need in a state machine.
            // Have to simplify them though (convert them to proper return statements).
            simplifyTailSuspendCalls(function, tailSuspendCalls)
            null
        }
    }

    private fun IrCall.isReturnIfSuspendedCall() =
            symbol == context.symbols.returnIfSuspended

    private fun simplifyTailSuspendCalls(irFunction: IrSimpleFunction, tailSuspendCalls: Set<IrCall>) {
        if (tailSuspendCalls.isEmpty()) return

        val irBuilder = context.createIrBuilder(irFunction.symbol)
        irFunction.body!!.transformChildrenVoid(object : IrElementTransformerVoid() {
            override fun visitCall(expression: IrCall): IrExpression {
                val shortCut = if (expression.isReturnIfSuspendedCall())
                    expression.arguments[0]!!
                else expression

                shortCut.transformChildrenVoid(this)

                return if (!expression.isSuspend || expression !in tailSuspendCalls)
                    shortCut
                else irBuilder.at(expression).irReturn(
                        irBuilder.generateDelegatedCall(irFunction.returnType, shortCut)
                )
            }
        })
    }

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    override fun getCoroutineBaseClass(function: IrFunction): IrClassSymbol =
            if (function.descriptor.isRestrictedSuspendFunction() || function.isRestrictedSuspensionInvokeMethod) {
                symbols.restrictedContinuationImpl
            } else {
                symbols.continuationImpl
            }

    override fun nameForCoroutineClass(function: IrFunction) =
            fileLowerState.getCoroutineImplUniqueName(function).synthesizedName

    override fun IrBlockBodyBuilder.generateCoroutineStart(invokeSuspendFunction: IrFunction, receiver: IrExpression) {
        +irReturn(
                irCall(invokeSuspendFunction).apply {
                    arguments[0] = receiver
                    arguments[1] = irSuccess(irGetObject(symbols.unit))
                }
        )
    }

    override fun buildStateMachine(
            stateMachineFunction: IrFunction,
            transformingFunction: IrFunction,
            argumentToPropertiesMap: Map<IrValueParameter, IrField>,
    ) {
        val originalBody = transformingFunction.body!!

        val (thisReceiver, resultArgument) = stateMachineFunction.parameters.also { check(it.size == 2) }

        val coroutineClass = stateMachineFunction.parentAsClass

        val labelField = coroutineClass.addField(Name.identifier("label"), symbols.nativePtrType, true)

        val startOffset = transformingFunction.startOffset
        val endOffset = transformingFunction.endOffset

        val irBuilder = context.createIrBuilder(stateMachineFunction.symbol, startOffset, endOffset)
        stateMachineFunction.body = irBuilder.irBlockBody(startOffset, endOffset) {
            val suspendResult = irVar("suspendResult".synthesizedName, context.irBuiltIns.anyNType, true)

            // Extract all suspend calls to temporaries in order to make correct jumps to them.
            originalBody.transformChildrenVoid(
                    ExpressionSlicer(labelField.type, transformingFunction, suspendResult, resultArgument, thisReceiver, labelField))

            originalBody.transformChildrenVoid(object : IrElementTransformerVoid() {
                // Replace returns to refer to the new function.
                override fun visitReturn(expression: IrReturn): IrExpression {
                    expression.transformChildrenVoid(this)

                    return if (expression.returnTargetSymbol != transformingFunction.symbol)
                        expression
                    else
                        irReturn(expression.value)
                }

                // Replace function arguments loading with properties reading.
                override fun visitGetValue(expression: IrGetValue): IrExpression {
                    expression.transformChildrenVoid(this)

                    val capturedValue = argumentToPropertiesMap[expression.symbol.owner]
                            ?: return expression
                    return irGetField(irGet(thisReceiver), capturedValue)
                }

                override fun visitSetValue(expression: IrSetValue): IrExpression {
                    expression.transformChildrenVoid(this)
                    val capturedValue = argumentToPropertiesMap[expression.symbol.owner]
                            ?: return expression
                    return irSetField(irGet(thisReceiver), capturedValue, expression.value)
                }
            })

            originalBody.setDeclarationsParent(stateMachineFunction)

            +suspendResult
            +IrSuspendableExpressionImpl(
                    startOffset, endOffset,
                    context.irBuiltIns.unitType,
                    suspensionPointId = irGetField(irGet(stateMachineFunction.dispatchReceiverParameter!!), labelField),
                    result = irBlock(startOffset, endOffset) {
                        +irThrowIfNotNull(irExceptionOrNull(irGet(resultArgument))) // Coroutine might start with an exception.
                        (originalBody as IrBlockBody).statements.forEach { +it }
                    })
            if (transformingFunction.returnType.isUnit())
                +irReturn(irGetObject(symbols.unit))                             // Insert explicit return for Unit functions.
        }
    }

    private var tempIndex = 0
    private var suspensionPointIdIndex = 0

    private inner class ExpressionSlicer(
            val suspensionPointIdType: IrType,
            val irFunction: IrFunction,
            val suspendResult: IrVariable,
            val resultArgument: IrValueParameter,
            val thisReceiver: IrValueParameter,
            val labelField: IrField,
    ) : IrElementTransformerVoid() {
        // TODO: optimize - it has square complexity.

        override fun visitSetField(expression: IrSetField): IrExpression {
            expression.transformChildrenVoid(this)

            return sliceExpression(expression)
        }

        override fun visitMemberAccess(expression: IrMemberAccessExpression<*>): IrExpression {
            expression.transformChildrenVoid(this)

            return sliceExpression(expression)
        }

        private fun sliceExpression(expression: IrExpression): IrExpression {
            val irBuilder = context.createIrBuilder(irFunction.symbol, expression.startOffset, expression.endOffset)
            irBuilder.run {
                val children = when (expression) {
                    is IrSetField -> listOf(expression.receiver, expression.value)
                    is IrMemberAccessExpression<*> -> expression.arguments
                    else -> throw Error("Unexpected expression: $expression")
                }

                val numberOfChildren = children.size

                val hasSuspendCallInTail = BooleanArray(numberOfChildren + 1)
                for (i in numberOfChildren - 1 downTo 0)
                    hasSuspendCallInTail[i] = hasSuspendCallInTail[i + 1] || children[i].let { it != null && it.hasSuspendCalls() }

                val newChildren = arrayOfNulls<IrExpression?>(numberOfChildren)
                val tempStatements = mutableListOf<IrStatement>()
                // No constructor argument is first since the call will be lowered to (val inst = alloc(); call(inst, *args); inst)
                // and therefore an actual first argument will be the just allocated instance.
                var first = expression !is IrConstructorCall
                for ((index, child) in children.withIndex()) {
                    if (child == null) continue
                    val transformedChild =
                            if (!child.isSpecialBlock())
                                child
                            else {
                                val statements = (child as IrBlock).statements
                                tempStatements += statements.take(statements.size - 1)
                                statements.last() as IrExpression
                            }
                    if (first && !hasSuspendCallInTail[index + 1]) {
                        // Don't extract suspend call to a temporary if it is the first argument and is the only suspend call.
                        newChildren[index] = transformedChild
                        first = false
                        continue
                    }
                    first = false
                    if (transformedChild.isPure() || !hasSuspendCallInTail[index])
                        newChildren[index] = transformedChild
                    else {
                        // Save to temporary in order to save execution order.
                        val tmp = irVar(transformedChild)

                        tempStatements += tmp
                        newChildren[index] = irGet(tmp)
                    }
                }

                val suspensionPointIdParameter by lazy {
                    irVar("suspensionPointId${suspensionPointIdIndex++}".synthesizedName, suspensionPointIdType)
                }

                fun saveState() = irBlock {
                    +irCall(saveCoroutineState)
                    +irSetField(
                            irGet(thisReceiver),
                            labelField,
                            irGet(suspensionPointIdParameter)
                    )
                }

                var calledSaveState = false
                var suspendCall: IrExpression? = null
                when {
                    expression.isReturnIfSuspendedCall -> {
                        calledSaveState = true
                        val firstArgument = newChildren[0]!!
                        newChildren[0] = irBlock(firstArgument) {
                            +saveState()
                            +firstArgument
                        }
                        suspendCall = newChildren[0]
                    }
                    expression.isSuspendCall -> {
                        val lastChildIndex = newChildren.indexOfLast { it != null }
                        if (lastChildIndex != -1) {
                            // Save state as late as possible.
                            val lastChild = newChildren[lastChildIndex]!!
                            calledSaveState = true
                            newChildren[lastChildIndex] =
                                    irBlock(lastChild) {
                                        if (lastChild.isPure()) {
                                            +saveState()
                                            +lastChild
                                        } else {
                                            val tmp = irVar(lastChild)
                                            +tmp
                                            +saveState()
                                            +irGet(tmp)
                                        }
                                    }
                        }
                        suspendCall = expression
                    }
                }

                when (expression) {
                    is IrSetField -> {
                        expression.receiver = newChildren[0]
                        expression.value = newChildren[1]!!
                    }
                    is IrMemberAccessExpression<*> -> {
                        newChildren.forEachIndexed { index, newChild ->
                            expression.arguments[index] = newChild
                        }
                    }
                }

                if (suspendCall == null)
                    return irWrap(expression, tempStatements)

                val suspensionPoint = IrSuspensionPointImpl(
                        startOffset                = startOffset,
                        endOffset                  = endOffset,
                        type                       = context.irBuiltIns.anyNType,
                        suspensionPointIdParameter = suspensionPointIdParameter,
                        result                     = irBlock(startOffset, endOffset) {
                            if (!calledSaveState)
                                +saveState()
                            +irSet(suspendResult.symbol, suspendCall)
                            +irReturnIfSuspended(suspendResult)
                            +irGet(suspendResult)
                        },
                        resumeResult               = irBlock(startOffset, endOffset) {
                            +irCall(restoreCoroutineState)
                            +irGetOrThrow(irGet(resultArgument))
                        })
                val expressionResult = when {
                    suspendCall.type.isUnit() -> irImplicitCoercionToUnit(suspensionPoint)
                    else -> irAs(suspensionPoint, suspendCall.type)
                }
                return irBlock(expression) {
                    tempStatements.forEach { +it }
                    +expressionResult
                }
            }

        }
    }

    private fun IrBuilderWithScope.irWrap(expression: IrExpression, tempStatements: List<IrStatement>)
            = if (tempStatements.isEmpty())
        expression
    else irBlock(expression, STATEMENT_ORIGIN_COROUTINE_IMPL) {
        tempStatements.forEach { +it }
        +expression
    }


    private fun IrBuilderWithScope.irThrowIfNotNull(exception: IrExpression) = irLetS(exception) {
        irThrowIfNotNull(it.owner)
    }

    fun IrBuilderWithScope.irThrowIfNotNull(exception: IrValueDeclaration) =
            irIfThen(irNot(irEqeqeq(irGet(exception), irNull())),
                    irThrow(irImplicitCast(irGet(exception), exception.type.makeNotNull())))

    private val IrExpression.isSuspendCall: Boolean
        get() = this is IrCall && this.isSuspend

    private fun IrElement.isSpecialBlock()
            = this is IrBlock && this.origin == STATEMENT_ORIGIN_COROUTINE_IMPL

    private fun IrElement.hasSuspendCalls(): Boolean {
        var hasSuspendCalls = false
        acceptVoid(object : IrVisitorVoid() {
            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }

            override fun visitCall(expression: IrCall) {
                expression.acceptChildrenVoid(this)
                hasSuspendCalls = hasSuspendCalls || expression.isSuspendCall
            }

            override fun visitExpression(expression: IrExpression) {
                expression.acceptChildrenVoid(this)
                hasSuspendCalls = hasSuspendCalls || expression is IrSuspensionPointImpl
            }
        })

        return hasSuspendCalls
    }

    private val IrExpression.isReturnIfSuspendedCall: Boolean
        get() = this is IrCall && isReturnIfSuspendedCall()

    private fun IrBuilderWithScope.irVar(initializer: IrExpression) =
            irVar("tmp${tempIndex++}".synthesizedName, initializer.type, false, initializer)


    private fun IrBuilderWithScope.irReturnIfSuspended(value: IrValueDeclaration) =
            irIfThen(irEqeqeq(irGet(value), irCall(symbols.coroutineSuspendedGetter)),
                    irReturn(irGet(value)))

    private fun IrExpression.isPure(): Boolean {
        return when (this) {
            is IrConst -> true
            is IrCall -> false // TODO: skip builtin operators.
            is IrTypeOperatorCall -> this.argument.isPure() && this.operator != IrTypeOperator.CAST
            is IrGetValue -> !this.symbol.owner.let { it is IrVariable && it.isVar }
            else -> false
        }
    }

    private fun IrBuilderWithScope.irVar(name: Name, type: IrType,
                                         isMutable: Boolean = false,
                                         initializer: IrExpression? = null) =
        IrVariableImpl(
                startOffset, endOffset,
                DECLARATION_ORIGIN_COROUTINE_IMPL,
                IrVariableSymbolImpl(),
                name,
                type,
                isMutable,
                isConst = false,
                isLateinit = false
        ).apply {
            this.initializer = initializer
            this.parent = this@irVar.parent
        }

    private fun IrBuilderWithScope.irGetOrThrow(result: IrExpression): IrExpression =
            irCall(symbols.kotlinResultGetOrThrow.owner).apply {
                arguments[0] = result
            } // TODO: consider inlining getOrThrow function body here.

    private fun IrBuilderWithScope.irExceptionOrNull(result: IrExpression): IrExpression {
        val resultClass = symbols.kotlinResult.owner
        val exceptionOrNull = resultClass.simpleFunctions().single { it.name.asString() == "exceptionOrNull" }
        return irCall(exceptionOrNull).apply {
            dispatchReceiver = result
        }
    }

    fun IrBlockBodyBuilder.irSuccess(value: IrExpression): IrMemberAccessExpression<*> {
        val createResult = symbols.kotlinResult.owner.constructors.single { it.isPrimary }
        return irCall(createResult).apply {
            arguments[0] = value
        }
    }
}
