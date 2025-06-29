// RUN_PIPELINE_TILL: BACKEND
// FIR_IDENTICAL
// FULL_JDK

import java.util.stream.*

interface A : Collection<String> {
    override fun stream(): Stream<String> = Stream.of()
}

fun foo(x: List<String>, y: A) {
    x.stream().filter { it.length > 0 }.collect(Collectors.toList())
    y.stream().filter { it.length > 0 }
}

/* GENERATED_FIR_TAGS: comparisonExpression, flexibleType, functionDeclaration, inProjection, integerLiteral,
interfaceDeclaration, javaFunction, lambdaLiteral, override, samConversion, starProjection */
