// RUN_PIPELINE_TILL: FRONTEND
interface Promise<T>

fun <T> materializePromise(): Promise<T> = TODO()

fun foo0(x: Int, p2: Promise<Nothing?>): Promise<*> {
    return if (x == 3) {
        <!CANNOT_INFER_PARAMETER_TYPE!>materializePromise<!>()
    } else {
        p2
    }
}

fun foo1(x: Int, p1: Promise<Any?>, p2: Promise<Nothing?>): Promise<*> {
    return if (x == 0) {
        p1
    } else {
        if (x == 3) {
            materializePromise()
        } else {
            p2
        }
    }
}

fun foo2(x: Int, p1: Promise<Any?>): Promise<*> {
    return if (x == 0) {
        p1
    } else if (x == 2) {
        materializePromise()
    } else {
        p1
    }
}

/* GENERATED_FIR_TAGS: equalityExpression, functionDeclaration, ifExpression, integerLiteral, interfaceDeclaration,
nullableType, starProjection, typeParameter */
