// RUN_PIPELINE_TILL: BACKEND
// FIR_IDENTICAL
package a

//KT-2166 Control flow analysis doesn't detect that a 'while(true)' loop never terminates
fun foo(): Int {
    while (true) {
    }
}

//KT-2103 Compiler requires return statement after loop which never exits
fun foo1() : Boolean{
    while(true){
        if (bar()) continue
        return true
    }
}

fun bar() : Boolean = true

/* GENERATED_FIR_TAGS: continue, functionDeclaration, ifExpression, whileLoop */
