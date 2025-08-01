// RUN_PIPELINE_TILL: FRONTEND
// DIAGNOSTICS: -INVISIBLE_MEMBER -INVISIBLE_REFERENCE

// FILE: b.kt
<!JVM_PACKAGE_NAME_CANNOT_BE_EMPTY!>@file:JvmPackageName("")<!>
package b
fun b() {}

// FILE: c.kt
<!JVM_PACKAGE_NAME_MUST_BE_VALID_NAME!>@file:JvmPackageName("invalid-fq-name")<!>
package c
fun c() {}

// FILE: d.kt
<!JVM_PACKAGE_NAME_NOT_SUPPORTED_IN_FILES_WITH_CLASSES!>@file:JvmPackageName("d")<!>
package d
class D
fun d() {}

// FILE: e.kt
@file:JvmPackageName(42)
package e
fun e() {}

// FILE: f.kt
@file:JvmPackageName(<!EXPRESSION_EXPECTED_PACKAGE_FOUND!>f<!>)
package f
const val name = "f"

/* GENERATED_FIR_TAGS: annotationUseSiteTargetFile, classDeclaration, const, functionDeclaration, integerLiteral,
propertyDeclaration, stringLiteral */
