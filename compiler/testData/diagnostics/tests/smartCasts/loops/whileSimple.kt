// RUN_PIPELINE_TILL: FRONTEND
// FIR_IDENTICAL
fun x(): Boolean { return true }

public fun foo(p: String?): Int {    
    while(x()) {
        p!!.length
        if (x()) break
    }
    // p is nullable because it's possible loop body is not executed at all
    return p<!UNSAFE_CALL!>.<!>length
}

/* GENERATED_FIR_TAGS: break, checkNotNullCall, functionDeclaration, ifExpression, nullableType, whileLoop */
