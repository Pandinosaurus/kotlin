// RUN_PIPELINE_TILL: BACKEND
// FIR_IDENTICAL
// do not report generate empty synthetic constructor by primary as it leads to CONFLICTING_JVM_DECLARATIONS
class A(val x: Int = 1, val y: Int = 2) {
    constructor(): this(0, 0)
}

/* GENERATED_FIR_TAGS: classDeclaration, integerLiteral, primaryConstructor, propertyDeclaration, secondaryConstructor */
