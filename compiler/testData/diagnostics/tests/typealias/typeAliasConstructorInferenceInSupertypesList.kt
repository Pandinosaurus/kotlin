// RUN_PIPELINE_TILL: FRONTEND
// FIR_IDENTICAL
open class Ref<T>(var x: T)

typealias R<T> = Ref<T>

// Type inference SHOULD NOT work for type alias constructor in supertypes list
class Test1 : <!WRONG_NUMBER_OF_TYPE_ARGUMENTS!>R<!>(0)
class Test2 : R<Int>(0)

/* GENERATED_FIR_TAGS: classDeclaration, integerLiteral, nullableType, primaryConstructor, propertyDeclaration,
typeAliasDeclaration, typeAliasDeclarationWithTypeParameter, typeParameter */
