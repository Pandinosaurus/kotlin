// RUN_PIPELINE_TILL: BACKEND
// FIR_IDENTICAL
// DIAGNOSTICS: -UNUSED_PARAMETER

import kotlin.reflect.KProperty1

class Sample(val str: String)

class Inv<T>

class Form {
    operator fun <F> get(field: KProperty1<*, F>): Inv<F> = TODO()
}

fun <K> foo(i: Inv<K>) {}

fun test(f: Form) {
    foo(f[Sample::str])
}

/* GENERATED_FIR_TAGS: callableReference, classDeclaration, functionDeclaration, nullableType, operator,
primaryConstructor, propertyDeclaration, starProjection, typeParameter */
