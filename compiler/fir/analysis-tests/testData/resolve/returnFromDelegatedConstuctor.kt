// RUN_PIPELINE_TILL: FRONTEND
// ISSUE: KT-24503
data class StringPair(val a: String, val b: String) {
    constructor() : this(<!RETURN_NOT_ALLOWED!>return<!>, <!RETURN_NOT_ALLOWED!>return<!>)
}

abstract class Abs(val a: String)

class Smth : Abs {
    constructor() : super(<!RETURN_NOT_ALLOWED!>return<!>)
}

/* GENERATED_FIR_TAGS: classDeclaration, data, primaryConstructor, propertyDeclaration, secondaryConstructor */
