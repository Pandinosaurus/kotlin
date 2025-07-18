// RUN_PIPELINE_TILL: FRONTEND
// FIR_IDENTICAL
interface A {
    fun <T> foo() where T : Any, T : Cloneable?
}

interface B : A {
    <!NOTHING_TO_OVERRIDE!>override<!> fun <T> foo() where T : Any?, T : Cloneable
}

/* GENERATED_FIR_TAGS: functionDeclaration, interfaceDeclaration, nullableType, override, typeConstraint, typeParameter */
