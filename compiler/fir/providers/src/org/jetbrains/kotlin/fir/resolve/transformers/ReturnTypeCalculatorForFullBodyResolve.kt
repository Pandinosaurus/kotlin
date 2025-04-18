/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.transformers

import org.jetbrains.kotlin.fir.canHaveDeferredReturnTypeCalculation
import org.jetbrains.kotlin.fir.declarations.FirCallableDeclaration
import org.jetbrains.kotlin.fir.diagnostics.ConeSimpleDiagnostic
import org.jetbrains.kotlin.fir.diagnostics.DiagnosticKind
import org.jetbrains.kotlin.fir.render
import org.jetbrains.kotlin.fir.scopes.CallableCopyTypeCalculator
import org.jetbrains.kotlin.fir.types.FirResolvedTypeRef
import org.jetbrains.kotlin.fir.types.builder.buildErrorTypeRef

class ReturnTypeCalculatorForFullBodyResolve private constructor(
    private val diagnosticKind: DiagnosticKind,
    private val reason: String,
    override val callableCopyTypeCalculator: CallableCopyTypeCalculator.DeferredCallableCopyTypeCalculator,
) : ReturnTypeCalculator() {
    companion object {
        /**
         * Uses [CallableCopyTypeCalculator.CalculateDeferredForceLazyResolution].
         */
        val Default: ReturnTypeCalculatorForFullBodyResolve = ReturnTypeCalculatorForFullBodyResolve(
            DiagnosticKind.RecursionInImplicitTypes,
            "Recursion with local function",
            CallableCopyTypeCalculator.CalculateDeferredForceLazyResolution
        )

        /**
         * Uses [CallableCopyTypeCalculator.CalculateDeferredWhenPossible] so it's safe to be used during STATUS phase.
         */
        val Status: ReturnTypeCalculatorForFullBodyResolve = ReturnTypeCalculatorForFullBodyResolve(
            DiagnosticKind.RecursionInImplicitTypes,
            "Recursion with local function",
            CallableCopyTypeCalculator.CalculateDeferredWhenPossible
        )

        val Contract: ReturnTypeCalculatorForFullBodyResolve = ReturnTypeCalculatorForFullBodyResolve(
            DiagnosticKind.InferenceError,
            "Cannot calculate return type during full-body resolution (local class/object?)",
            CallableCopyTypeCalculator.CalculateDeferredForceLazyResolution
        )
    }

    override fun tryCalculateReturnTypeOrNull(declaration: FirCallableDeclaration): FirResolvedTypeRef? {
        val returnTypeRef = declaration.returnTypeRef
        if (returnTypeRef is FirResolvedTypeRef) return returnTypeRef
        if (declaration.canHaveDeferredReturnTypeCalculation) {
            return callableCopyTypeCalculator.computeReturnType(declaration)
        }

        return buildErrorTypeRef { diagnostic = ConeSimpleDiagnostic("$reason: ${declaration.render()}", diagnosticKind) }
    }
}
