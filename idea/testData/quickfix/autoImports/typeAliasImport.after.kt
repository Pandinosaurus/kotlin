import dependency.TestAlias

// "Import" "true"
// ERROR: Unresolved reference: TestAlias

fun test() {
    val a = <caret>TestAlias
}

/* IGNORE_FIR */