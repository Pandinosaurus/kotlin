// CHECK_TYPE

fun foo(arr: Array<out Number>): Int {
    @Suppress("UNCHECKED_CAST")
    val result = (arr as Array<Int>)[0]
    checkSubtype<Array<Int>>(arr)
    return result
}
