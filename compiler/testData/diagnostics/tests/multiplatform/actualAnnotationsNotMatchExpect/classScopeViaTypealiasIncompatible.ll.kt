// IGNORE_FIR_DIAGNOSTICS
// RUN_PIPELINE_TILL: FIR2IR
// MODULE: m1-common
// FILE: common.kt
annotation class Ann

expect class WeakIncompatibility {
    @Ann
    fun foo(p: String)
}

expect class StrongIncompatibility {
    @Ann
    fun foo(p: Int)
}

// MODULE: m1-jvm()()(m1-common)
// FILE: jvm.kt
class WeakIncompatibilityImpl {
    fun foo(differentName: String) {}
}

actual typealias <!ACTUAL_ANNOTATIONS_NOT_MATCH_EXPECT, EXPECT_ACTUAL_INCOMPATIBLE_CLASS_SCOPE!>WeakIncompatibility<!> = WeakIncompatibilityImpl

class StrongIncompatibilityImpl {
    fun foo(p: String) {} // Different param type
}

actual typealias <!ACTUAL_ANNOTATIONS_NOT_MATCH_EXPECT, NO_ACTUAL_CLASS_MEMBER_FOR_EXPECTED_CLASS!>StrongIncompatibility<!> = StrongIncompatibilityImpl

/* GENERATED_FIR_TAGS: actual, annotationDeclaration, classDeclaration, expect, functionDeclaration,
typeAliasDeclaration */
