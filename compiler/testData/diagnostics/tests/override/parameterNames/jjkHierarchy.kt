// RUN_PIPELINE_TILL: BACKEND
// FIR_IDENTICAL
// FILE: Super.java

interface Super {
    void foo(long superName);
}

// FILE: Sub.java

interface Sub extends Super {
}

// FILE: SubSub.kt

class SubSub : Sub {
    override fun foo(subName: Long) {}
}

/* GENERATED_FIR_TAGS: classDeclaration, functionDeclaration, javaType, override */
