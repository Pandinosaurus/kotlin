// RUN_PIPELINE_TILL: BACKEND
// FIR_IDENTICAL
// SKIP_TXT
import kotlin.coroutines.*

fun <T> foo(): Continuation<T> = null!!

fun bar() {
    suspend {
        println()
    }.startCoroutine(foo<Unit>())
}

/* GENERATED_FIR_TAGS: checkNotNullCall, functionDeclaration, lambdaLiteral, nullableType, typeParameter */
