// RUN_PIPELINE_TILL: BACKEND
// FIR_IDENTICAL
interface A<R>

interface B {
    fun accept(visitor: String)
    fun <R> accept(visitor: A<R>): R
}

interface C : B {
    override fun <R> accept(visitor: A<R>): R
}

/* GENERATED_FIR_TAGS: functionDeclaration, interfaceDeclaration, nullableType, override, typeParameter */
