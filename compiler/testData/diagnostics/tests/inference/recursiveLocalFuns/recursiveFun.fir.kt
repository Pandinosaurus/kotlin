// RUN_PIPELINE_TILL: FRONTEND
fun foo() {
    fun bar() = (fun() = <!TYPECHECKER_HAS_RUN_INTO_RECURSIVE_PROBLEM!>bar()<!>)
}

/* GENERATED_FIR_TAGS: anonymousFunction, functionDeclaration, localFunction */
