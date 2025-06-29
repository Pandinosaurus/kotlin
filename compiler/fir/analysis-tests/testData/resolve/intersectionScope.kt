// RUN_PIPELINE_TILL: BACKEND
interface A {
    fun foo()
}

abstract class B : A {
    override fun foo() {}
}

interface C : A {}

fun main(c: C) {
    if (c !is B) return
    c.foo()
}

/* GENERATED_FIR_TAGS: classDeclaration, functionDeclaration, ifExpression, interfaceDeclaration, intersectionType,
isExpression, override, smartcast */
