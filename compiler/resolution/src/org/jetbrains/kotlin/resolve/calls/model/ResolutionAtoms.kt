/*
 * Copyright 2000-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.calls.model

import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.calls.components.ReturnArgumentsInfo
import org.jetbrains.kotlin.resolve.calls.components.TypeArgumentsToParametersMapper
import org.jetbrains.kotlin.resolve.calls.components.candidate.CallableReferenceResolutionCandidate
import org.jetbrains.kotlin.resolve.calls.components.candidate.ResolutionCandidate
import org.jetbrains.kotlin.resolve.calls.components.extractInputOutputTypesFromCallableReferenceExpectedType
import org.jetbrains.kotlin.resolve.calls.inference.NewConstraintSystem
import org.jetbrains.kotlin.resolve.calls.inference.components.FreshVariableNewTypeSubstitutor
import org.jetbrains.kotlin.resolve.calls.inference.components.NewTypeSubstitutor
import org.jetbrains.kotlin.resolve.calls.inference.model.NewConstraintError
import org.jetbrains.kotlin.resolve.calls.inference.model.NewConstraintMismatch
import org.jetbrains.kotlin.resolve.calls.inference.model.NewConstraintWarning
import org.jetbrains.kotlin.resolve.calls.inference.model.TypeVariableForLambdaReturnType
import org.jetbrains.kotlin.resolve.calls.tasks.ExplicitReceiverKind
import org.jetbrains.kotlin.resolve.constants.IntegerValueTypeConstant
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeConstructor
import org.jetbrains.kotlin.types.UnwrappedType
import org.jetbrains.kotlin.types.model.K2Only
import org.jetbrains.kotlin.types.model.KotlinTypeMarker
import org.jetbrains.kotlin.types.typeUtil.unCapture
import org.jetbrains.kotlin.utils.addIfNotNull

/**
 * Call, Callable reference, lambda & function expression, collection literal.
 * In future we should add literals here, because they have similar lifecycle.
 *
 * Expression with type is also primitive. This is done for simplification. todo
 */
interface ResolutionAtom

sealed interface CallableReferenceResolutionAtom : ResolutionAtom {
    val lhsResult: LHSResult
    val rhsName: Name
    val call: KotlinCall
}

class CallableReferenceKotlinCall(
    override val call: KotlinCall,
    override val lhsResult: LHSResult,
    override val rhsName: Name,
) : CallableReferenceResolutionAtom

sealed interface ResolvedCallableReferenceAtom

sealed class ResolvedAtom {
    abstract val atom: ResolutionAtom? // CallResolutionResult has no ResolutionAtom

    var analyzed: Boolean = false
        private set

    var subResolvedAtoms: List<ResolvedAtom>? = null
        private set

    protected open fun setAnalyzedResults(subResolvedAtoms: List<ResolvedAtom>) {
        assert(!analyzed) {
            "Already analyzed: $this"
        }

        analyzed = true

        this.subResolvedAtoms = subResolvedAtoms
    }

    // For AllCandidates mode to avoid analyzing postponed arguments
    fun setEmptyAnalyzedResults() {
        setAnalyzedResults(emptyList())
    }
}

abstract class ResolvedCallAtom : ResolvedAtom() {
    abstract override val atom: KotlinCall
    abstract val candidateDescriptor: CallableDescriptor
    abstract val explicitReceiverKind: ExplicitReceiverKind
    abstract val dispatchReceiverArgument: SimpleKotlinCallArgument?
    abstract var extensionReceiverArgument: SimpleKotlinCallArgument?
    abstract val extensionReceiverArgumentCandidates: List<SimpleKotlinCallArgument>?
    abstract var contextReceiversArguments: List<SimpleKotlinCallArgument>
    abstract val typeArgumentMappingByOriginal: TypeArgumentsToParametersMapper.TypeArgumentsMapping
    abstract val argumentMappingByOriginal: Map<ValueParameterDescriptor, ResolvedCallArgument>
    abstract val freshVariablesSubstitutor: FreshVariableNewTypeSubstitutor
    abstract val knownParametersSubstitutor: NewTypeSubstitutor
    abstract val argumentsWithConversion: Map<KotlinCallArgument, SamConversionDescription>
    abstract val argumentsWithSuspendConversion: Map<KotlinCallArgument, UnwrappedType>
    abstract val argumentsWithUnitConversion: Map<KotlinCallArgument, UnwrappedType>
    abstract val argumentsWithConstantConversion: Map<KotlinCallArgument, IntegerValueTypeConstant>
    abstract fun setCandidateDescriptor(newCandidateDescriptor: CallableDescriptor)
}

class SamConversionDescription(
    val convertedTypeByOriginParameter: UnwrappedType,
    val convertedTypeByCandidateParameter: UnwrappedType, // expected type for corresponding argument
    val originalParameterType: UnwrappedType // need to overload resolution on inherited SAM interfaces
)

class ResolvedExpressionAtom(override val atom: ExpressionKotlinCallArgument) : ResolvedAtom() {
    init {
        setAnalyzedResults(listOf())
    }
}

class ResolvedSubCallArgument(override val atom: SubKotlinCallArgument, resolveIndependently: Boolean) : ResolvedAtom() {
    init {
        if (resolveIndependently)
            setAnalyzedResults(listOf())
        else
            setAnalyzedResults(listOf(atom.callResult))
    }
}


sealed class PostponedResolvedAtom : ResolvedAtom(), PostponedResolvedAtomMarker {
    abstract override val inputTypes: Collection<UnwrappedType>
    abstract override val outputType: UnwrappedType?
    abstract override val expectedType: UnwrappedType?
}

class LambdaWithTypeVariableAsExpectedTypeAtom(
    override val atom: LambdaKotlinCallArgument,
    override val expectedType: UnwrappedType
) : PostponedResolvedAtom(), LambdaWithTypeVariableAsExpectedTypeMarker {
    override val inputTypes: Collection<UnwrappedType> get() = listOf(expectedType)
    override val outputType: UnwrappedType? get() = null

    override var revisedExpectedType: UnwrappedType? = null
        private set

    override var parameterTypesFromDeclaration: List<UnwrappedType?>? = null
        private set

    override fun updateParameterTypesFromDeclaration(types: List<KotlinTypeMarker?>?) {
        @Suppress("UNCHECKED_CAST")
        types as List<UnwrappedType?>?
        parameterTypesFromDeclaration = types
    }

    override fun reviseExpectedType(expectedType: KotlinTypeMarker) {
        require(expectedType is UnwrappedType)
        revisedExpectedType = expectedType
    }

    fun setAnalyzed(resolvedLambdaAtom: ResolvedLambdaAtom) {
        setAnalyzedResults(listOf(resolvedLambdaAtom))
    }
}

class ResolvedLambdaAtom(
    override val atom: LambdaKotlinCallArgument,
    val isSuspend: Boolean,
    val receiver: UnwrappedType?,
    val contextReceivers: List<UnwrappedType>,
    val parameters: List<UnwrappedType>,
    val returnType: UnwrappedType,
    val typeVariableForLambdaReturnType: TypeVariableForLambdaReturnType?,
    override val expectedType: UnwrappedType?
) : PostponedResolvedAtom() {
    /**
     * [resultArgumentsInfo] can be null only if lambda was analyzed in process of resolve
     *   ambiguity by lambda return type
     * There is a contract that [resultArgumentsInfo] will be not null for unwrapped lambda atom
     *   (see [unwrap])
     */
    var resultArgumentsInfo: ReturnArgumentsInfo? = null
        private set

    fun setAnalyzedResults(
        resultArguments: ReturnArgumentsInfo?,
        subResolvedAtoms: List<ResolvedAtom>
    ) {
        this.resultArgumentsInfo = resultArguments
        setAnalyzedResults(subResolvedAtoms)
    }

    override val inputTypes: Collection<UnwrappedType>
        get() {
            if (receiver == null && contextReceivers.isEmpty()) return parameters
            return ArrayList<UnwrappedType>(parameters.size + contextReceivers.size + (if (receiver != null) 1 else 0)).apply {
                addAll(parameters)
                addIfNotNull(receiver)
                addAll(contextReceivers)
            }
        }

    override val outputType: UnwrappedType get() = returnType
}

fun ResolvedLambdaAtom.unwrap(): ResolvedLambdaAtom {
    return if (resultArgumentsInfo != null) this else subResolvedAtoms!!.single() as ResolvedLambdaAtom
}

abstract class ResolvedCallableReferenceArgumentAtom(
    override val atom: CallableReferenceKotlinCallArgument,
    override val expectedType: UnwrappedType?
) : PostponedResolvedAtom(), ResolvedCallableReferenceAtom {
    var candidate: CallableReferenceResolutionCandidate? = null
        private set

    var completed: Boolean = false

    fun setAnalyzedResults(
        candidate: CallableReferenceResolutionCandidate?,
        subResolvedAtoms: List<ResolvedAtom>
    ) {
        this.candidate = candidate
        setAnalyzedResults(subResolvedAtoms)
    }

}

class EagerCallableReferenceAtom(
    atom: CallableReferenceKotlinCallArgument,
    expectedType: UnwrappedType?
) : ResolvedCallableReferenceArgumentAtom(atom, expectedType) {
    override val inputTypes: Collection<UnwrappedType> get() = emptyList()
    override val outputType: UnwrappedType? get() = null

    fun transformToPostponed(): PostponedCallableReferenceAtom = PostponedCallableReferenceAtom(this)
}

sealed class AbstractPostponedCallableReferenceAtom(
    atom: CallableReferenceKotlinCallArgument,
    expectedType: UnwrappedType?
) : ResolvedCallableReferenceArgumentAtom(atom, expectedType) {
    override val inputTypes: Collection<UnwrappedType>
        get() = extractInputOutputTypesFromCallableReferenceExpectedType(expectedType)?.inputTypes ?: listOfNotNull(expectedType)

    override val outputType: UnwrappedType?
        get() = extractInputOutputTypesFromCallableReferenceExpectedType(expectedType)?.outputType
}

class CallableReferenceWithRevisedExpectedTypeAtom(
    atom: CallableReferenceKotlinCallArgument,
    expectedType: UnwrappedType?,
) : AbstractPostponedCallableReferenceAtom(atom, expectedType)

class PostponedCallableReferenceAtom(
    eagerCallableReferenceAtom: EagerCallableReferenceAtom
) : AbstractPostponedCallableReferenceAtom(eagerCallableReferenceAtom.atom, eagerCallableReferenceAtom.expectedType),
    PostponedCallableReferenceMarker {
    override var revisedExpectedType: UnwrappedType? = null
        private set

    override fun reviseExpectedType(expectedType: KotlinTypeMarker) {
        require(expectedType is UnwrappedType)
        revisedExpectedType = expectedType
    }
}

class ResolvedCollectionLiteralAtom(
    override val atom: CollectionLiteralKotlinCallArgument,
    val expectedType: UnwrappedType?
) : ResolvedAtom() {
    init {
        setAnalyzedResults(listOf())
    }
}

sealed class CallResolutionResult(
    resultCallAtom: ResolvedCallAtom?,
    val diagnostics: List<KotlinCallDiagnostic>,
    val constraintSystem: NewConstraintSystem
) : ResolvedAtom() {
    init {
        setAnalyzedResults(listOfNotNull(resultCallAtom))
    }

    final override fun setAnalyzedResults(subResolvedAtoms: List<ResolvedAtom>) {
        super.setAnalyzedResults(subResolvedAtoms)
    }

    fun completedDiagnostic(substitutor: NewTypeSubstitutor): List<KotlinCallDiagnostic> {
        return diagnostics.map {
            val error = it.constraintSystemError ?: return@map it
            if (error !is NewConstraintMismatch) return@map it
            val lowerType = (error.lowerType as? KotlinType)?.unwrap() ?: return@map it
            val newLowerType = substitutor.safeSubstitute(lowerType.unCapture())

            @OptIn(K2Only::class)
            when (error) {
                is NewConstraintError -> NewConstraintError(newLowerType, error.upperType, error.position).asDiagnostic()
                is NewConstraintWarning -> NewConstraintWarning(newLowerType, error.upperType, error.position).asDiagnostic()
            }
        }
    }

    override val atom: ResolutionAtom? get() = null

    override fun toString(): String = "diagnostics: (${diagnostics.joinToString()})"
}

open class SingleCallResolutionResult(
    val resultCallAtom: ResolvedCallAtom,
    diagnostics: List<KotlinCallDiagnostic>,
    constraintSystem: NewConstraintSystem
) : CallResolutionResult(resultCallAtom, diagnostics, constraintSystem)

class PartialCallResolutionResult(
    resultCallAtom: ResolvedCallAtom,
    diagnostics: List<KotlinCallDiagnostic>,
    constraintSystem: NewConstraintSystem,
    val forwardToInferenceSession: Boolean = false
) : SingleCallResolutionResult(resultCallAtom, diagnostics, constraintSystem)

class CompletedCallResolutionResult(
    resultCallAtom: ResolvedCallAtom,
    diagnostics: List<KotlinCallDiagnostic>,
    constraintSystem: NewConstraintSystem
) : SingleCallResolutionResult(resultCallAtom, diagnostics, constraintSystem)

class ErrorCallResolutionResult(
    resultCallAtom: ResolvedCallAtom,
    diagnostics: List<KotlinCallDiagnostic>,
    constraintSystem: NewConstraintSystem
) : SingleCallResolutionResult(resultCallAtom, diagnostics, constraintSystem)

class AllCandidatesResolutionResult(
    val allCandidates: Collection<CandidateWithDiagnostics>,
    constraintSystem: NewConstraintSystem
) : CallResolutionResult(null, emptyList(), constraintSystem)

data class CandidateWithDiagnostics(val candidate: ResolutionCandidate, val diagnostics: List<KotlinCallDiagnostic>)

fun CallResolutionResult.resultCallAtom(): ResolvedCallAtom? =
    if (this is SingleCallResolutionResult) resultCallAtom else null

val ResolvedCallAtom.freshReturnType: UnwrappedType?
    get() {
        val returnType = candidateDescriptor.returnType ?: return null
        return freshVariablesSubstitutor.safeSubstitute(returnType.unwrap())
    }

class PartialCallContainer(val result: PartialCallResolutionResult?) {
    companion object {
        val empty = PartialCallContainer(null)
    }
}

/*
 * Used only for delegated properties with one good candidate and one for bad
 * e.g. in case `var x by lazy { "" }
 */
class StubResolvedAtom(val typeVariable: TypeConstructor) : ResolvedAtom() {
    override val atom: ResolutionAtom? get() = null
}
