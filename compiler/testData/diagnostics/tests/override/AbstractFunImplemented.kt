// RUN_PIPELINE_TILL: BACKEND
// FIR_IDENTICAL
abstract class A {
    abstract fun foo(): Int
}

class B() : A() {
    override fun foo() = 1
}

/* GENERATED_FIR_TAGS: classDeclaration, functionDeclaration, integerLiteral, override, primaryConstructor */
