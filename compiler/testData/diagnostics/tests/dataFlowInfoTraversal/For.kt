// RUN_PIPELINE_TILL: FRONTEND
fun bar(x: Int): Int = x + 1

fun foo() {
    val x: Int? = null
    val a = Array<Int>(3, {0})

    for (p in a) {
        bar(<!TYPE_MISMATCH!>x<!>)
        if (x == null) continue
        bar(<!DEBUG_INFO_SMARTCAST!>x<!>)
        for (q in a) {
            bar(<!DEBUG_INFO_SMARTCAST!>x<!>)
            if (<!SENSELESS_COMPARISON!>x == null<!>) bar(<!DEBUG_INFO_SMARTCAST!>x<!>)
        }
    }

    for (p in a) {
        bar(<!TYPE_MISMATCH!>x<!>)
        if (x == null) break
        bar(<!DEBUG_INFO_SMARTCAST!>x<!>)
    }
}

/* GENERATED_FIR_TAGS: additiveExpression, break, continue, equalityExpression, forLoop, functionDeclaration,
ifExpression, integerLiteral, lambdaLiteral, localProperty, nullableType, propertyDeclaration, smartcast */
