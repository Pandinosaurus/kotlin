// RUN_PIPELINE_TILL: FRONTEND
// FIR_IDENTICAL
// SKIP_JAVAC
// LANGUAGE: +InlineClasses
// ALLOW_KOTLIN_PACKAGE
// DIAGNOSTICS: -UNUSED_PARAMETER, -UNUSED_VARIABLE, -UNUSED_ANONYMOUS_PARAMETER

package kotlin.jvm

annotation class JvmInline

@JvmInline
value class Foo(val x: Int)

fun f1(<!FORBIDDEN_VARARG_PARAMETER_TYPE!>vararg<!> a: Foo) {}
fun f2(<!FORBIDDEN_VARARG_PARAMETER_TYPE!>vararg<!> a: Foo?) {}

class A {
    fun f3(a0: Int, <!FORBIDDEN_VARARG_PARAMETER_TYPE!>vararg<!> a1: Foo) {
        fun f4(<!FORBIDDEN_VARARG_PARAMETER_TYPE!>vararg<!> a: Foo) {}

        val g = fun (<!USELESS_VARARG_ON_PARAMETER!><!FORBIDDEN_VARARG_PARAMETER_TYPE!>vararg<!> v: Foo<!>) {}
        fun (<!USELESS_VARARG_ON_PARAMETER!><!FORBIDDEN_VARARG_PARAMETER_TYPE!>vararg<!> v: Foo<!>) {}
    }
}

class B(<!FORBIDDEN_VARARG_PARAMETER_TYPE!>vararg<!> val s: Foo) {
    constructor(a: Int, <!FORBIDDEN_VARARG_PARAMETER_TYPE!>vararg<!> s: Foo) : this(*s)
}

annotation class Ann(<!FORBIDDEN_VARARG_PARAMETER_TYPE!>vararg<!> val f: <!INVALID_TYPE_OF_ANNOTATION_MEMBER!>Foo<!>)

/* GENERATED_FIR_TAGS: annotationDeclaration, anonymousFunction, classDeclaration, functionDeclaration, localFunction,
localProperty, outProjection, primaryConstructor, propertyDeclaration, secondaryConstructor, value, vararg */
