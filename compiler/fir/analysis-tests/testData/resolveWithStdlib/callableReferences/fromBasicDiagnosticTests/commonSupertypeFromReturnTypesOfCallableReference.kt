// RUN_PIPELINE_TILL: BACKEND
interface Parent
interface Child1 : Parent
interface Child2 : Parent

fun foo(): Child1 = TODO()
fun bar(): Child2 = TODO()

fun <K> select(x: K, y: K): K = TODO()

fun test() {
    val a = select(::foo, ::bar)
}

/* GENERATED_FIR_TAGS: callableReference, functionDeclaration, interfaceDeclaration, localProperty, nullableType,
propertyDeclaration, typeParameter */
