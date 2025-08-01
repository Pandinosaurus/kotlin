// RUN_PIPELINE_TILL: FRONTEND
// CHECK_TYPE
// DIAGNOSTICS: -UNUSED_EXPRESSION

package test

import checkSubtype

class C {
    companion object {
        fun foo(): String = "companion"
        fun bar() {}
    }

    fun foo(): Int = 0
}

fun test() {
    val r1 = C::foo
    checkSubtype<(C) -> Int>(r1)

    val r2 = test.C::foo
    checkSubtype<(C) -> Int>(r2)

    val r3 = C.Companion::foo
    checkSubtype<() -> String>(r3)

    val r4 = test.C.Companion::foo
    checkSubtype<() -> String>(r4)

    val r5 = (C)::foo
    checkSubtype<() -> String>(<!ARGUMENT_TYPE_MISMATCH!>r5<!>)

    val r6 = (test.C)::foo
    checkSubtype<() -> String>(<!ARGUMENT_TYPE_MISMATCH!>r6<!>)

    val c = C.Companion
    val r7 = c::foo
    checkSubtype<() -> String>(r7)

    C::bar
}

/* GENERATED_FIR_TAGS: callableReference, classDeclaration, companionObject, funWithExtensionReceiver,
functionDeclaration, functionalType, infix, integerLiteral, localProperty, nullableType, objectDeclaration,
propertyDeclaration, stringLiteral, typeParameter, typeWithExtension */
