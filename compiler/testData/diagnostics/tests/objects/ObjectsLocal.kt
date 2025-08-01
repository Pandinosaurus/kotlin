// RUN_PIPELINE_TILL: FRONTEND
// NI_EXPECTED_FILE

package localObjects

object A {
    val x : Int = 0
}

open class Foo {
    fun foo() : Int = 1
}

fun test() {
    A.x
    val b = object : Foo() {
    }
    b.foo()

    <!LOCAL_OBJECT_NOT_ALLOWED!>object B<!> {
        fun foo() {}
    }
    B.foo()
}

val bb = <!UNRESOLVED_REFERENCE!>B<!>.<!DEBUG_INFO_MISSING_UNRESOLVED!>foo<!>()

/* GENERATED_FIR_TAGS: anonymousObjectExpression, classDeclaration, functionDeclaration, integerLiteral, localClass,
localProperty, objectDeclaration, propertyDeclaration */
