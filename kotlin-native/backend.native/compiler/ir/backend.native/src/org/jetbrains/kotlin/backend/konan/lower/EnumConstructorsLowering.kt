/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.kotlin.backend.konan.lower

import org.jetbrains.kotlin.backend.common.ClassLoweringPass
import org.jetbrains.kotlin.backend.common.runOnFilePostfix
import org.jetbrains.kotlin.backend.konan.Context
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.symbols.impl.IrConstructorSymbolImpl
import org.jetbrains.kotlin.ir.symbols.impl.IrValueParameterSymbolImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.Name

internal class EnumConstructorsLowering(val context: Context) : ClassLoweringPass {

    fun run(irFile: IrFile) {
        runOnFilePostfix(irFile)
    }

    override fun lower(irClass: IrClass) {
        if (irClass.kind != ClassKind.ENUM_CLASS) return
        EnumClassTransformer(irClass).run()
    }

    private interface EnumConstructorCallTransformer {
        fun transform(enumConstructorCall: IrEnumConstructorCall): IrExpression
        fun transform(delegatingConstructorCall: IrDelegatingConstructorCall): IrExpression
    }

    private inner class EnumClassTransformer(val irClass: IrClass) {
        private val loweredEnumConstructors = mutableMapOf<IrConstructor, IrConstructor>()
        private val loweredEnumConstructorParameters = mutableMapOf<IrValueParameter, IrValueParameter>()

        fun run() {
            insertInstanceInitializerCall()
            lowerEnumConstructors(irClass)
            lowerEnumEntriesClasses()
            lowerEnumClassBody()
        }

        private fun insertInstanceInitializerCall() {
            irClass.transformChildrenVoid(object: IrElementTransformerVoid() {
                override fun visitClass(declaration: IrClass): IrStatement {
                    // Skip nested
                    return declaration
                }

                override fun visitConstructor(declaration: IrConstructor): IrStatement {
                    declaration.transformChildrenVoid(this)

                    val blockBody = declaration.body as? IrBlockBody
                            ?: throw AssertionError("Unexpected constructor body: ${declaration.body}")
                    if (blockBody.statements.all { it !is IrInstanceInitializerCall }) {
                        blockBody.statements.transformFlat {
                            if (it is IrEnumConstructorCall)
                                listOf(it, IrInstanceInitializerCallImpl(declaration.startOffset, declaration.startOffset,
                                        irClass.symbol, context.irBuiltIns.unitType))
                            else null
                        }
                    }
                    return declaration
                }
            })
        }

        private fun lowerEnumEntriesClasses() {
            for (enumEntry in irClass.declarations.filterIsInstance<IrEnumEntry>())
                enumEntry.correspondingClass?.let { lowerEnumConstructors(it) }
        }

        private fun lowerEnumConstructors(irClass: IrClass) {
            irClass.declarations.forEachIndexed { index, declaration ->
                if (declaration is IrConstructor)
                    irClass.declarations[index] = transformEnumConstructor(declaration)
            }
        }

        private fun transformEnumConstructor(enumConstructor: IrConstructor): IrConstructor {
            val loweredEnumConstructor = lowerEnumConstructor(enumConstructor)

            for (parameter in enumConstructor.parameters) {
                val defaultValue = parameter.defaultValue ?: continue
                defaultValue.transformChildrenVoid(ParameterMapper(enumConstructor, loweredEnumConstructor, true))
                loweredEnumConstructor.parameters[parameter.loweredIndex].defaultValue = defaultValue
                defaultValue.setDeclarationsParent(loweredEnumConstructor)
            }

            return loweredEnumConstructor
        }

        private fun lowerEnumConstructor(constructor: IrConstructor): IrConstructor {
            val startOffset = constructor.startOffset
            val endOffset = constructor.endOffset
            val loweredConstructor =
                    context.irFactory.createConstructor(
                            startOffset,
                            endOffset,
                            constructor.origin,
                            constructor.name,
                            DescriptorVisibilities.PROTECTED,
                            isInline = false,
                            isExpect = false,
                            constructor.returnType,
                            IrConstructorSymbolImpl(),
                            isPrimary = constructor.isPrimary,
                    ).apply {
                        parent = constructor.parent
                        val body = constructor.body!!
                        this.body = body // Will be transformed later.
                        body.setDeclarationsParent(this)
                    }

            fun createSynthesizedValueParameter(name: String, type: IrType): IrValueParameter =
                    context.irFactory.createValueParameter(
                            startOffset = startOffset,
                            endOffset = endOffset,
                            origin = DECLARATION_ORIGIN_ENUM,
                            kind = IrParameterKind.Regular,
                            name = Name.identifier(name),
                            type = type,
                            isAssignable = false,
                            symbol = IrValueParameterSymbolImpl(),
                            varargElementType = null,
                            isCrossinline = false,
                            isNoinline = false,
                            isHidden = false,
                    ).apply {
                        parent = loweredConstructor
                    }

            loweredConstructor.parameters += createSynthesizedValueParameter("name", context.irBuiltIns.stringType)
            loweredConstructor.parameters += createSynthesizedValueParameter("ordinal", context.irBuiltIns.intType)
            loweredConstructor.parameters += constructor.parameters.map {
                it.copyTo(loweredConstructor).apply {
                    loweredEnumConstructorParameters[it] = this
                }
            }

            loweredEnumConstructors[constructor] = loweredConstructor

            return loweredConstructor
        }

        private fun lowerEnumClassBody() {
            val transformer = EnumClassBodyTransformer()
            irClass.transformChildrenVoid(transformer)
            irClass.declarations.filterIsInstance<IrEnumEntry>().forEach {
                it.correspondingClass?.transformChildrenVoid(transformer)
            }
        }

        private inner class InEnumClassConstructor(val enumClassConstructor: IrConstructor) :
                EnumConstructorCallTransformer {
            override fun transform(enumConstructorCall: IrEnumConstructorCall): IrExpression {
                val startOffset = enumConstructorCall.startOffset
                val endOffset = enumConstructorCall.endOffset
                val origin = enumConstructorCall.origin

                val result = IrDelegatingConstructorCallImpl.fromSymbolOwner(
                        startOffset, endOffset,
                        context.irBuiltIns.unitType,
                        enumConstructorCall.symbol
                )
                assert(result.symbol.owner.parameters.size == 2) {
                    "Enum(String, Int) constructor call expected:\n${result.dump()}"
                }

                val nameParameter = enumClassConstructor.parameters.getOrElse(0) {
                    throw AssertionError("No 'name' parameter in enum constructor: $enumClassConstructor")
                }

                val ordinalParameter = enumClassConstructor.parameters.getOrElse(1) {
                    throw AssertionError("No 'ordinal' parameter in enum constructor: $enumClassConstructor")
                }

                result.arguments[0] = IrGetValueImpl(startOffset, endOffset, nameParameter.type, nameParameter.symbol, origin)
                result.arguments[1] = IrGetValueImpl(startOffset, endOffset, ordinalParameter.type, ordinalParameter.symbol, origin)
                return result
            }

            override fun transform(delegatingConstructorCall: IrDelegatingConstructorCall): IrExpression {
                val startOffset = delegatingConstructorCall.startOffset
                val endOffset = delegatingConstructorCall.endOffset

                val delegatingConstructor = delegatingConstructorCall.symbol.owner
                val loweredDelegatingConstructor = loweredEnumConstructors.getOrElse(delegatingConstructor) {
                    throw AssertionError("Constructor called in enum entry initializer should've been lowered: $delegatingConstructor")
                }

                val result = IrDelegatingConstructorCallImpl.fromSymbolOwner(
                        startOffset, endOffset,
                        context.irBuiltIns.unitType,
                        loweredDelegatingConstructor.symbol
                )
                val firstParameter = enumClassConstructor.parameters[0]
                result.arguments[0] = IrGetValueImpl(startOffset, endOffset, firstParameter.type, firstParameter.symbol)
                val secondParameter = enumClassConstructor.parameters[1]
                result.arguments[1] = IrGetValueImpl(startOffset, endOffset, secondParameter.type, secondParameter.symbol)

                delegatingConstructor.parameters.forEach {
                    result.arguments[it.loweredIndex] = delegatingConstructorCall.arguments[it.indexInParameters]
                }

                return result
            }
        }

        private abstract inner class InEnumEntry(private val enumEntry: IrEnumEntry) : EnumConstructorCallTransformer {

            override fun transform(enumConstructorCall: IrEnumConstructorCall): IrExpression {
                val name = enumEntry.name.asString()
                val ordinal = context.enumsSupport.enumEntriesMap(enumEntry.parentAsClass)[enumEntry.name]!!.ordinal

                val startOffset = enumConstructorCall.startOffset
                val endOffset = enumConstructorCall.endOffset

                val enumConstructor = enumConstructorCall.symbol.owner
                val loweredConstructor = loweredEnumConstructors.getOrElse(enumConstructor) {
                    throw AssertionError("Constructor called in enum entry initializer should've been lowered: $enumConstructor")
                }

                val result = createConstructorCall(startOffset, endOffset, loweredConstructor.symbol)

                result.arguments[0] = IrConstImpl.string(startOffset, endOffset, context.irBuiltIns.stringType, name)
                result.arguments[1] = IrConstImpl.int(startOffset, endOffset, context.irBuiltIns.intType, ordinal)

                enumConstructor.parameters.forEach {
                    result.arguments[it.loweredIndex] = enumConstructorCall.arguments[it.indexInParameters]
                }

                return result
            }

            override fun transform(delegatingConstructorCall: IrDelegatingConstructorCall): IrExpression {
                throw AssertionError("Unexpected delegating constructor call within enum entry: $enumEntry")
            }

            abstract fun createConstructorCall(startOffset: Int, endOffset: Int, loweredConstructor: IrConstructorSymbol): IrMemberAccessExpression<*>
        }

        private inner class InEnumEntryClassConstructor(enumEntry: IrEnumEntry) : InEnumEntry(enumEntry) {
            override fun createConstructorCall(startOffset: Int, endOffset: Int, loweredConstructor: IrConstructorSymbol) =
                    IrDelegatingConstructorCallImpl(startOffset, endOffset, context.irBuiltIns.unitType, loweredConstructor,
                    loweredConstructor.owner.typeParameters.size)
        }

        private inner class InEnumEntryInitializer(enumEntry: IrEnumEntry) : InEnumEntry(enumEntry) {
            override fun createConstructorCall(startOffset: Int, endOffset: Int, loweredConstructor: IrConstructorSymbol) =
                    IrConstructorCallImpl.fromSymbolOwner(startOffset, endOffset, loweredConstructor.owner.returnType, loweredConstructor)
        }

        private inner class EnumClassBodyTransformer : IrElementTransformerVoid() {
            private var enumConstructorCallTransformer: EnumConstructorCallTransformer? = null

            override fun visitClass(declaration: IrClass): IrStatement {
                if (declaration.kind == ClassKind.ENUM_CLASS)
                    return declaration
                return super.visitClass(declaration)
            }

            override fun visitEnumEntry(declaration: IrEnumEntry): IrStatement {
                assert(enumConstructorCallTransformer == null) { "Nested enum entry initialization:\n${declaration.dump()}" }

                enumConstructorCallTransformer = InEnumEntryInitializer(declaration)

                declaration.initializerExpression = declaration.initializerExpression?.transform(this, data = null)

                enumConstructorCallTransformer = null

                return declaration
            }

            override fun visitConstructor(declaration: IrConstructor): IrStatement {
                val containingClass = declaration.parentAsClass

                // TODO local (non-enum) class in enum class constructor?
                val previous = enumConstructorCallTransformer

                if (containingClass.kind == ClassKind.ENUM_ENTRY) {
                    assert(enumConstructorCallTransformer == null) { "Nested enum entry initialization:\n${declaration.dump()}" }
                    val entry = irClass.declarations.filterIsInstance<IrEnumEntry>().single { it.correspondingClass == containingClass }
                    enumConstructorCallTransformer = InEnumEntryClassConstructor(entry)
                } else if (containingClass.kind == ClassKind.ENUM_CLASS) {
                    assert(enumConstructorCallTransformer == null) { "Nested enum entry initialization:\n${declaration.dump()}" }
                    enumConstructorCallTransformer = InEnumClassConstructor(declaration)
                }

                val result = super.visitConstructor(declaration)

                enumConstructorCallTransformer = previous

                return result
            }

            override fun visitEnumConstructorCall(expression: IrEnumConstructorCall): IrExpression {
                expression.transformChildrenVoid(this)

                val callTransformer = enumConstructorCallTransformer ?:
                throw AssertionError("Enum constructor call outside of enum entry initialization or enum class constructor:\n" + irClass.dump())


                return callTransformer.transform(expression)
            }

            override fun visitDelegatingConstructorCall(expression: IrDelegatingConstructorCall): IrExpression {
                expression.transformChildrenVoid(this)

                if (expression.symbol.owner.parentAsClass.kind == ClassKind.ENUM_CLASS) {
                    val callTransformer = enumConstructorCallTransformer ?:
                    throw AssertionError("Enum constructor call outside of enum entry initialization or enum class constructor:\n" + irClass.dump())

                    return callTransformer.transform(expression)
                }
                return expression
            }

            override fun visitGetValue(expression: IrGetValue): IrExpression {
                val parameter = expression.symbol.owner
                val loweredParameter = loweredEnumConstructorParameters[parameter]
                return if (loweredParameter == null) {
                    expression
                } else {
                    IrGetValueImpl(expression.startOffset, expression.endOffset, loweredParameter.type,
                            loweredParameter.symbol, expression.origin)
                }
            }

            override fun visitSetValue(expression: IrSetValue): IrExpression {
                expression.transformChildrenVoid()
                return loweredEnumConstructorParameters[expression.symbol.owner]?.let {
                    IrSetValueImpl(expression.startOffset, expression.endOffset, it.type,
                            it.symbol, expression.value, expression.origin)
                } ?: expression
            }
        }
    }
}

private val IrValueParameter.loweredIndex: Int get() = indexInParameters + 2

private class ParameterMapper(superConstructor: IrConstructor,
                              val constructor: IrConstructor,
                              val useLoweredIndex: Boolean) : IrElementTransformerVoid() {
    private val valueParameters = superConstructor.parameters.toSet()

    override fun visitGetValue(expression: IrGetValue): IrExpression {

        val superParameter = expression.symbol.owner as? IrValueParameter ?: return expression
        if (valueParameters.contains(superParameter)) {
            val index = if (useLoweredIndex) superParameter.loweredIndex else superParameter.indexInParameters
            val parameter = constructor.parameters[index]
            return IrGetValueImpl(
                    expression.startOffset, expression.endOffset,
                    parameter.type,
                    parameter.symbol)
        }
        return expression
    }

    override fun visitSetValue(expression: IrSetValue): IrExpression {
        expression.transformChildrenVoid()
        val superParameter = expression.symbol.owner as? IrValueParameter ?: return expression
        if (valueParameters.contains(superParameter)) {
            val index = if (useLoweredIndex) superParameter.loweredIndex else superParameter.indexInParameters
            val parameter = constructor.parameters[index]
            return IrSetValueImpl(
                    expression.startOffset, expression.endOffset,
                    parameter.type,
                    parameter.symbol,
                    expression.value,
                    expression.origin)
        }
        return expression
    }
}
