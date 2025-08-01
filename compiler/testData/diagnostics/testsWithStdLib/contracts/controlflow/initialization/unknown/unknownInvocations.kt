// RUN_PIPELINE_TILL: FRONTEND
// FIR_IDENTICAL
// LANGUAGE: +AllowContractsForCustomFunctions +UseCallsInPlaceEffect
// OPT_IN: kotlin.contracts.ExperimentalContracts
// DIAGNOSTICS: -INVISIBLE_REFERENCE -INVISIBLE_MEMBER

import kotlin.contracts.*

fun <T> inPlace(block: () -> T): T {
    contract {
        callsInPlace(block)
    }
    return block()
}

fun reassignmentAndNoInitializaiton() {
    val x: Int
    inPlace { <!VAL_REASSIGNMENT!>x<!> = 42 }
    <!UNINITIALIZED_VARIABLE!>x<!>.inc()
}

/* GENERATED_FIR_TAGS: assignment, contractCallsEffect, contracts, functionDeclaration, functionalType, integerLiteral,
lambdaLiteral, localProperty, nullableType, propertyDeclaration, typeParameter */
