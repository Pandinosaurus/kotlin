// RUN_PIPELINE_TILL: FRONTEND
// FIR_IDENTICAL
package checkFiles

import java.util.HashMap

fun main() {
    val hashMap = HashMap<String, String>()
    hashMap[<!SYNTAX!><!>]
}

/* GENERATED_FIR_TAGS: flexibleType, functionDeclaration, javaFunction, localProperty, nullableType, propertyDeclaration */
