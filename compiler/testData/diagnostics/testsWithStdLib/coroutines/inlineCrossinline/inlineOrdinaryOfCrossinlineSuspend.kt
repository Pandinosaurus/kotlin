// RUN_PIPELINE_TILL: FRONTEND
// FIR_IDENTICAL
// LANGUAGE: +ForbidExtensionCallsOnInlineFunctionalParameters
// DIAGNOSTICS: -UNUSED_VARIABLE -UNUSED_PARAMETER -NOTHING_TO_INLINE
// SKIP_TXT
// WITH_COROUTINES
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*
import helpers.*

interface SuspendRunnable {
    suspend fun run()
}

// Function is NOT suspend
// parameter is crossinline
// parameter is suspend
// Block is NOT allowed to be called inside the body of owner inline function
// Block is allowed to be called from nested classes/lambdas (as common crossinlines)
// It is NOT possible to call startCoroutine on the parameter
// suspend calls possible inside lambda matching to the parameter

inline fun test(crossinline c: suspend () -> Unit)  {
    <!ILLEGAL_SUSPEND_FUNCTION_CALL!>c<!>()
    val o = object : SuspendRunnable {
        override suspend fun run() {
            c()
        }
    }
    val l: suspend () -> Unit = { c() }
    <!USAGE_IS_NOT_INLINABLE!>c<!>.startCoroutine(EmptyContinuation)
}

suspend fun calculate() = "OK"

fun box() {
    test {
        calculate()
    }
}

/* GENERATED_FIR_TAGS: anonymousObjectExpression, assignment, classDeclaration, companionObject, crossinline,
functionDeclaration, functionalType, inline, interfaceDeclaration, lambdaLiteral, localProperty, nullableType,
objectDeclaration, override, primaryConstructor, propertyDeclaration, safeCall, stringLiteral, suspend, thisExpression,
typeParameter */
