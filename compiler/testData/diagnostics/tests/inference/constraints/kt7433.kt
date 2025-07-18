// RUN_PIPELINE_TILL: BACKEND
// FIR_IDENTICAL
// DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE

public inline fun <S, T: S> Iterable<T>.reduce1(operation: (S, T) -> S): S = throw Exception()

fun test(ints: List<Int>) {
    val f: () -> Unit = {
        ints.reduce1 { a, b -> a + b }
    }
}

/* GENERATED_FIR_TAGS: additiveExpression, funWithExtensionReceiver, functionDeclaration, functionalType, inline,
lambdaLiteral, localProperty, nullableType, propertyDeclaration, typeConstraint, typeParameter */
