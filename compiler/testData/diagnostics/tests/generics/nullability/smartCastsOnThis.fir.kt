// RUN_PIPELINE_TILL: FRONTEND
// DIAGNOSTICS: -UNUSED_EXPRESSION,-UNUSED_VARIABLE

fun <T : CharSequence?> T.bar1() {}
fun CharSequence?.bar2() {}

fun <T : CharSequence> T.bar3() {}
fun CharSequence.bar4() {}

fun <T : String?> T.foo() {
    if (this != null) {
        if (<!SENSELESS_COMPARISON!>this != null<!>) {}

        length
        this<!UNNECESSARY_SAFE_CALL!>?.<!>length

        bar1()
        bar2()
        bar3()
        bar4()


        this<!UNNECESSARY_SAFE_CALL!>?.<!>bar1()
    }

    <!UNSAFE_CALL!>length<!>

    if (this is String) {
        length
        this<!UNNECESSARY_SAFE_CALL!>?.<!>length

        bar1()
        bar2()
        bar3()
    }
}

/* GENERATED_FIR_TAGS: dnnType, equalityExpression, funWithExtensionReceiver, functionDeclaration, ifExpression,
isExpression, nullableType, safeCall, smartcast, thisExpression, typeConstraint, typeParameter */
