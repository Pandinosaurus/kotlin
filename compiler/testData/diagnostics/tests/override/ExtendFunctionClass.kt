// RUN_PIPELINE_TILL: FRONTEND
// FIR_IDENTICAL
package extendFunctionClass

<!ABSTRACT_MEMBER_NOT_IMPLEMENTED!>class A<!> : Function1<Int, Int> {

}

class B : Function1<Int, Int> {
    override fun invoke(p1 : Int) = p1
}

/* GENERATED_FIR_TAGS: classDeclaration, functionDeclaration, operator, override */
