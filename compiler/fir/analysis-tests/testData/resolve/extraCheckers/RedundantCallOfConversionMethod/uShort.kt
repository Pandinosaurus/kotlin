// RUN_PIPELINE_TILL: BACKEND
// WITH_STDLIB
fun test(i: UShort) {
    val <!UNUSED_VARIABLE!>foo<!> = i.<!REDUNDANT_CALL_OF_CONVERSION_METHOD!>toUShort()<!>
}

/* GENERATED_FIR_TAGS: functionDeclaration, localProperty, propertyDeclaration */
