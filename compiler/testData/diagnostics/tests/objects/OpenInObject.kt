// RUN_PIPELINE_TILL: BACKEND
// FIR_IDENTICAL
object Obj {
    fun foo() {}

    <!NON_FINAL_MEMBER_IN_OBJECT!>open<!> fun bar() {}

    var x: Int = 0

    <!NON_FINAL_MEMBER_IN_OBJECT!>open<!> var y: Int = 1
}

/* GENERATED_FIR_TAGS: functionDeclaration, integerLiteral, objectDeclaration, propertyDeclaration */
