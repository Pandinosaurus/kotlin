// RUN_PIPELINE_TILL: BACKEND
// FIR_IDENTICAL
// FILE: B.java

public abstract class B implements A {
    private int size = 1;
}

// FILE: main.kt

interface A {
    val size: Int
}

class C : B() {
    override val size: Int get() = 1
}

fun foo() {
    C().size 
}

/* GENERATED_FIR_TAGS: classDeclaration, functionDeclaration, getter, integerLiteral, interfaceDeclaration, javaType,
override, propertyDeclaration */
