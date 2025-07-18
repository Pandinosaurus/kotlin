// RUN_PIPELINE_TILL: BACKEND
// JVM_DEFAULT_MODE: enable
// JVM_TARGET: 1.8
// WITH_STDLIB

interface Foo<T> {
    fun test(p: T) = "fail"
    val T.prop: String
        get() = "fail"
}

interface FooDerived: Foo<String>

class Unspecialized<Y> : Foo<Y>

open class <!EXPLICIT_OVERRIDE_REQUIRED_IN_COMPATIBILITY_MODE, EXPLICIT_OVERRIDE_REQUIRED_IN_COMPATIBILITY_MODE!>UnspecializedFromDerived<!> : FooDerived

abstract class <!EXPLICIT_OVERRIDE_REQUIRED_IN_COMPATIBILITY_MODE, EXPLICIT_OVERRIDE_REQUIRED_IN_COMPATIBILITY_MODE!>AbstractUnspecializedFromDerived<!> : FooDerived

open class <!EXPLICIT_OVERRIDE_REQUIRED_IN_COMPATIBILITY_MODE, EXPLICIT_OVERRIDE_REQUIRED_IN_COMPATIBILITY_MODE!>Specialized<!> : Foo<String>

abstract class <!EXPLICIT_OVERRIDE_REQUIRED_IN_COMPATIBILITY_MODE, EXPLICIT_OVERRIDE_REQUIRED_IN_COMPATIBILITY_MODE!>AbstractSpecialized<!> : Foo<String>


@JvmDefaultWithoutCompatibility
open class UnspecializedFromDerivedNC : FooDerived

@JvmDefaultWithoutCompatibility
abstract class AbstractUnspecializedFromDerivedNC : FooDerived

@JvmDefaultWithoutCompatibility
open class SpecializedNC : Foo<String>

@JvmDefaultWithoutCompatibility
abstract class AbstractSpecializedNC : Foo<String>


final class FinalSpecialized : Foo<String>

sealed class SealedSpecialized : Foo<String> {
    open class A : SealedSpecialized();
}

enum class EnumSpecialized : Foo<String> {
     ENTRY {
         fun test() = 123
     }
}

object ObjectSpecialized : Foo<String>

private class Outer {

    open class InnerSpecialized: Foo<String>
}

fun local() {
    object : Foo<String> {}
}

/* GENERATED_FIR_TAGS: anonymousObjectExpression, classDeclaration, enumDeclaration, enumEntry, functionDeclaration,
getter, interfaceDeclaration, nestedClass, nullableType, objectDeclaration, propertyDeclaration,
propertyWithExtensionReceiver, sealed, stringLiteral, typeParameter */
