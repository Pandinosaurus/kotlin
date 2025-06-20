// DISABLE_JAVA_FACADE
// RUN_PIPELINE_TILL: BACKEND
// FIR_IDENTICAL
// DIAGNOSTICS: -UNUSED_VARIABLE
// FILE: A.java

import java.util.List;

public interface A<T> {
    boolean value(T t);
}

// FILE: B.java

class B {
    void foo(Runnable runnable, A x);

    static A bar() {}
}

// FILE: main.kt

fun main() {
    fun println() {}
    // All parameters in SAM adapter of `foo` have functional types
    B().foo({ println() }, B.bar())
    // So you should use SAM constructors when you want to use mix lambdas and Java objects
    B().foo(Runnable { println() }, B.bar())
    B().foo({ println() }, { it: Any? -> it == null } )
}

/* GENERATED_FIR_TAGS: equalityExpression, flexibleType, functionDeclaration, javaFunction, javaType, lambdaLiteral,
localFunction, nullableType, samConversion */
