// RUN_PIPELINE_TILL: BACKEND
// FIR_IDENTICAL
object A {
    val iii = 42
}

//inappropriate but participating in resolve functions
//fun foo(s: String, a: Any) = s + a
//fun foo(a: Any, s: String) = s + a
//fun foo(i: Int, j: Int) = i + j
//fun foo(a: Any, i: Int) = "$a$i"
//fun foo(f: (Int)->Int, i: Int) = f(i)
//fun foo(f: (String)->Int, s: String) = f(s)
//fun foo(f: (Any)->Int, a: Any) = f(a)
//fun foo(s: String, f: (String)->Int) = f(s)
//fun foo(a: Any, f: (Any)->Int) = f(a)
//appropriate function
fun foo(i: Int, f: (Int)->Int) = f(i)

fun test() {
    foo(1) { x1: Int ->
        foo(2) { x2: Int ->
            foo(3) { x3: Int ->
                foo(4) { x4: Int ->
                    foo(5) { x5: Int ->
                        x1 + x2 + x3 + x4 + x5 + A.iii
                    }
                }
            }
        }
    }
}

/* GENERATED_FIR_TAGS: additiveExpression, functionDeclaration, functionalType, integerLiteral, lambdaLiteral,
objectDeclaration, propertyDeclaration */
