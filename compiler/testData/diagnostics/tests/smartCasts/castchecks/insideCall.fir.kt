// RUN_PIPELINE_TILL: BACKEND
// LANGUAGE: +SafeCastCheckBoundSmartCasts
// See KT-19007

// Stub
fun String.indexOf(arg: String) = this.length - arg.length

// Stub
fun String.toLowerCase() = this

fun foo(a: Any) {
    // Should compile in 1.2
    (a as? String)?.indexOf(a.toLowerCase())
}

/* GENERATED_FIR_TAGS: additiveExpression, funWithExtensionReceiver, functionDeclaration, nullableType, safeCall,
smartcast, thisExpression */
