// RUN_PIPELINE_TILL: FRONTEND
// FIR_IDENTICAL
val x = object {
    val <!REDECLARATION!>y<!> = 1
    val <!REDECLARATION!>y<!> = 2
}

/* GENERATED_FIR_TAGS: anonymousObjectExpression, integerLiteral, propertyDeclaration */
