// RUN_PIPELINE_TILL: BACKEND
// DIAGNOSTICS: -UNUSED_PARAMETER

@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
inline fun <reified @kotlin.internal.PureReifiable T> foo(x: T) {}

fun test() {
    foo<List<String>>(listOf(""))
    foo(listOf(""))

    foo<Array<String>>(arrayOf(""))
    foo(arrayOf(""))
}

/* GENERATED_FIR_TAGS: functionDeclaration, inline, nullableType, reified, stringLiteral, typeParameter */
