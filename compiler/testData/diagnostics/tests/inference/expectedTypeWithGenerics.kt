// RUN_PIPELINE_TILL: BACKEND
// LANGUAGE: +ExpectedTypeFromCast

class X<S> {
    fun <T : S> foo(): T = TODO()
}

fun test(x: X<Number>) {
    val y = x.foo() as Int
}

fun <S, D: S> g() {
    fun <T : S> foo(): T = TODO()

    val y = <!TYPE_MISMATCH!>foo<!>() as Int

    val y2 = foo() as D
}

/* GENERATED_FIR_TAGS: asExpression, classDeclaration, functionDeclaration, intersectionType, localFunction,
localProperty, nullableType, propertyDeclaration, typeConstraint, typeParameter */
