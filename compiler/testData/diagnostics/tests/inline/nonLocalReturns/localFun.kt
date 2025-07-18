// RUN_PIPELINE_TILL: FRONTEND
// FIR_IDENTICAL
// DIAGNOSTICS: -UNUSED_EXPRESSION -UNUSED_PARAMETER -UNUSED_VARIABLE -NOTHING_TO_INLINE

inline fun <R> inlineFunOnlyLocal(crossinline p: () -> R) {
    <!NOT_YET_SUPPORTED_IN_INLINE!>fun<!> a() {
        val z = p()
    }
    a()
}

inline fun <R> inlineFun(p: () -> R) {
    <!NOT_YET_SUPPORTED_IN_INLINE!>fun<!> a() {
        <!NON_LOCAL_RETURN_NOT_ALLOWED!>p<!>()
    }
    a()
}

/* GENERATED_FIR_TAGS: crossinline, functionDeclaration, functionalType, inline, localFunction, localProperty,
nullableType, propertyDeclaration, typeParameter */
