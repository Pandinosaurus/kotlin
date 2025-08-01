// RUN_PIPELINE_TILL: BACKEND
// ISSUE: KT-25668
suspend fun SequenceScope<Int>.bar() = yield(1)

fun test() {
    val seq = sequence {
        val f: suspend SequenceScope<Int>.() -> Unit = SequenceScope<Int>::<!ILLEGAL_RESTRICTED_SUSPENDING_FUNCTION_CALL!>bar<!>
        f()
    }
    seq.toList()
}

/* GENERATED_FIR_TAGS: callableReference, funWithExtensionReceiver, functionDeclaration, functionalType, integerLiteral,
lambdaLiteral, localProperty, propertyDeclaration, suspend, typeWithExtension */
