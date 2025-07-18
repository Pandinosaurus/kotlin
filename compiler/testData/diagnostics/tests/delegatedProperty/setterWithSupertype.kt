// RUN_PIPELINE_TILL: BACKEND
// FIR_IDENTICAL
// DIAGNOSTICS: -UNUSED_PARAMETER

import kotlin.reflect.KProperty

open class Base
class Derived: Base()

var a: Derived by A()

class A {
  operator fun getValue(t: Any?, p: KProperty<*>): Derived {
    return Derived()
  }

  operator fun setValue(t: Any?, p: KProperty<*>, i: Base) {}
}

/* GENERATED_FIR_TAGS: classDeclaration, functionDeclaration, nullableType, operator, propertyDeclaration,
propertyDelegate, setter, starProjection */
