// RUN_PIPELINE_TILL: FRONTEND
// DIAGNOSTICS: -UNUSED_VARIABLE, -UNUSED_PARAMETER

fun foo(i: Int) {}
fun foo(s: String) {}
fun <T> id(x: T): T = x
fun <T> baz(x: T, y: T): T = TODO()

fun test() {
    val x1: (Int) -> Unit = id(id(::foo))
    val x2: (Int) -> Unit = baz(id(::foo), ::foo)
    val x3: (Int) -> Unit = baz(id(::foo), id(id(::foo)))
    val x4: (String) -> Unit = baz(id(::foo), id(id(::foo)))
    val x5: (Double) -> Unit = baz(id(::<!CALLABLE_REFERENCE_RESOLUTION_AMBIGUITY!>foo<!>), id(id(::<!CALLABLE_REFERENCE_RESOLUTION_AMBIGUITY!>foo<!>)))


    id<(Int) -> Unit>(id(id(::foo)))
    id(id<(Int) -> Unit>(::foo))
    baz<(Int) -> Unit>(id(::foo), id(id(::foo)))
    baz(id(::foo), id(id<(Int) -> Unit>(::foo)))
    baz(id(::foo), id<(Int) -> Unit>(id(::foo)))
}

/* GENERATED_FIR_TAGS: callableReference, functionDeclaration, functionalType, localProperty, nullableType,
propertyDeclaration, typeParameter */
