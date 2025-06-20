// RUN_PIPELINE_TILL: FRONTEND
// DIAGNOSTICS: -UNUSED_VARIABLE, -UNSUPPORTED

fun basicTypes() {
    val a: IntArray = [1]
    val b: ByteArray = [1]
    val c: BooleanArray = [true, false]
    val d: CharArray = ['a']
    val e: ShortArray = [1]
    val f: FloatArray = [1.0f]
    val g: LongArray = [1]
    val h: DoubleArray = [1.0]
}

fun basicTypesWithErrors() {
    val a: IntArray = <!INITIALIZER_TYPE_MISMATCH!>[1.0]<!>
    val b: ShortArray = <!INITIALIZER_TYPE_MISMATCH!>[1.0]<!>
    val c: CharArray = <!INITIALIZER_TYPE_MISMATCH!>["a"]<!>
}

/* GENERATED_FIR_TAGS: collectionLiteral, functionDeclaration, integerLiteral, localProperty, propertyDeclaration,
stringLiteral */
