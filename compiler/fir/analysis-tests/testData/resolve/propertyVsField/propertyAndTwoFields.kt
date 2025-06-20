// RUN_PIPELINE_TILL: BACKEND
// FILE: Test.java

public class Test extends KotlinBase {
    public final String abcd = "ABCD";
}

// FILE: KotlinBase.kt

open class KotlinBase {
    val abcd = "abcd"
}

// FILE: JavaDerived.java

public class JavaDerived extends Test {
    public final String abcd = "AaBbCcDd";
}

// FILE: test.kt

fun test(d: JavaDerived) {
    d.abcd // JavaDerived
}

/* GENERATED_FIR_TAGS: classDeclaration, flexibleType, functionDeclaration, javaProperty, javaType, propertyDeclaration,
stringLiteral */
