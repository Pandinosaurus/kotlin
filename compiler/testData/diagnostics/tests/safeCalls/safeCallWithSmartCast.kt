// RUN_PIPELINE_TILL: FRONTEND
// WITH_STDLIB

class C {
    var x: Int = 0
}

fun test(с: C?, a: Any) {
    с?.x = if (a is String) 0 else throw Exception();
    <!DEBUG_INFO_SMARTCAST!>a<!>.uppercase()
}


fun main(args: Array<String>) {
    test(null, 1)
}

/* GENERATED_FIR_TAGS: assignment, classDeclaration, functionDeclaration, ifExpression, integerLiteral, isExpression,
nullableType, propertyDeclaration, safeCall */
