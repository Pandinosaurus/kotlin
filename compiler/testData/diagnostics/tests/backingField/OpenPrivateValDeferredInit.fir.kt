// RUN_PIPELINE_TILL: FRONTEND
// LANGUAGE:+ProhibitOpenValDeferredInitialization
open class Foo {
    <!INCOMPATIBLE_MODIFIERS!>private<!> <!INCOMPATIBLE_MODIFIERS!>open<!> val foo: Int

    init {
        foo = 1
    }
}

/* GENERATED_FIR_TAGS: assignment, classDeclaration, init, integerLiteral, propertyDeclaration */
