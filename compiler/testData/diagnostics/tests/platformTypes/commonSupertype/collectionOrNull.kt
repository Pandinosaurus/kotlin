// RUN_PIPELINE_TILL: BACKEND
// FIR_IDENTICAL
// FILE: p/Utils.java

package p;

public class Utils {
    public static java.util.Collection<String> c() { return null; }
}

// FILE: k.kt

import p.*

fun <T : Any> T.foo() {}

fun <D> test(b: Boolean) {
    val c = if (b) Utils.c() else null

    c?.foo()
}

/* GENERATED_FIR_TAGS: flexibleType, funWithExtensionReceiver, functionDeclaration, ifExpression, javaFunction,
localProperty, nullableType, propertyDeclaration, safeCall, typeConstraint, typeParameter */
