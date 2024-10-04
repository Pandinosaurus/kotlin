package org.jetbrains.kotlinx.dataframe.plugin.impl.api

import org.jetbrains.kotlin.descriptors.EffectiveVisibility
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlinx.dataframe.plugin.classId
import org.jetbrains.kotlinx.dataframe.plugin.utils.Names
import org.jetbrains.kotlin.fir.declarations.FirResolvePhase
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.declarations.utils.effectiveVisibility
import org.jetbrains.kotlin.fir.declarations.utils.isEnumClass
import org.jetbrains.kotlin.fir.declarations.utils.isStatic
import org.jetbrains.kotlin.fir.expressions.FirCallableReferenceAccess
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirGetClassCall
import org.jetbrains.kotlin.fir.expressions.FirVarargArgumentsExpression
import org.jetbrains.kotlin.fir.java.JavaTypeParameterStack
import org.jetbrains.kotlin.fir.java.declarations.FirJavaClass
import org.jetbrains.kotlin.fir.java.resolveIfJavaType
import org.jetbrains.kotlin.fir.references.toResolvedPropertySymbol
import org.jetbrains.kotlin.fir.resolve.ScopeSession
import org.jetbrains.kotlin.fir.scopes.collectAllFunctions
import org.jetbrains.kotlin.fir.scopes.collectAllProperties
import org.jetbrains.kotlin.fir.scopes.unsubstitutedScope
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.ConeClassLikeLookupTagImpl
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.types.ConeClassLikeType
import org.jetbrains.kotlin.fir.types.ConeFlexibleType
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.ConeNullability
import org.jetbrains.kotlin.fir.types.ConeStarProjection
import org.jetbrains.kotlin.fir.types.ConeTypeParameterType
import org.jetbrains.kotlin.fir.types.canBeNull
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.constructClassLikeType
import org.jetbrains.kotlin.fir.types.impl.ConeClassLikeTypeImpl
import org.jetbrains.kotlin.fir.types.isArrayTypeOrNullableArrayType
import org.jetbrains.kotlin.fir.types.isNullable
import org.jetbrains.kotlin.fir.types.isStarProjection
import org.jetbrains.kotlin.fir.types.isSubtypeOf
import org.jetbrains.kotlin.fir.types.resolvedType
import org.jetbrains.kotlin.fir.types.toRegularClassSymbol
import org.jetbrains.kotlin.fir.types.toSymbol
import org.jetbrains.kotlin.fir.types.type
import org.jetbrains.kotlin.fir.types.typeContext
import org.jetbrains.kotlin.fir.types.upperBoundIfFlexible
import org.jetbrains.kotlin.fir.types.withArguments
import org.jetbrains.kotlin.fir.types.withNullability
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.name.StandardClassIds.List
import org.jetbrains.kotlin.types.checker.SimpleClassicTypeSystemContext.withNullability
import org.jetbrains.kotlinx.dataframe.codeGen.*
import org.jetbrains.kotlinx.dataframe.plugin.extensions.KotlinTypeFacade
import org.jetbrains.kotlinx.dataframe.plugin.extensions.wrap
import org.jetbrains.kotlinx.dataframe.plugin.impl.AbstractInterpreter
import org.jetbrains.kotlinx.dataframe.plugin.impl.AbstractSchemaModificationInterpreter
import org.jetbrains.kotlinx.dataframe.plugin.impl.Arguments
import org.jetbrains.kotlinx.dataframe.plugin.impl.Interpreter
import org.jetbrains.kotlinx.dataframe.plugin.impl.PluginDataFrameSchema
import org.jetbrains.kotlinx.dataframe.plugin.impl.Present
import org.jetbrains.kotlinx.dataframe.plugin.impl.SimpleCol
import org.jetbrains.kotlinx.dataframe.plugin.impl.SimpleDataColumn
import org.jetbrains.kotlinx.dataframe.plugin.impl.SimpleColumnGroup
import org.jetbrains.kotlinx.dataframe.plugin.impl.SimpleFrameColumn
import org.jetbrains.kotlinx.dataframe.plugin.impl.dsl
import org.jetbrains.kotlinx.dataframe.plugin.impl.simpleColumnOf
import org.jetbrains.kotlinx.dataframe.plugin.impl.type
import org.jetbrains.kotlinx.dataframe.plugin.utils.Names.DATA_ROW_CLASS_ID
import org.jetbrains.kotlinx.dataframe.plugin.utils.Names.DATA_SCHEMA_CLASS_ID
import org.jetbrains.kotlinx.dataframe.plugin.utils.Names.DF_CLASS_ID
import java.util.*

class ToDataFrameDsl : AbstractSchemaModificationInterpreter() {
    val Arguments.receiver: FirExpression? by arg(lens = Interpreter.Id)
    val Arguments.body by dsl()
    override fun Arguments.interpret(): PluginDataFrameSchema {
        val dsl = CreateDataFrameDslImplApproximation()
        body(dsl, mapOf("explicitReceiver" to Interpreter.Success(receiver)))
        return PluginDataFrameSchema(dsl.columns)
    }
}

class ToDataFrame : AbstractSchemaModificationInterpreter() {
    val Arguments.receiver: FirExpression? by arg(lens = Interpreter.Id)
    val Arguments.maxDepth: Number by arg(defaultValue = Present(DEFAULT_MAX_DEPTH))

    override fun Arguments.interpret(): PluginDataFrameSchema {
        return toDataFrame(maxDepth.toInt(), receiver, TraverseConfiguration())
    }
}

class ToDataFrameDefault : AbstractSchemaModificationInterpreter() {
    val Arguments.receiver: FirExpression? by arg(lens = Interpreter.Id)

    override fun Arguments.interpret(): PluginDataFrameSchema {
        return toDataFrame(DEFAULT_MAX_DEPTH, receiver, TraverseConfiguration())
    }
}

class ToDataFrameColumn : AbstractSchemaModificationInterpreter() {
    val Arguments.receiver: FirExpression? by arg(lens = Interpreter.Id)
    val Arguments.typeArg0 by type()
    val Arguments.columnName: String by arg()

    override fun Arguments.interpret(): PluginDataFrameSchema {
        return PluginDataFrameSchema(listOf(simpleColumnOf(columnName, typeArg0.type)))
    }
}

private const val DEFAULT_MAX_DEPTH = 0

class Properties0 : AbstractInterpreter<Unit>() {
    val Arguments.dsl: CreateDataFrameDslImplApproximation by arg()
    val Arguments.explicitReceiver: FirExpression? by arg()
    val Arguments.maxDepth: Int by arg()
    val Arguments.body by dsl()

    override fun Arguments.interpret() {
        dsl.configuration.maxDepth = maxDepth
        body(dsl.configuration.traverseConfiguration, emptyMap())
        val schema = toDataFrame(dsl.configuration.maxDepth, explicitReceiver, dsl.configuration.traverseConfiguration)
        dsl.columns.addAll(schema.columns())
    }
}

class CreateDataFrameConfiguration {
    var maxDepth = DEFAULT_MAX_DEPTH
    var traverseConfiguration: TraverseConfiguration = TraverseConfiguration()
}

class TraverseConfiguration {
    val excludeProperties = mutableSetOf<FirCallableReferenceAccess>()
    val excludeClasses = mutableSetOf<FirGetClassCall>()
    val preserveClasses = mutableSetOf<FirGetClassCall>()
    val preserveProperties = mutableSetOf<FirCallableReferenceAccess>()
}

class Preserve0 : AbstractInterpreter<Unit>() {
    val Arguments.dsl: TraverseConfiguration by arg()
    val Arguments.classes: FirVarargArgumentsExpression by arg(lens = Interpreter.Id)

    override fun Arguments.interpret() {
        dsl.preserveClasses.addAll(classes.arguments.filterIsInstance<FirGetClassCall>())
    }
}

class Preserve1 : AbstractInterpreter<Unit>() {
    val Arguments.dsl: TraverseConfiguration by arg()
    val Arguments.properties: FirVarargArgumentsExpression by arg(lens = Interpreter.Id)

    override fun Arguments.interpret() {
        dsl.preserveProperties.addAll(properties.arguments.filterIsInstance<FirCallableReferenceAccess>())
    }
}

class Exclude0 : AbstractInterpreter<Unit>() {
    val Arguments.dsl: TraverseConfiguration by arg()
    val Arguments.classes: FirVarargArgumentsExpression by arg(lens = Interpreter.Id)

    override fun Arguments.interpret() {
        dsl.excludeClasses.addAll(classes.arguments.filterIsInstance<FirGetClassCall>())
    }
}

class Exclude1 : AbstractInterpreter<Unit>() {
    val Arguments.dsl: TraverseConfiguration by arg()
    val Arguments.properties: FirVarargArgumentsExpression by arg(lens = Interpreter.Id)

    override fun Arguments.interpret() {
        dsl.excludeProperties.addAll(properties.arguments.filterIsInstance<FirCallableReferenceAccess>())
    }
}
@Suppress("INVISIBLE_MEMBER")
@OptIn(SymbolInternals::class)
internal fun KotlinTypeFacade.toDataFrame(
    maxDepth: Int,
    explicitReceiver: FirExpression?,
    traverseConfiguration: TraverseConfiguration
): PluginDataFrameSchema {
    fun ConeKotlinType.isValueType() =
        this.isArrayTypeOrNullableArrayType ||
            this.classId == StandardClassIds.Any ||
            this.classId == StandardClassIds.String ||
            this.classId == StandardClassIds.Boolean ||
            classId in setOf(Names.DURATION_CLASS_ID, Names. LOCAL_DATE_CLASS_ID, Names.LOCAL_DATE_TIME_CLASS_ID, Names.INSTANT_CLASS_ID) ||
            this.isSubtypeOf(session.builtinTypes.numberType.type, session) ||
            this.isSubtypeOf(StandardClassIds.Number.constructClassLikeType(emptyArray(), isNullable = true), session) ||
            this.toRegularClassSymbol(session)?.isEnumClass ?: false ||
            this.isSubtypeOf(
                ConeClassLikeTypeImpl(
                    ConeClassLikeLookupTagImpl(
                        ClassId(FqName("java.time.temporal"), Name.identifier("Temporal"))
                    ), arrayOf(), isNullable = false
                ), session
            )

    val excludes = traverseConfiguration.excludeProperties.mapNotNullTo(mutableSetOf()) { it.calleeReference.toResolvedPropertySymbol() }
    val excludedClasses = traverseConfiguration.excludeClasses.mapTo(mutableSetOf()) { it.argument.resolvedType }
    val preserveClasses = traverseConfiguration.preserveClasses.mapNotNullTo(mutableSetOf()) { it.classId }
    val preserveProperties = traverseConfiguration.preserveProperties.mapNotNullTo(mutableSetOf()) { it.calleeReference.toResolvedPropertySymbol() }

    fun convert(classLike: ConeKotlinType, depth: Int, makeNullable: Boolean): List<SimpleCol> {
        val symbol = classLike.toRegularClassSymbol(session) ?: return emptyList()
        val scope = symbol.unsubstitutedScope(session, ScopeSession(), false, FirResolvePhase.STATUS)
        val declarations = if (symbol.fir is FirJavaClass) {
            scope
                .collectAllFunctions()
                .filter { !it.isStatic && it.valueParameterSymbols.isEmpty() && it.typeParameterSymbols.isEmpty() }
                .mapNotNull { function ->
                    val name = function.name.identifier
                    if (name.startsWith("get") || name.startsWith("is")) {
                        val propertyName = name
                            .replaceFirst("get", "")
                            .replaceFirst("is", "")
                            .let {
                                if (it.firstOrNull()?.isUpperCase() == true) {
                                    it.replaceFirstChar { it.lowercase(Locale.getDefault()) }
                                } else {
                                    null
                                }
                            }
                        propertyName?.let { function to it }
                    } else {
                        null
                    }
                }
        } else {
            scope
                .collectAllProperties()
                .filterIsInstance<FirPropertySymbol>()
                .map {
                    it to it.name.identifier
                }
        }

        return declarations
            .filterNot { excludes.contains(it.first) }
            .filterNot { excludedClasses.contains(it.first.resolvedReturnType) }
            .filter { it.first.effectiveVisibility == EffectiveVisibility.Public }
            .map { (it, name) ->
                var returnType = it.fir.returnTypeRef.resolveIfJavaType(session, JavaTypeParameterStack.EMPTY, null)
                    .coneType.upperBoundIfFlexible()

                returnType = if (returnType is ConeTypeParameterType) {
                    if (returnType.canBeNull(session)) {
                        session.builtinTypes.nullableAnyType.type
                    } else {
                        session.builtinTypes.anyType.type
                    }
                } else {
                    returnType.withArguments {
                        val type = it.type
                        if (type is ConeTypeParameterType) {
                            session.builtinTypes.nullableAnyType.type
                        } else {
                            type?.upperBoundIfFlexible() ?: it
                        }
                    }
                }

                val fieldKind = returnType.getFieldKind(session)

                val keepSubtree = depth >= maxDepth && !fieldKind.shouldBeConvertedToColumnGroup && !fieldKind.shouldBeConvertedToFrameColumn
                if (keepSubtree || returnType.isValueType() || returnType.classId in preserveClasses || it in preserveProperties) {
                    SimpleDataColumn(name, TypeApproximation(returnType.withNullability(ConeNullability.create(makeNullable), session.typeContext)))
                } else if (
                    returnType.isSubtypeOf(StandardClassIds.Iterable.constructClassLikeType(arrayOf(ConeStarProjection)), session) ||
                    returnType.isSubtypeOf(StandardClassIds.Iterable.constructClassLikeType(arrayOf(ConeStarProjection), isNullable = true), session)
                ) {
                    val type: ConeKotlinType = when (val typeArgument = returnType.typeArguments[0]) {
                        is ConeKotlinType -> typeArgument
                        ConeStarProjection -> session.builtinTypes.nullableAnyType.type
                        else -> session.builtinTypes.nullableAnyType.type
                    }
                    if (type.isValueType()) {
                        val columnType = List.constructClassLikeType(arrayOf(type), returnType.isNullable)
                            .withNullability(ConeNullability.create(makeNullable), session.typeContext)
                            .wrap()
                        SimpleDataColumn(name, columnType)
                    } else {
                        SimpleFrameColumn(name, convert(type, depth + 1, makeNullable = false))
                    }
                } else {
                    SimpleColumnGroup(name, convert(returnType, depth + 1, returnType.isNullable || makeNullable))
                }
            }
    }

    val receiver = explicitReceiver ?: return PluginDataFrameSchema.EMPTY
    val arg = receiver.resolvedType.typeArguments.firstOrNull() ?: return PluginDataFrameSchema.EMPTY
    return when {
        arg.isStarProjection -> PluginDataFrameSchema.EMPTY
        else -> {
            val classLike = when (val type = arg.type) {
                is ConeClassLikeType -> type
                is ConeFlexibleType -> type.upperBound
                else -> null
            } ?: return PluginDataFrameSchema.EMPTY
            val columns = convert(classLike, 0, makeNullable = classLike.isNullable)
            PluginDataFrameSchema(columns)
        }
    }
}

// org.jetbrains.kotlinx.dataframe.codeGen.getFieldKind
@Suppress("INVISIBLE_MEMBER")
private fun ConeKotlinType.getFieldKind(session: FirSession) = when {
    classId == DF_CLASS_ID -> Frame
    classId == List && typeArguments[0].type.hasAnnotation(DATA_SCHEMA_CLASS_ID, session) -> ListToFrame
    classId == DATA_ROW_CLASS_ID -> Group
    hasAnnotation(DATA_SCHEMA_CLASS_ID, session) -> ObjectToGroup
    else -> Default
}


private fun ConeKotlinType?.hasAnnotation(id: ClassId, session: FirSession) =
    this?.toSymbol(session)?.hasAnnotation(id, session) == true


class CreateDataFrameDslImplApproximation {
    val configuration: CreateDataFrameConfiguration = CreateDataFrameConfiguration()
    val columns: MutableList<SimpleCol> = mutableListOf()
}

class ToDataFrameFrom : AbstractInterpreter<Unit>() {
    val Arguments.dsl: CreateDataFrameDslImplApproximation by arg()
    val Arguments.receiver: String by arg()
    val Arguments.expression: TypeApproximation by type()
    override fun Arguments.interpret() {
        dsl.columns += simpleColumnOf(receiver, expression.type)
    }
}
