// RUN_PIPELINE_TILL: BACKEND
operator fun String.invoke() = this

val some = ""
fun sss() {
    val some = 10

    // Should be resolved to top-level some,
    // because with local some invoke isn't applicable
    some()
}

/* GENERATED_FIR_TAGS: funWithExtensionReceiver, functionDeclaration, integerLiteral, localProperty, operator,
propertyDeclaration, stringLiteral, thisExpression */
