// RUN_PIPELINE_TILL: BACKEND
// FIR_IDENTICAL
// DIAGNOSTICS: -UNUSED_PARAMETER

<!SYNCHRONIZED_ON_INLINE!>@Synchronized<!>
inline fun foo(f: () -> Unit): Unit = f()

var bar: String
    <!SYNCHRONIZED_ON_INLINE!>@Synchronized<!>
    inline get() = ""
    <!SYNCHRONIZED_ON_INLINE!>@Synchronized<!>
    inline set(value) {}

inline var baz: String
    <!SYNCHRONIZED_ON_INLINE!>@Synchronized<!>
    get() = ""
    <!SYNCHRONIZED_ON_INLINE!>@Synchronized<!>
    set(value) {}

/* GENERATED_FIR_TAGS: functionDeclaration, functionalType, getter, inline, propertyDeclaration, setter, stringLiteral */
