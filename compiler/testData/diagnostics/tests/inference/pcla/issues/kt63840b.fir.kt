// RUN_PIPELINE_TILL: BACKEND
// ISSUE: KT-63840
// CHECK_TYPE_WITH_EXACT

fun test() {
    val buildee = build {
        if (true)
            replaceTypeVariable(TargetType())
        else
            DifferentType()
    }
    // exact type equality check — turns unexpected compile-time behavior into red code
    // considered to be non-user-reproducible code for the purposes of these tests
    checkExactType<Buildee<TargetType>>(buildee)
}




class TargetType
class DifferentType

class Buildee<TV> {
    fun replaceTypeVariable(value: TV): TV { val temp = storage; storage = value; return temp }
    private var storage: TV = null!!
}

fun <PTV> build(instructions: Buildee<PTV>.() -> Unit): Buildee<PTV> {
    return Buildee<PTV>().apply(instructions)
}

/* GENERATED_FIR_TAGS: assignment, checkNotNullCall, classDeclaration, functionDeclaration, functionalType, ifExpression,
lambdaLiteral, localProperty, nullableType, propertyDeclaration, stringLiteral, typeParameter, typeWithExtension */
