// RUN_PIPELINE_TILL: BACKEND
// FIR_IDENTICAL
open class Outer<T> {
    class Nested<U> : Outer<U>() {
        fun bar(): U = foo()
        fun baz(): U = super.foo()
    }
    class Nested2 : Outer<String>() {
        fun bar(): String = foo()
        fun baz(): String = super.foo()
    }
    
    fun foo(): T = null!!
}

/* GENERATED_FIR_TAGS: checkNotNullCall, classDeclaration, functionDeclaration, nestedClass, nullableType,
superExpression, typeParameter */
