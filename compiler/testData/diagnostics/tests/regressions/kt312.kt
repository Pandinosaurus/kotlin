// RUN_PIPELINE_TILL: FRONTEND
// KT-312 Nullability problem when a nullable version of a generic type is returned

fun <T> Array<out T>.safeGet(index : Int) : T? {
    return if (index < size) this[index] else null
}

val args : Array<String> = Array<String>(1, {""})
val name : String = <!TYPE_MISMATCH!>args.<!TYPE_MISMATCH!>safeGet<String>(0)<!><!> // No error, must be type mismatch
val name1 : String? = args.safeGet(0)

/* GENERATED_FIR_TAGS: capturedType, comparisonExpression, funWithExtensionReceiver, functionDeclaration, ifExpression,
integerLiteral, lambdaLiteral, nullableType, outProjection, propertyDeclaration, stringLiteral, thisExpression,
typeParameter */
