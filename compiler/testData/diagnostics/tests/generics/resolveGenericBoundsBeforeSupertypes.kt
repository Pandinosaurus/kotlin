// RUN_PIPELINE_TILL: BACKEND
// FIR_IDENTICAL
// Tests that generic bounds in the object supertype are resolved prior to the supertype itself

object O : Tr<V<*>>

interface Tr<T>

class V<out S>

/* GENERATED_FIR_TAGS: classDeclaration, interfaceDeclaration, nullableType, objectDeclaration, out, starProjection,
typeParameter */
