// RUN_PIPELINE_TILL: BACKEND
// FIR_IDENTICAL
// DIAGNOSTICS: -UNUSED_PARAMETER

inline fun<T1, reified T2> bar(x: T1, y: T2): T2 = y
inline fun<reified R> foo(z: R): R = bar(1, z)

fun box() {
    foo("abc")
}

/* GENERATED_FIR_TAGS: functionDeclaration, inline, integerLiteral, nullableType, reified, stringLiteral, typeParameter */
