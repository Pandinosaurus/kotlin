// RUN_PIPELINE_TILL: FRONTEND
// FIR_IDENTICAL
// CHECK_TYPE
// DIAGNOSTICS: -UNUSED_VARIABLE

import java.util.ArrayList

class F<T> {
    fun <T, V> x (y: F<ArrayList<T>>, w: ArrayList<V>) {
        val z: ArrayList<T> = y["", w]
    }
}
operator fun <V, T> Any.get(s: String, w: ArrayList<V>): ArrayList<T> = throw Exception()

/* GENERATED_FIR_TAGS: classDeclaration, funWithExtensionReceiver, functionDeclaration, functionalType, infix,
localProperty, nullableType, operator, propertyDeclaration, stringLiteral, typeParameter, typeWithExtension */
