// RUN_PIPELINE_TILL: FRONTEND
// FIR_IDENTICAL
// CHECK_TYPE

import kotlin.reflect.KFunction0

fun main() {
    class A
    
    fun A.foo() {
        val x = ::A
        checkSubtype<KFunction0<A>>(x)
    }
    
    fun Int.foo() {
        val x = ::A
        checkSubtype<KFunction0<A>>(x)
    }
}

/* GENERATED_FIR_TAGS: callableReference, classDeclaration, funWithExtensionReceiver, functionDeclaration,
functionalType, infix, localClass, localFunction, localProperty, nullableType, propertyDeclaration, typeParameter,
typeWithExtension */
