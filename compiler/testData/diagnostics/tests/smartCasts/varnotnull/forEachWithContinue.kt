// RUN_PIPELINE_TILL: FRONTEND
data class SomeObject(val n: SomeObject?) {
    fun doSomething() {}
    fun next(): SomeObject? = n    
}


fun list(start: SomeObject): SomeObject {
    var e: SomeObject? = start
    for (i in 0..42) {
        if (e == null)
            continue
        // Smart casts are possible because of the continue before
        <!DEBUG_INFO_SMARTCAST!>e<!>.doSomething()
        e = <!DEBUG_INFO_SMARTCAST!>e<!>.next()
    }
    return <!TYPE_MISMATCH!>e<!>
}

/* GENERATED_FIR_TAGS: assignment, classDeclaration, continue, data, equalityExpression, forLoop, functionDeclaration,
ifExpression, integerLiteral, localProperty, nullableType, primaryConstructor, propertyDeclaration, rangeExpression,
smartcast */
