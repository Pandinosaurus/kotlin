// RUN_PIPELINE_TILL: BACKEND
data class D(val x: Int, val y: String)

fun foo(list: List<D>) {
    for ((x, y) in list) {
    }
    val (x, y) = list.first()
    list.forEach { (x, y) ->
        println(x)
        println(y)
    }
}

/* GENERATED_FIR_TAGS: classDeclaration, data, destructuringDeclaration, forLoop, functionDeclaration, lambdaLiteral,
localProperty, primaryConstructor, propertyDeclaration */
