// RUN_PIPELINE_TILL: FRONTEND
// CHECK_TYPE

fun foo(s : String?, b : Boolean) {
    if (s == null) return

    val s1 = if (b) "" else <!DEBUG_INFO_SMARTCAST!>s<!>
    s1 checkType { _<String>() }

    val s2 = s
    <!DEBUG_INFO_SMARTCAST!>s2<!> checkType { _<String>() }
}

/* GENERATED_FIR_TAGS: classDeclaration, equalityExpression, funWithExtensionReceiver, functionDeclaration,
functionalType, ifExpression, infix, lambdaLiteral, localProperty, nullableType, propertyDeclaration, smartcast,
stringLiteral, typeParameter, typeWithExtension */
