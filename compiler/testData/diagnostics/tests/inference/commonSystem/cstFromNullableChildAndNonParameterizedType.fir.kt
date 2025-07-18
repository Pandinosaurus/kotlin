// RUN_PIPELINE_TILL: FRONTEND
// DIAGNOSTICS: -UNUSED_PARAMETER

interface Base<out T>

class ParameterizedChild<out R> : Base<R>
class Child : Base<Nothing>

fun <K> elvis(x: K?, y: K): K = TODO()
fun <K> select(x: K, y: K): K = TODO()

fun <V> myRun(f: () -> V): V = f()

fun <S> test1(a: ParameterizedChild<S>?, b: Child): Base<S> = myRun {
    elvis(a, b)
}

fun <S> test2(a: S?, b: S): S = myRun {
    <!RETURN_TYPE_MISMATCH!>select(a, b)<!>
}

/* GENERATED_FIR_TAGS: classDeclaration, functionDeclaration, functionalType, interfaceDeclaration, lambdaLiteral,
nullableType, out, typeParameter */
