// RUN_PIPELINE_TILL: FRONTEND
class A {
    val x: Int = 1
        get() {
            ::<!UNSUPPORTED!>field<!>
            return field
        }
}
