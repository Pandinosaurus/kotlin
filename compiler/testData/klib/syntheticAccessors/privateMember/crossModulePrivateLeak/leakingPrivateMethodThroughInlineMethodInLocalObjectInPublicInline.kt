// KT-72840: java.lang.AssertionError: AFTER mandatory stack transformations: incorrect bytecode
// IGNORE_BACKEND: JVM_IR
// IGNORE_BACKEND: ANDROID

// MODULE: lib
// FILE: A.kt
class A {
    public inline fun publicInlineMethod(crossinline f: () -> String) = object {
        private fun privateMethod() = "O"
        inline fun run() = privateMethod() + f()
    }.run()
}

// MODULE: main(lib)
// FILE: main.kt
fun box(): String {
    return A().publicInlineMethod { "K" }
}
