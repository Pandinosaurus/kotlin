// RUN_PIPELINE_TILL: FRONTEND
fun <E : String?, T : ((CharSequence) -> Unit)?> foo(x: E, y: T) {
    if (x != null) {
        <!UNSAFE_IMPLICIT_INVOKE_CALL!>y<!>(<!DEBUG_INFO_SMARTCAST!>x<!>)
    }

    if (y != null) {
        <!DEBUG_INFO_SMARTCAST!>y<!>(<!TYPE_MISMATCH!>x<!>)
    }

    if (x != null && y != null) {
        <!DEBUG_INFO_SMARTCAST!>y<!>(<!DEBUG_INFO_SMARTCAST!>x<!>)
    }
}

/* GENERATED_FIR_TAGS: andExpression, dnnType, equalityExpression, functionDeclaration, functionalType, ifExpression,
nullableType, smartcast, typeConstraint, typeParameter */
