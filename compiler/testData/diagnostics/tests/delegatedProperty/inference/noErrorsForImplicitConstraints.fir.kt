// RUN_PIPELINE_TILL: FRONTEND
package foo

import kotlin.reflect.KProperty

class A {
    var a5: String <!DELEGATE_SPECIAL_FUNCTION_NONE_APPLICABLE!>by<!> MyProperty1()
    var b5: String <!DELEGATE_SPECIAL_FUNCTION_NONE_APPLICABLE!>by<!> getMyProperty1()
}

fun <A, B> getMyProperty1() = MyProperty1<A, B>()

class MyProperty1<T, R> {

    operator fun getValue(thisRef: R, desc: KProperty<*>): T {
        throw Exception()
    }

    operator fun setValue(i: Int, j: Any, k: Int) {
        println("set")
    }
}

// -----------------

class B {
    var a5: String <!DELEGATE_SPECIAL_FUNCTION_NONE_APPLICABLE!>by<!> MyProperty2()
    var b5: String <!DELEGATE_SPECIAL_FUNCTION_NONE_APPLICABLE!>by<!> getMyProperty2()
}

fun <A, B> getMyProperty2() = MyProperty2<A, B>()

class MyProperty2<T, R> {

    operator fun getValue(thisRef: R, desc: KProperty<*>): T {
        throw Exception()
    }

    <!INAPPLICABLE_OPERATOR_MODIFIER!>operator<!> fun setValue(i: Int) {
        println("set")
    }
}

// -----------------
fun println(a: Any?) = a

/* GENERATED_FIR_TAGS: classDeclaration, functionDeclaration, nullableType, operator, propertyDeclaration,
propertyDelegate, setter, starProjection, stringLiteral, typeParameter */
