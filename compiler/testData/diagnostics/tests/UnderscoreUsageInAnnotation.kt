// RUN_PIPELINE_TILL: FRONTEND
// FIR_IDENTICAL
package test

annotation class `__`(val value: String)

@<!UNDERSCORE_USAGE_WITHOUT_BACKTICKS!>__<!>("") class TestAnn
@`__`("") class TestAnn2

/* GENERATED_FIR_TAGS: annotationDeclaration, classDeclaration, primaryConstructor, propertyDeclaration, stringLiteral */
