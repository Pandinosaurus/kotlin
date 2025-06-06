// This file was generated automatically. See  generateTestDataForTypeScriptWithFileExport.kt
// DO NOT MODIFY IT MANUALLY.

// CHECK_TYPESCRIPT_DECLARATIONS
// RUN_PLAIN_BOX_FUNCTION
// SKIP_NODE_JS
// INFER_MAIN_MODULE

// TODO fix statics export in DCE-driven mode
// SKIP_DCE_DRIVEN

// MODULE: JS_TESTS
// FILE: visibility.kt

@file:JsExport


internal val internalVal = 10

internal fun internalFun() = 10

internal class internalClass

internal external interface internalInterface


private val privateVal = 10

private fun privateFun() = 10

private class privateClass

private external interface privateInterface


public val publicVal = 10

public fun publicFun() = 10

public class publicClass

public external interface publicInterface


open class Class {
    internal val internalVal = 10
    internal fun internalFun() = 10
    internal class internalClass

    private val privateVal = 10
    private fun privateFun() = 10
    private class privateClass

    protected val protectedVal = 10
    protected fun protectedFun() = 10
    protected class protectedClass {}
    protected object protectedNestedObject {}
    protected companion object {
        val companionObjectProp = 10
    }

    public class classWithProtectedConstructors protected constructor() {

        @JsName("createWithString")
        protected constructor(arg: String): this()
    }

    public val publicVal = 10
    @JsName("publicFun")  // TODO: Should work without JsName
    public fun publicFun() = 10
    public class publicClass
}


enum class EnumClass {
    EC1,
    EC2
}