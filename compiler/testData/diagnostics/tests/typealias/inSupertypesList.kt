// RUN_PIPELINE_TILL: FRONTEND
// FIR_IDENTICAL
interface IBase

typealias B = IBase

class Test1 : B
class Test2 : IBase, <!SUPERTYPE_APPEARS_TWICE!>B<!>

/* GENERATED_FIR_TAGS: classDeclaration, interfaceDeclaration, typeAliasDeclaration */
