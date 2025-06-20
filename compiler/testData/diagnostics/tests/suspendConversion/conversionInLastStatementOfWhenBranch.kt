// RUN_PIPELINE_TILL: BACKEND
// FIR_IDENTICAL
// ISSUE: KT-69473

fun test(b: Boolean) {
    val f: (suspend (String) -> String)? =
        if (b) {
            { s: String -> s }
        } else {
            null
        }
}

/* GENERATED_FIR_TAGS: functionDeclaration, functionalType, ifExpression, lambdaLiteral, localProperty, nullableType,
propertyDeclaration, suspend */
