// RUN_PIPELINE_TILL: BACKEND
// FIR_IDENTICAL
//KT-2856 Fix the getOrElse signature to be able to return any supertype of V
package d

import java.util.HashMap

public inline fun <K,V1, V: V1> Map<K,V>.getOrElse1(key: K, defaultValue: ()-> V1) : V1 {
    if (this.containsKey(key)) {
        return this.get(key) <!UNCHECKED_CAST!>as V<!>
    } else {
        return defaultValue()
    }
}

fun main() {
    val map = HashMap<Int, Int>()
    println(map.getOrElse1(2, { null })) // Error
}

//from standard library
fun println(message : Any?) {}

/* GENERATED_FIR_TAGS: asExpression, flexibleType, funWithExtensionReceiver, functionDeclaration, functionalType,
ifExpression, inline, integerLiteral, javaFunction, lambdaLiteral, localProperty, nullableType, propertyDeclaration,
thisExpression, typeConstraint, typeParameter */
