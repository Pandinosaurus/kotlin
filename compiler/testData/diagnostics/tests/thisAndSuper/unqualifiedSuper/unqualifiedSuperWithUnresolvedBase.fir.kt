// RUN_PIPELINE_TILL: FRONTEND
// Check that unresolved super type doesn't interfere with unqualified super resolution.

open class Base() {
    open fun foo() {}

    open fun ambiguous() {}

    open val prop: Int
        get() = 1234

    open val ambiguousProp: Int
        get() = 111
}

interface Interface {
    fun bar() {}

    fun ambiguous() {}

    val ambiguousProp: Int
        get() = 222
}

class ClassDerivedFromUnresolved : Base(), Interface, <!UNRESOLVED_REFERENCE!>Unresolved<!> {
    override fun foo() {}
    override fun bar() {}

    override fun ambiguous() {}

    override val ambiguousProp: Int
        get() = 333

    override val prop: Int
        get() = 4321

    fun callsFunFromSuperClass() {
        super.foo()
    }

    fun getSuperProp(): Int =
            super.prop

    fun getAmbiguousSuperProp(): Int =
    <!AMBIGUOUS_SUPER!>super<!>.ambiguousProp

    fun callsFunFromSuperInterface() {
        super.bar()
    }

    fun callsAmbiguousSuperFun() {
        <!AMBIGUOUS_SUPER!>super<!>.ambiguous()
    }
}

/* GENERATED_FIR_TAGS: classDeclaration, functionDeclaration, getter, integerLiteral, interfaceDeclaration, override,
primaryConstructor, propertyDeclaration, superExpression */
