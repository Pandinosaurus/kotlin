// RUN_PIPELINE_TILL: BACKEND
import kotlin.reflect.KProperty

class A3 {
    val a: String by <!REDUNDANT_LABEL_WARNING!>l@<!> MyProperty()

    class MyProperty<T> {}

    operator fun <T> MyProperty<T>.getValue(thisRef: Any?, desc: KProperty<*>): T {
        throw Exception("$thisRef $desc")
    }
}

/* GENERATED_FIR_TAGS: classDeclaration, funWithExtensionReceiver, functionDeclaration, nestedClass, nullableType,
operator, propertyDeclaration, propertyDelegate, starProjection, stringLiteral, typeParameter */
