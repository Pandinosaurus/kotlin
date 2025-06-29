// RUN_PIPELINE_TILL: BACKEND
// FIR_IDENTICAL
package a

//KT-2240 Wrong overload resolution ambiguity when object literal is involved

class A {}

fun <T> A.foo(f : T) {}

val o = object {
    fun <T> foo(f: T) {
        A().foo(f) // Ambiguity here!
    }
}

/* GENERATED_FIR_TAGS: anonymousObjectExpression, classDeclaration, funWithExtensionReceiver, functionDeclaration,
nullableType, propertyDeclaration, typeParameter */
