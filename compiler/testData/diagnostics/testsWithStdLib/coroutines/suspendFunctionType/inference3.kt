// RUN_PIPELINE_TILL: BACKEND
// FIR_IDENTICAL
// DIAGNOSTICS: -UNUSED_PARAMETER

fun <T> withS(x: T, sfn: suspend T.() -> Unit) = x

val test1 = withS(100) {}

fun <TT> test2(x: TT) = withS(x) {}

/* GENERATED_FIR_TAGS: functionDeclaration, functionalType, integerLiteral, lambdaLiteral, nullableType,
propertyDeclaration, suspend, typeParameter, typeWithExtension */
