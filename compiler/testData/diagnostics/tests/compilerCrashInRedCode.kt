// RUN_PIPELINE_TILL: FRONTEND
// FIR_IDENTICAL
fun foo(x: Int.() -> Unit) { }

fun test(){
    foo(Int.<!ILLEGAL_SELECTOR!>{}<!>)
}

/* GENERATED_FIR_TAGS: functionDeclaration, functionalType, lambdaLiteral, typeWithExtension */
