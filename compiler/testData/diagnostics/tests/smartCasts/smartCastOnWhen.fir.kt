// RUN_PIPELINE_TILL: FRONTEND
// Smart casts on complex expressions
fun baz(s: String?): Int {
    if (s == null) return 0
    return when(s) {
        "abc" -> s
        else -> "xyz"
    }.length
}

var ss: String? = null

fun bar(): Int {
    if (ss == null) return 0
    // ss cannot be smart casted, so an error here
    return when(ss) {
        "abc" -> ss
        else -> "xyz"
    }<!UNSAFE_CALL!>.<!>length
}

/* GENERATED_FIR_TAGS: equalityExpression, functionDeclaration, ifExpression, integerLiteral, nullableType,
propertyDeclaration, smartcast, stringLiteral, whenExpression, whenWithSubject */
