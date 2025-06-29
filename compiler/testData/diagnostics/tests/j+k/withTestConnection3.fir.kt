// RUN_PIPELINE_TILL: BACKEND
// FULL_JDK
// JVM_TARGET: 1.8

// FILE: test.kt

fun waitTestConnection(res: B<*>) {
    C.wait(res.toFuture()).length
}

// FILE: B.kt

import java.util.concurrent.CompletableFuture

abstract class B<S : CharSequence?> {
    abstract fun toFuture(): CompletableFuture<S>
}

// FILE: C.java

import java.util.concurrent.Future;

public class C {
    public static <F> F wait(Future<F> future) {
        return null;
    }
}

/* GENERATED_FIR_TAGS: capturedType, classDeclaration, flexibleType, functionDeclaration, javaFunction, nullableType,
starProjection, typeConstraint, typeParameter */
