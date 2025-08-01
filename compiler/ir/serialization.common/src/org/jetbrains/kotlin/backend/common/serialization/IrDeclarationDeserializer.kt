/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common.serialization

import org.jetbrains.kotlin.backend.common.linkage.issues.IrSymbolTypeMismatchException
import org.jetbrains.kotlin.backend.common.serialization.IrDeserializationSettings.DeserializeFunctionBodies
import org.jetbrains.kotlin.backend.common.serialization.encodings.*
import org.jetbrains.kotlin.backend.common.serialization.encodings.BinarySymbolData.SymbolKind
import org.jetbrains.kotlin.backend.common.serialization.encodings.BinarySymbolData.SymbolKind.*
import org.jetbrains.kotlin.backend.common.serialization.proto.IrDeclaration.DeclaratorCase.*
import org.jetbrains.kotlin.backend.common.serialization.proto.IrType.KindCase.*
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.InlineClassRepresentation
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.MultiFieldValueClassRepresentation
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.impl.IrVariableImpl
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrCompositeImpl
import org.jetbrains.kotlin.ir.symbols.*
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.types.impl.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.Variance
import org.jetbrains.kotlin.utils.*
import org.jetbrains.kotlin.utils.memoryOptimizedMap
import kotlin.reflect.full.declaredMemberProperties
import org.jetbrains.kotlin.backend.common.serialization.proto.IrAnonymousInit as ProtoAnonymousInit
import org.jetbrains.kotlin.backend.common.serialization.proto.IrClass as ProtoClass
import org.jetbrains.kotlin.backend.common.serialization.proto.IrConstructor as ProtoConstructor
import org.jetbrains.kotlin.backend.common.serialization.proto.IrConstructorCall as ProtoConstructorCall
import org.jetbrains.kotlin.backend.common.serialization.proto.IrDeclaration as ProtoDeclaration
import org.jetbrains.kotlin.backend.common.serialization.proto.IrDeclarationBase as ProtoDeclarationBase
import org.jetbrains.kotlin.backend.common.serialization.proto.IrDefinitelyNotNullType as ProtoDefinitelyNotNullType
import org.jetbrains.kotlin.backend.common.serialization.proto.IrDynamicType as ProtoDynamicType
import org.jetbrains.kotlin.backend.common.serialization.proto.IrEnumEntry as ProtoEnumEntry
import org.jetbrains.kotlin.backend.common.serialization.proto.IrExpression as ProtoExpression
import org.jetbrains.kotlin.backend.common.serialization.proto.IrField as ProtoField
import org.jetbrains.kotlin.backend.common.serialization.proto.IrFunction as ProtoFunction
import org.jetbrains.kotlin.backend.common.serialization.proto.IrFunctionBase as ProtoFunctionBase
import org.jetbrains.kotlin.backend.common.serialization.proto.IrInlineClassRepresentation as ProtoIrInlineClassRepresentation
import org.jetbrains.kotlin.backend.common.serialization.proto.IrLocalDelegatedProperty as ProtoLocalDelegatedProperty
import org.jetbrains.kotlin.backend.common.serialization.proto.IrMultiFieldValueClassRepresentation as ProtoIrMultiFieldValueClassRepresentation
import org.jetbrains.kotlin.backend.common.serialization.proto.IrProperty as ProtoProperty
import org.jetbrains.kotlin.backend.common.serialization.proto.IrSimpleType as ProtoSimpleType
import org.jetbrains.kotlin.backend.common.serialization.proto.IrSimpleTypeLegacy as ProtoSimpleTypeLegacy
import org.jetbrains.kotlin.backend.common.serialization.proto.IrSimpleTypeNullability as ProtoSimpleTypeNullablity
import org.jetbrains.kotlin.backend.common.serialization.proto.IrStatement as ProtoStatement
import org.jetbrains.kotlin.backend.common.serialization.proto.IrType as ProtoType
import org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeAlias as ProtoTypeAlias
import org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeParameter as ProtoTypeParameter
import org.jetbrains.kotlin.backend.common.serialization.proto.IrValueParameter as ProtoValueParameter
import org.jetbrains.kotlin.backend.common.serialization.proto.IrVariable as ProtoVariable

class IrDeclarationDeserializer(
    builtIns: IrBuiltIns,
    private val symbolTable: SymbolTable,
    val irFactory: IrFactory,
    private val libraryFile: IrLibraryFile,
    parent: IrDeclarationParent,
    private val settings: IrDeserializationSettings,
    val symbolDeserializer: IrSymbolDeserializer,
    private val onDeserializedClass: (IrClass, IdSignature) -> Unit,
    private val needToDeserializeFakeOverrides: (IrClass) -> Boolean,
    private val specialProcessingForMismatchedSymbolKind: ((deserializedSymbol: IrSymbol, fallbackSymbolKind: SymbolKind?) -> IrSymbol)?,
    private val irInterner: IrInterningService,
) {
    private var areFunctionBodiesDeserialized: Boolean =
        settings.deserializeFunctionBodies == DeserializeFunctionBodies.ALL

    private val bodyDeserializer = IrBodyDeserializer(builtIns, irFactory, libraryFile, this, settings)

    private fun deserializeName(index: Int): Name = irInterner.name(Name.guessByFirstCharacter(libraryFile.string(index)))

    private val irTypeCache = hashMapOf<Int, IrType>()

    fun deserializeNullableIrType(index: Int): IrType? = if (index == -1) null else deserializeIrType(index)

    fun deserializeIrType(index: Int): IrType {
        return irTypeCache.getOrPut(index) {
            val typeData = libraryFile.type(index)
            deserializeIrTypeData(typeData)
        }
    }

    private fun deserializeIrTypeArgument(proto: Long): IrTypeArgument {
        val encoding = BinaryTypeProjection.decode(proto)

        if (encoding.isStarProjection) return IrStarProjectionImpl

        return makeTypeProjection(deserializeIrType(encoding.typeIndex), encoding.variance)
    }

    // Deserializes all annotations, even having SOURCE retention, since they might be needed in backends, like @Volatile
    internal fun deserializeAnnotations(annotations: List<ProtoConstructorCall>): List<IrConstructorCall> {
        return annotations.memoryOptimizedMap { bodyDeserializer.deserializeAnnotation(it) }
    }

    private fun deserializeSimpleTypeNullability(proto: ProtoSimpleTypeNullablity) = when (proto) {
        ProtoSimpleTypeNullablity.MARKED_NULLABLE -> SimpleTypeNullability.MARKED_NULLABLE
        ProtoSimpleTypeNullablity.NOT_SPECIFIED -> SimpleTypeNullability.NOT_SPECIFIED
        ProtoSimpleTypeNullablity.DEFINITELY_NOT_NULL -> SimpleTypeNullability.DEFINITELY_NOT_NULL
    }

    private fun deserializeSimpleType(proto: ProtoSimpleType): IrSimpleType {
        val symbol = deserializeIrSymbol(proto.classifier)
            .checkSymbolType<IrClassifierSymbol>(fallbackSymbolKind = /* just the first possible option */ CLASS_SYMBOL)

        val arguments = proto.argumentList.memoryOptimizedMap { deserializeIrTypeArgument(it) }
        val annotations = deserializeAnnotations(proto.annotationList)

        return IrSimpleTypeImpl(
            symbol,
            deserializeSimpleTypeNullability(proto.nullability),
            arguments,
            annotations
        )
    }

    private fun deserializeLegacySimpleType(proto: ProtoSimpleTypeLegacy): IrSimpleType {
        val symbol = deserializeIrSymbol(proto.classifier)
            .checkSymbolType<IrClassifierSymbol>(fallbackSymbolKind = /* just the first possible option */ CLASS_SYMBOL)

        val arguments = proto.argumentList.memoryOptimizedMap { deserializeIrTypeArgument(it) }
        val annotations = deserializeAnnotations(proto.annotationList)

        return IrSimpleTypeImpl(
            symbol,
            SimpleTypeNullability.fromHasQuestionMark(proto.hasQuestionMark),
            arguments,
            annotations
        )
    }

    private val SIMPLE_DYNAMIC_TYPE = IrDynamicTypeImpl(emptyList(), Variance.INVARIANT)

    private fun deserializeDynamicType(proto: ProtoDynamicType): IrDynamicType {
        return if (proto.annotationCount == 0) {
            SIMPLE_DYNAMIC_TYPE
        } else {
            val annotations = deserializeAnnotations(proto.annotationList)
            IrDynamicTypeImpl(annotations, Variance.INVARIANT)
        }
    }

    private fun deserializeDefinitelyNotNullType(proto: ProtoDefinitelyNotNullType): IrSimpleType {
        assert(proto.typesCount == 1) { "Only DefinitelyNotNull type is now supported" }
        // TODO support general case of intersection type
        return deserializeIrType(proto.typesList[0]).makeNotNull() as IrSimpleType
    }

    private fun deserializeIrTypeData(proto: ProtoType): IrType {
        return when (proto.kindCase) {
            DNN -> deserializeDefinitelyNotNullType(proto.dnn)
            SIMPLE -> deserializeSimpleType(proto.simple)
            LEGACYSIMPLE -> deserializeLegacySimpleType(proto.legacySimple)
            DYNAMIC -> deserializeDynamicType(proto.dynamic)
            else -> error("Unexpected IrType kind: ${proto.kindCase}")
        }
    }

    private var currentParent: IrDeclarationParent = parent

    private inline fun <T : IrDeclarationParent> T.usingParent(block: T.() -> Unit): T =
        this.apply {
            val oldParent = currentParent
            currentParent = this
            try {
                block(this)
            } finally {
                currentParent = oldParent
            }
        }

    internal fun deserializeIrSymbol(code: Long): IrSymbol {
        return symbolDeserializer.deserializeSymbolWithOwnerMaybeInOtherFile(code)
    }

    private var isEffectivelyExternal = false

    private inline fun withExternalValue(value: Boolean, fn: () -> Unit) {
        val oldExternalValue = isEffectivelyExternal
        isEffectivelyExternal = value
        try {
            fn()
        } finally {
            isEffectivelyExternal = oldExternalValue
        }
    }

    private inline fun <T> withDeserializedIrDeclarationBase(
        proto: ProtoDeclarationBase,
        setParent: Boolean = true,
        block: (IrSymbol, IdSignature, Int, Int, IrDeclarationOrigin, Long) -> T,
    ): T where T : IrDeclaration, T : IrSymbolOwner {
        val (s, uid) = symbolDeserializer.deserializeSymbolToDeclareInCurrentFile(proto.symbol)
        val coordinates = BinaryCoordinates.decode(proto.coordinates)
        val result = block(
            s,
            uid,
            coordinates.startOffset, coordinates.endOffset,
            deserializeIrDeclarationOrigin(proto.originName), proto.flags
        )
        // avoid duplicate annotations for local variables
        result.annotations = deserializeAnnotations(proto.annotationList)
        if (setParent) {
            result.parent = currentParent
        }
        return result
    }

    private fun deserializeIrTypeParameter(
        proto: ProtoTypeParameter,
        index: Int,
        isGlobal: Boolean,
        setParent: Boolean = true,
    ): IrTypeParameter {

        val name = deserializeName(proto.name)
        val coordinates = BinaryCoordinates.decode(proto.base.coordinates)
        val flags = TypeParameterFlags.decode(proto.base.flags)

        val signature: IdSignature = symbolDeserializer.deserializeIdSignature(
            symbolDeserializer.parseSymbolData(proto.base.symbol).signatureId
        )

        val symbolFactory: () -> IrTypeParameterSymbol = {
            symbolDeserializer.deserializeSymbolWithOwnerInCurrentFile(signature, TYPE_PARAMETER_SYMBOL)
                .checkSymbolType(TYPE_PARAMETER_SYMBOL)
        }

        val typeParameterFactory: (IrTypeParameterSymbol) -> IrTypeParameter = { symbol: IrTypeParameterSymbol ->
            createIfUnbound(symbol) {
                irFactory.createTypeParameter(
                    startOffset = coordinates.startOffset,
                    endOffset = coordinates.endOffset,
                    origin = deserializeIrDeclarationOrigin(proto.base.originName),
                    name = name,
                    symbol = symbol,
                    variance = flags.variance,
                    index = index,
                    isReified = flags.isReified
                )
            }
        }

        val typeParameter: IrTypeParameter = if (isGlobal) {
            symbolTable.declareGlobalTypeParameter(
                signature = signature,
                symbolFactory = symbolFactory,
                typeParameterFactory = typeParameterFactory
            )
        } else {
            symbolTable.declareScopedTypeParameter(
                signature = signature,
                symbolFactory = { symbolFactory() },
                typeParameterFactory = typeParameterFactory
            )
        }

        typeParameter.annotations = deserializeAnnotations(proto.base.annotationList)
        if (setParent) typeParameter.parent = currentParent
        return typeParameter
    }

    private fun deserializeIrValueParameter(proto: ProtoValueParameter, kind: IrParameterKind, setParent: Boolean = true): IrValueParameter =
        withDeserializedIrDeclarationBase(proto.base, setParent) { symbol, _, startOffset, endOffset, origin, fcode ->
            val flags = ValueParameterFlags.decode(fcode)
            val nameAndType = BinaryNameAndType.decode(proto.nameType)
            irFactory.createValueParameter(
                startOffset = startOffset,
                endOffset = endOffset,
                origin = origin,
                kind = kind,
                name = deserializeName(nameAndType.nameIndex),
                type = deserializeIrType(nameAndType.typeIndex),
                isAssignable = flags.isAssignable,
                symbol = symbol.checkSymbolType(fallbackSymbolKind = null),
                varargElementType = if (proto.hasVarargElementType()) deserializeIrType(proto.varargElementType) else null,
                isCrossinline = flags.isCrossInline,
                isNoinline = flags.isNoInline,
                isHidden = flags.isHidden,
            ).apply {
                if (proto.hasDefaultValue())
                    defaultValue = deserializeExpressionBody(proto.defaultValue)
                        ?: irFactory.createExpressionBody(IrCompositeImpl(startOffset, endOffset, type))
            }
        }

    private fun deserializeIrClass(proto: ProtoClass, setParent: Boolean = true): IrClass =
        withDeserializedIrDeclarationBase(proto.base, setParent) { symbol, signature, startOffset, endOffset, origin, fcode ->
            val flags = ClassFlags.decode(fcode)
            // Similar to 948dc4f3, compatibility hack for libs that were generated before 1.6.20.
            val effectiveModality = if (flags.kind == ClassKind.ANNOTATION_CLASS) {
                Modality.OPEN
            } else {
                flags.modality
            }
            symbolTable.declareClass(signature, { symbol.checkSymbolType(CLASS_SYMBOL) }) {
                createIfUnbound(it) {
                    irFactory.createClass(
                        startOffset = startOffset,
                        endOffset = endOffset,
                        origin = origin,
                        name = deserializeName(proto.name),
                        visibility = flags.visibility,
                        symbol = it,
                        kind = flags.kind,
                        modality = effectiveModality,
                        isExternal = flags.isExternal || isEffectivelyExternal,
                        isCompanion = flags.isCompanion,
                        isInner = flags.isInner,
                        isData = flags.isData,
                        isValue = flags.isValue,
                        isExpect = flags.isExpect,
                        isFun = flags.isFun,
                        hasEnumEntries = flags.hasEnumEntries,
                    )
                }
            }.usingParent {
                typeParameters = deserializeTypeParameters(proto.typeParameterList, true)

                superTypes = proto.superTypeList.memoryOptimizedMap { deserializeIrType(it) }

                withExternalValue(isExternal) {
                    val oldDeclarations = declarations.toSet()
                    proto.declarationList
                        .asSequence()
                        .filterNot { isSkippedFakeOverride(it, this) }
                        // On JVM, deserialization may fill bodies of existing declarations, so avoid adding duplicates.
                        .mapNotNullTo(declarations) { declProto -> deserializeDeclaration(declProto).takeIf { it !in oldDeclarations } }
                }

                thisReceiver = deserializeIrValueParameter(proto.thisReceiver, IrParameterKind.DispatchReceiver)

                valueClassRepresentation = when {
                    !flags.isValue -> null
                    proto.hasMultiFieldValueClassRepresentation() && proto.hasInlineClassRepresentation() ->
                        error("Class cannot be both inline and multi-field value: $name")
                    proto.hasInlineClassRepresentation() -> deserializeInlineClassRepresentation(proto.inlineClassRepresentation)
                    proto.hasMultiFieldValueClassRepresentation() ->
                        deserializeMultiFieldValueClassRepresentation(proto.multiFieldValueClassRepresentation)
                    else -> computeMissingInlineClassRepresentationForCompatibility(this)
                }

                // It has been decided not to deserialize the list of sealed subclasses because of KT-54028
                // sealedSubclasses = proto.sealedSubclassList.memoryOptimizedMap { deserializeIrSymbol(it).checkSymbolType(CLASS_SYMBOL) }

                onDeserializedClass(this, signature)
            }
        }

    private fun deserializeInlineClassRepresentation(proto: ProtoIrInlineClassRepresentation): InlineClassRepresentation<IrSimpleType> =
        InlineClassRepresentation(
            deserializeName(proto.underlyingPropertyName),
            deserializeIrType(proto.underlyingPropertyType) as IrSimpleType,
        )

    private fun deserializeMultiFieldValueClassRepresentation(proto: ProtoIrMultiFieldValueClassRepresentation): MultiFieldValueClassRepresentation<IrSimpleType> {
        val names = proto.underlyingPropertyNameList.memoryOptimizedMap { deserializeName(it) }
        val types = proto.underlyingPropertyTypeList.memoryOptimizedMap { deserializeIrType(it) as IrSimpleType }
        return MultiFieldValueClassRepresentation(names memoryOptimizedZip types)
    }

    private fun computeMissingInlineClassRepresentationForCompatibility(irClass: IrClass): InlineClassRepresentation<IrSimpleType> {
        // For inline classes compiled with 1.5.20 or earlier, try to reconstruct inline class representation from the single parameter of
        // the primary constructor. Something similar is happening in `DeserializedClassDescriptor.computeInlineClassRepresentation`.
        // This code will be unnecessary as soon as klibs compiled with Kotlin 1.5.20 are no longer supported.
        val ctor = irClass.primaryConstructor ?: error("Inline class has no primary constructor: ${irClass.render()}")
        val parameter =
            ctor.parameters.singleOrNull() ?: error("Failed to get single parameter of inline class constructor: ${ctor.render()}")
        return InlineClassRepresentation(parameter.name, parameter.type as IrSimpleType)
    }

    private fun deserializeIrTypeAlias(proto: ProtoTypeAlias, setParent: Boolean = true): IrTypeAlias =
        withDeserializedIrDeclarationBase(proto.base, setParent) { symbol, uniqId, startOffset, endOffset, origin, fcode ->
            symbolTable.declareTypeAlias(uniqId, { symbol.checkSymbolType(TYPEALIAS_SYMBOL) }) {
                createIfUnbound(it) {
                    val flags = TypeAliasFlags.decode(fcode)
                    val nameType = BinaryNameAndType.decode(proto.nameType)
                    irFactory.createTypeAlias(
                        startOffset = startOffset,
                        endOffset = endOffset,
                        origin = origin,
                        name = deserializeName(nameType.nameIndex),
                        visibility = flags.visibility,
                        symbol = it,
                        isActual = flags.isActual,
                        expandedType = deserializeIrType(nameType.typeIndex),
                    )
                }
            }.usingParent {
                typeParameters = deserializeTypeParameters(proto.typeParameterList, true)
            }
        }

    private fun deserializeTypeParameters(protos: List<ProtoTypeParameter>, isGlobal: Boolean): List<IrTypeParameter> {
        // NOTE: fun <C : MutableCollection<in T>, T : Any> Array<out T?>.filterNotNullTo(destination: C): C
        return protos.memoryOptimizedMapIndexed { index, proto ->
            deserializeIrTypeParameter(proto, index, isGlobal).apply {
                superTypes = proto.superTypeList.memoryOptimizedMap { deserializeIrType(it) }
            }
        }
    }

    /**
     * In `declarations-only` mode in case of private property/function with inferred anonymous private type like this
     * class C {
     *   private val p = object {
     *     fun foo() = 42
     *   }
     *
     *   private fun f() = object {
     *     fun bar() = "42"
     *   }
     *
     *   private val pp = p.foo()
     *   private fun ff() = f().bar()
     * }
     * object's classifier is leaked outside p/f scopes and accessible on C's level so
     * if their initializer/body weren't read we have unbound `foo/bar` symbol and unbound `object` symbols.
     * To fix this make sure that such declaration forced to be deserialized completely.
     *
     * For more information see `anonymousClassLeak.kt` test and issue KT-40216
     */
    private fun IrType.checkObjectLeak(): Boolean {
        return if (this is IrSimpleType) {
            val signature = classifier.signature

            val possibleLeakedClassifier = (signature == null || signature.isLocal) && classifier !is IrTypeParameterSymbol

            possibleLeakedClassifier || arguments.any { it.typeOrNull?.checkObjectLeak() == true }
        } else false
    }

    private fun <T : IrFunction> T.withBodyGuard(block: T.() -> Unit) {
        val oldBodiesPolicy = areFunctionBodiesDeserialized

        fun checkInlineBody(): Boolean =
            settings.deserializeFunctionBodies == DeserializeFunctionBodies.ONLY_INLINE && this is IrSimpleFunction && isInline

        try {
            areFunctionBodiesDeserialized = oldBodiesPolicy || checkInlineBody() || returnType.checkObjectLeak()
            block()
        } finally {
            areFunctionBodiesDeserialized = oldBodiesPolicy
        }
    }


    private fun IrField.withInitializerGuard(isConst: Boolean, f: IrField.() -> Unit) {
        val oldBodiesPolicy = areFunctionBodiesDeserialized

        try {
            areFunctionBodiesDeserialized = isConst || oldBodiesPolicy || type.checkObjectLeak()
            f()
        } finally {
            areFunctionBodiesDeserialized = oldBodiesPolicy
        }
    }

    private fun loadStatementBodyProto(index: Int): ProtoStatement {
        return libraryFile.statementBody(index)
    }

    private fun loadExpressionBodyProto(index: Int): ProtoExpression {
        return libraryFile.expressionBody(index)
    }

    fun deserializeExpressionBody(index: Int): IrExpressionBody? {
        return if (areFunctionBodiesDeserialized) {
            val bodyData = loadExpressionBodyProto(index)
            irFactory.createExpressionBody(bodyDeserializer.deserializeExpression(bodyData))
        } else {
            null
        }
    }

    fun deserializeStatementBody(index: Int): IrElement? {
        return if (areFunctionBodiesDeserialized) {
            val bodyData = loadStatementBodyProto(index)
            bodyDeserializer.deserializeStatement(bodyData)
        } else {
            null
        }
    }

    private inline fun <reified S : IrFunctionSymbol, T : IrFunction> withDeserializedIrFunctionBase(
        proto: ProtoFunctionBase,
        setParent: Boolean = true,
        fallbackSymbolKind: SymbolKind,
        block: (S, IdSignature, Int, Int, IrDeclarationOrigin, Long) -> T,
    ): T = withDeserializedIrDeclarationBase(proto.base, setParent) { symbol, idSig, startOffset, endOffset, origin, fcode ->
        val functionSymbol: S = symbol.checkSymbolType(fallbackSymbolKind)
        symbolTable.withScope(functionSymbol) {
            block(functionSymbol, idSig, startOffset, endOffset, origin, fcode).usingParent {
                typeParameters = deserializeTypeParameters(proto.typeParameterList, false)
                val nameType = BinaryNameAndType.decode(proto.nameType)
                returnType = deserializeIrType(nameType.typeIndex)

                withBodyGuard {
                    parameters = buildList {
                        if (proto.hasDispatchReceiver()) {
                            add(deserializeIrValueParameter(proto.dispatchReceiver, IrParameterKind.DispatchReceiver))
                        }
                        proto.contextParameterList.mapTo(this) { proto ->
                            deserializeIrValueParameter(proto, IrParameterKind.Context)
                        }
                        if (proto.hasExtensionReceiver()) {
                            add(deserializeIrValueParameter(proto.extensionReceiver, IrParameterKind.ExtensionReceiver))
                        }
                        proto.regularParameterList.mapTo(this) { proto ->
                            deserializeIrValueParameter(proto, IrParameterKind.Regular)
                        }
                    }.compactIfPossible()
                    body =
                        if (proto.hasBody()) deserializeStatementBody(proto.body) as IrBody?
                        else null
                }
            }
        }
    }

    fun <T : IrFunction> T.withDeserializeBodies(block: T.() -> Unit) {
        val oldBodiesPolicy = areFunctionBodiesDeserialized
        try {
            areFunctionBodiesDeserialized = true
            usingParent { block() }
        } finally {
            areFunctionBodiesDeserialized = oldBodiesPolicy
        }
    }

    internal fun deserializeIrFunction(proto: ProtoFunction, setParent: Boolean = true): IrSimpleFunction =
        withDeserializedIrFunctionBase<IrSimpleFunctionSymbol, IrSimpleFunction>(
            proto.base,
            setParent,
            FUNCTION_SYMBOL
        ) { symbol, idSig, startOffset, endOffset, origin, fcode ->
            val flags = FunctionFlags.decode(fcode)
            symbolTable.declareSimpleFunction(idSig, { symbol }) {
                createIfUnbound(it) {
                    val nameType = BinaryNameAndType.decode(proto.base.nameType)
                    irFactory.createSimpleFunction(
                        startOffset = startOffset,
                        endOffset = endOffset,
                        origin = origin,
                        name = deserializeName(nameType.nameIndex),
                        visibility = flags.visibility,
                        isInline = flags.isInline,
                        isExpect = flags.isExpect,
                        returnType = null,
                        modality = flags.modality,
                        symbol = it,
                        isTailrec = flags.isTailrec,
                        isSuspend = flags.isSuspend,
                        isOperator = flags.isOperator,
                        isInfix = flags.isInfix,
                        isExternal = flags.isExternal || isEffectivelyExternal,
                        isFakeOverride = flags.isFakeOverride,
                    )
                }
            }.apply {
                overriddenSymbols =
                    proto.overriddenList.memoryOptimizedMap { deserializeIrSymbol(it).checkSymbolType(FUNCTION_SYMBOL) }
            }
        }

    fun deserializeIrVariable(proto: ProtoVariable, setParent: Boolean = true): IrVariable =
        withDeserializedIrDeclarationBase(proto.base, setParent) { symbol, _, startOffset, endOffset, origin, fcode ->
            val flags = LocalVariableFlags.decode(fcode)
            val nameType = BinaryNameAndType.decode(proto.nameType)

            IrVariableImpl(
                startOffset, endOffset, origin,
                symbol.checkSymbolType(fallbackSymbolKind = null),
                deserializeName(nameType.nameIndex),
                deserializeIrType(nameType.typeIndex),
                flags.isVar,
                flags.isConst,
                flags.isLateinit
            ).apply {
                if (proto.hasInitializer())
                    initializer = bodyDeserializer.deserializeExpression(proto.initializer)
            }
        }

    private fun deserializeIrEnumEntry(proto: ProtoEnumEntry, setParent: Boolean = true): IrEnumEntry =
        withDeserializedIrDeclarationBase(proto.base, setParent) { symbol, uniqId, startOffset, endOffset, origin, _ ->
            symbolTable.declareEnumEntry(uniqId, { symbol.checkSymbolType(ENUM_ENTRY_SYMBOL) }) {
                createIfUnbound(it) {
                    irFactory.createEnumEntry(startOffset, endOffset, origin, deserializeName(proto.name), it)
                }
            }.apply {
                if (proto.hasCorrespondingClass())
                    correspondingClass = deserializeIrClass(proto.correspondingClass)
                if (proto.hasInitializer())
                    initializerExpression = deserializeExpressionBody(proto.initializer)
            }
        }

    private fun deserializeIrAnonymousInit(proto: ProtoAnonymousInit, setParent: Boolean = true): IrAnonymousInitializer =
        withDeserializedIrDeclarationBase(
            proto.base,
            setParent
        ) { symbol, _, startOffset, endOffset, origin, _ ->
            irFactory.createAnonymousInitializer(startOffset, endOffset, origin, symbol.checkSymbolType(fallbackSymbolKind = null)).apply {
                body = deserializeStatementBody(proto.body) as IrBlockBody? ?: irFactory.createBlockBody(startOffset, endOffset)
            }
        }

    private fun deserializeIrConstructor(proto: ProtoConstructor, setParent: Boolean = true): IrConstructor =
        withDeserializedIrFunctionBase<IrConstructorSymbol, IrConstructor>(
            proto.base,
            setParent,
            CONSTRUCTOR_SYMBOL
        ) { symbol, idSig, startOffset, endOffset, origin, fcode ->
            val flags = FunctionFlags.decode(fcode)
            val nameType = BinaryNameAndType.decode(proto.base.nameType)
            symbolTable.declareConstructor(idSig, { symbol }) {
                createIfUnbound(it) {
                    irFactory.createConstructor(
                        startOffset = startOffset,
                        endOffset = endOffset,
                        origin = origin,
                        name = deserializeName(nameType.nameIndex),
                        visibility = flags.visibility,
                        isInline = flags.isInline,
                        isExpect = flags.isExpect,
                        returnType = null,
                        symbol = it,
                        isPrimary = flags.isPrimary,
                        isExternal = flags.isExternal || isEffectivelyExternal,
                    )
                }
            }
        }


    private fun deserializeIrField(proto: ProtoField, isConst: Boolean, setParent: Boolean = true): IrField =
        withDeserializedIrDeclarationBase(proto.base, setParent) { symbol, uniqId, startOffset, endOffset, origin, fcode ->
            val nameType = BinaryNameAndType.decode(proto.nameType)
            val type = deserializeIrType(nameType.typeIndex)
            val flags = FieldFlags.decode(fcode)

            val field = symbolTable.declareField(uniqId, { symbol.checkSymbolType(FIELD_SYMBOL) }) {
                createIfUnbound(it) {
                    irFactory.createField(
                        startOffset = startOffset,
                        endOffset = endOffset,
                        origin = origin,
                        name = deserializeName(nameType.nameIndex),
                        visibility = flags.visibility,
                        symbol = it,
                        type = type,
                        isFinal = flags.isFinal,
                        isStatic = flags.isStatic,
                        isExternal = flags.isExternal || isEffectivelyExternal,
                    )
                }
            }

            field.usingParent {
                if (proto.hasInitializer()) {
                    withInitializerGuard(isConst) {
                        initializer = deserializeExpressionBody(proto.initializer)
                    }
                }
            }

            field
        }

    private fun deserializeIrLocalDelegatedProperty(
        proto: ProtoLocalDelegatedProperty,
        setParent: Boolean = true,
    ): IrLocalDelegatedProperty =
        withDeserializedIrDeclarationBase(proto.base, setParent) { symbol, _, startOffset, endOffset, origin, fcode ->
            val flags = LocalVariableFlags.decode(fcode)
            val nameAndType = BinaryNameAndType.decode(proto.nameType)

            val prop = irFactory.createLocalDelegatedProperty(
                startOffset = startOffset,
                endOffset = endOffset,
                origin = origin,
                name = deserializeName(nameAndType.nameIndex),
                symbol = symbol.checkSymbolType(fallbackSymbolKind = null),
                type = deserializeIrType(nameAndType.typeIndex),
                isVar = flags.isVar,
            )

            prop.apply {
                delegate = deserializeIrVariable(proto.delegate)
                getter = deserializeIrFunction(proto.getter)
                if (proto.hasSetter())
                    setter = deserializeIrFunction(proto.setter)
            }
        }

    private fun deserializeIrProperty(proto: ProtoProperty, setParent: Boolean = true): IrProperty =
        withDeserializedIrDeclarationBase(proto.base, setParent) { symbol, uniqId, startOffset, endOffset, origin, fcode ->
            val flags = PropertyFlags.decode(fcode)
            val propertySymbol: IrPropertySymbol = symbol.checkSymbolType(PROPERTY_SYMBOL)
            val prop = symbolTable.declareProperty(uniqId, { propertySymbol }) {
                createIfUnbound(it) {
                    irFactory.createProperty(
                        startOffset = startOffset,
                        endOffset = endOffset,
                        origin = origin,
                        name = deserializeName(proto.name),
                        visibility = flags.visibility,
                        modality = flags.modality,
                        symbol = it,
                        isVar = flags.isVar,
                        isConst = flags.isConst,
                        isLateinit = flags.isLateinit,
                        isDelegated = flags.isDelegated,
                        isExternal = flags.isExternal || isEffectivelyExternal,
                        isExpect = flags.isExpect,
                        isFakeOverride = flags.isFakeOverride,
                    )
                }
            }

            prop.apply {
                withExternalValue(isExternal) {
                    if (proto.hasGetter()) {
                        getter = deserializeIrFunction(proto.getter).also {
                            it.correspondingPropertySymbol = propertySymbol
                        }
                    }
                    if (proto.hasSetter()) {
                        setter = deserializeIrFunction(proto.setter).also {
                            it.correspondingPropertySymbol = propertySymbol
                        }
                    }
                    if (proto.hasBackingField()) {
                        backingField = deserializeIrField(proto.backingField, prop.isConst).also {
                            it.correspondingPropertySymbol = propertySymbol
                        }
                    }
                }
            }
        }

    private companion object {
        private val declarationOriginIndex by lazy {
            IrDeclarationOrigin.Companion::class
                .declaredMemberProperties
                .mapNotNull { it.get(IrDeclarationOrigin.Companion) as? IrDeclarationOriginImpl }
                .associateBy { it.name }
        }
        private val unknownDeclarationOriginCache = mutableMapOf<String, IrDeclarationOrigin>()
    }

    private fun deserializeIrDeclarationOrigin(protoName: Int): IrDeclarationOrigin {
        val originName = libraryFile.string(protoName)
        return IrDeclarationOrigin.GeneratedByPlugin.fromSerializedString(originName)
            ?: declarationOriginIndex[originName]
            ?: unknownDeclarationOriginCache.getOrPut(originName) { IrDeclarationOriginImpl(originName) }
    }

    fun deserializeDeclaration(proto: ProtoDeclaration, setParent: Boolean = true): IrDeclaration {
        val declaration: IrDeclaration = when (proto.declaratorCase!!) {
            IR_ANONYMOUS_INIT -> deserializeIrAnonymousInit(proto.irAnonymousInit, setParent)
            IR_CONSTRUCTOR -> deserializeIrConstructor(proto.irConstructor, setParent)
            IR_FIELD -> deserializeIrField(proto.irField, isConst = false, setParent)
            IR_CLASS -> deserializeIrClass(proto.irClass, setParent)
            IR_FUNCTION -> deserializeIrFunction(proto.irFunction, setParent)
            IR_PROPERTY -> deserializeIrProperty(proto.irProperty, setParent)
            IR_TYPE_PARAMETER -> error("") // deserializeIrTypeParameter(proto.irTypeParameter, proto.irTypeParameter.index, proto.irTypeParameter.isGlobal)
            IR_VARIABLE -> deserializeIrVariable(proto.irVariable, setParent)
            IR_VALUE_PARAMETER -> error("") // deserializeIrValueParameter(proto.irValueParameter, proto.irValueParameter.index)
            IR_ENUM_ENTRY -> deserializeIrEnumEntry(proto.irEnumEntry, setParent)
            IR_LOCAL_DELEGATED_PROPERTY -> deserializeIrLocalDelegatedProperty(proto.irLocalDelegatedProperty, setParent)
            IR_TYPE_ALIAS -> deserializeIrTypeAlias(proto.irTypeAlias, setParent)
            DECLARATOR_NOT_SET -> error("Declaration deserialization not implemented: ${proto.declaratorCase}")
        }

        return declaration
    }

    // Depending on deserialization strategy we either deserialize public api fake overrides
    // or reconstruct them after IR linker completes.
    private fun isSkippedFakeOverride(fakeOverrideProto: ProtoDeclaration, parent: IrClass): Boolean {
        if (needToDeserializeFakeOverrides(parent)) return false

        val symbol = when (fakeOverrideProto.declaratorCase!!) {
            IR_FUNCTION -> symbolDeserializer.deserializeSymbolToDeclareInCurrentFile(fakeOverrideProto.irFunction.base.base.symbol).first
            IR_PROPERTY -> symbolDeserializer.deserializeSymbolToDeclareInCurrentFile(fakeOverrideProto.irProperty.base.symbol).first
            // Don't consider IR_FIELDS here.
            else -> return false
        }
        if (symbol.signature?.isPubliclyVisible != true) return false

        return when (fakeOverrideProto.declaratorCase!!) {
            IR_FUNCTION -> FunctionFlags.decode(fakeOverrideProto.irFunction.base.base.flags).isFakeOverride
            IR_PROPERTY -> PropertyFlags.decode(fakeOverrideProto.irProperty.base.flags).isFakeOverride
            // Don't consider IR_FIELDS here.
            else -> false
        }
    }

    /**
     * This function allows to check deserialized symbols. If the deserialized symbol mismatches the symbol kind
     * at the call site in the deserializer then generate and reference another symbol with
     * the same signature. In case PL is off, just throw [IrSymbolTypeMismatchException].
     *
     * Note: [fallbackSymbolKind] must not completely match [S], but it should represent a subclass of [S].
     *
     * Example: [S] is [IrClassifierSymbol] and [fallbackSymbolKind] is [CLASS_SYMBOL],
     * which is only one possible option along with [TYPE_PARAMETER_SYMBOL].
     *
     * Note, that for local IR declarations such as [IrValueDeclaration] [fallbackSymbolKind] can be left null.
     */
    internal inline fun <reified S : IrSymbol> IrSymbol.checkSymbolType(fallbackSymbolKind: SymbolKind?): S {
        if (this is S) return this // Fast pass.

        specialProcessingForMismatchedSymbolKind?.let {
            return it(this, fallbackSymbolKind) as S
        }

        throw IrSymbolTypeMismatchException(S::class.java, this)
    }

    private inline fun <Declaration : IrDeclaration, Symbol : IrBindableSymbol<*, Declaration>> createIfUnbound(
        symbol: Symbol,
        create: () -> Declaration,
    ): Declaration = if (settings.allowAlreadyBoundSymbols && symbol.isBound) {
        symbol.owner
    } else {
        create()
    }
}
