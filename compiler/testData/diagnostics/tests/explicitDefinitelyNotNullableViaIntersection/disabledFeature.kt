// RUN_PIPELINE_TILL: BACKEND
// LANGUAGE: -DefinitelyNonNullableTypes

fun <T> foo(x: T, y: <!UNSUPPORTED_FEATURE!>T & Any<!>): List<<!UNSUPPORTED_FEATURE!>T & Any<!>>? = null

/* GENERATED_FIR_TAGS: dnnType, functionDeclaration, nullableType, typeParameter */
