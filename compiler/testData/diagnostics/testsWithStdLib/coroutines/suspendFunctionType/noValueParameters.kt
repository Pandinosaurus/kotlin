// RUN_PIPELINE_TILL: BACKEND
// FIR_IDENTICAL
typealias Test1 = suspend (Int) -> Unit
typealias Test2 = suspend Int.(Int) -> Unit
typealias Test3 = List<suspend (Int) -> Unit>
typealias Test4 = List<suspend Int.(Int) -> Unit>

/* GENERATED_FIR_TAGS: functionalType, suspend, typeAliasDeclaration, typeWithExtension */
