// RUN_PIPELINE_TILL: BACKEND
// FIR_IDENTICAL
// DIAGNOSTICS: -UNUSED_PARAMETER
// FILE: p/J.java

package p;

public class J {
    public J j() { return this; }
}

// FILE: k.kt

import p.*

fun takeJ(j: J) {}

fun test() {
    takeJ(J().j())
}

/* GENERATED_FIR_TAGS: flexibleType, functionDeclaration, javaFunction, javaType */
