// IGNORE_FIR_DIAGNOSTICS
// RUN_PIPELINE_TILL: BACKEND
// MODULE: m1-common
// FILE: common.kt
expect class Foo {
    class Nested
}

fun foo(p: Foo.Nested) {}

// MODULE: m2-jvm()()(m1-common)
// FILE: jvm.kt
class FooImpl {
    class Nested
}

actual typealias Foo = FooImpl

fun test() {
    foo(<!ARGUMENT_TYPE_MISMATCH!>FooImpl.Nested()<!>)
}

/* GENERATED_FIR_TAGS: actual, classDeclaration, expect, functionDeclaration, nestedClass, typeAliasDeclaration */
