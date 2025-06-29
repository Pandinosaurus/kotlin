// RUN_PIPELINE_TILL: BACKEND
fun fibIterative(n: Int): Int {
    if (n < 2) return 1
    var current = 1
    var prev = 1
    for (i in 2..n) {
        val temp = current
        current += prev
        prev = temp
    }
    return current
}

/* GENERATED_FIR_TAGS: additiveExpression, assignment, comparisonExpression, forLoop, functionDeclaration, ifExpression,
integerLiteral, localProperty, propertyDeclaration, rangeExpression */
