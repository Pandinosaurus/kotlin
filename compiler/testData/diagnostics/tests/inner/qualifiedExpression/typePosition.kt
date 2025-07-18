// RUN_PIPELINE_TILL: BACKEND
// FIR_IDENTICAL
abstract class Outer {
    class Nested {
        class NestedNested
    }
    
    abstract val prop1: Nested
    abstract val prop2: Nested.NestedNested
}

fun foo(): Outer.Nested = null!!
val bar: Outer.Nested.NestedNested = null!!

/* GENERATED_FIR_TAGS: checkNotNullCall, classDeclaration, functionDeclaration, nestedClass, propertyDeclaration */
