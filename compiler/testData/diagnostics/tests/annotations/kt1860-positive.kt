// RUN_PIPELINE_TILL: FRONTEND
// COMPARE_WITH_LIGHT_TREE
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.EXPRESSION)
@Retention(AnnotationRetention.SOURCE)
annotation class test

fun foo(@test f : Int) {}

var bar : Int = 1
    set(@test v) {}

val x : (Int) -> Int = {@test <!TYPE_MISMATCH, UNINITIALIZED_VARIABLE!>x<!> <!SYNTAX!>: Int -> x<!>} // todo fix parser annotation on lambda parameter

class Hello(@test args: Any) {
}

/* GENERATED_FIR_TAGS: annotationDeclaration, classDeclaration, functionDeclaration, functionalType, integerLiteral,
lambdaLiteral, primaryConstructor, propertyDeclaration, setter */
