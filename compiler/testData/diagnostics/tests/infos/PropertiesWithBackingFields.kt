// RUN_PIPELINE_TILL: FRONTEND
// FIR_IDENTICAL
abstract class Test() {
    abstract val x : Int
    abstract val x1 : Int get
    abstract val x2 : Int <!ABSTRACT_PROPERTY_WITH_GETTER!>get() = 1<!>

    <!MUST_BE_INITIALIZED_OR_BE_ABSTRACT!>val a : Int<!>
    <!MUST_BE_INITIALIZED_OR_BE_ABSTRACT!>val b : Int<!> get
    val c = 1

    val c1 = 1
      get
    val c2 : Int
        get() = 1
    val c3 : Int
        get() { return 1 }
    val c4 : Int
        get() = 1
    <!MUST_BE_INITIALIZED!>val c5 : Int<!>
        get() = field + 1

    abstract var y : Int
    abstract var y1 : Int get
    abstract var y2 : Int set
    abstract var y3 : Int set get
    abstract var y4 : Int set <!ABSTRACT_PROPERTY_WITH_GETTER!>get() = 1<!>
    abstract var y5 : Int <!ABSTRACT_PROPERTY_WITH_SETTER!>set(x) {}<!> <!ABSTRACT_PROPERTY_WITH_GETTER!>get() = 1<!>
    abstract var y6 : Int <!ABSTRACT_PROPERTY_WITH_SETTER!>set(x) {}<!>

    <!MUST_BE_INITIALIZED_OR_BE_ABSTRACT!>var v : Int<!>
    <!MUST_BE_INITIALIZED_OR_BE_ABSTRACT!>var v1 : Int<!> get
    <!MUST_BE_INITIALIZED_OR_BE_ABSTRACT!>var v2 : Int<!> get set
    <!MUST_BE_INITIALIZED!>var v3 : Int<!> get() = 1; set
    var v4 : Int get() = 1; set(x){}

    <!MUST_BE_INITIALIZED!>var v5 : Int<!> get() = 1; set(x){field = x}
    <!MUST_BE_INITIALIZED!>var v6 : Int<!> get() = field + 1; set(x){}

  abstract val v7 : Int get
  abstract var v8 : Int get set
  <!MUST_BE_INITIALIZED_OR_BE_ABSTRACT!>var v9 : Int<!> set
  <!MUST_BE_INITIALIZED_OR_BE_ABSTRACT!>var v10 : Int<!>  get
  abstract val v11 : Int <!WRONG_MODIFIER_TARGET!>abstract<!> get
  abstract var v12 : Int <!WRONG_MODIFIER_TARGET!>abstract<!> get <!WRONG_MODIFIER_TARGET!>abstract<!> set

}

open class Super(i : Int)

class TestPCParameters(w : Int, x : Int, val y : Int, var z : Int) : Super(w) {

  val xx = w

  init {
    w + 1
  }

  fun foo() = <!UNRESOLVED_REFERENCE!>x<!>

}

/* GENERATED_FIR_TAGS: additiveExpression, assignment, classDeclaration, functionDeclaration, getter, init,
integerLiteral, primaryConstructor, propertyDeclaration, setter */
