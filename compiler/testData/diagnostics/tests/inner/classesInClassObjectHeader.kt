// RUN_PIPELINE_TILL: FRONTEND
class Test {
    @`InnerAnnotation` <!REPEATED_ANNOTATION!>@InnerAnnotation<!>
    companion object : StaticClass(), <!DEBUG_INFO_UNRESOLVED_WITH_TARGET, MANY_CLASSES_IN_SUPERTYPE_LIST, UNRESOLVED_REFERENCE!>InnerClass<!>() {

    }

    annotation class InnerAnnotation
    open class StaticClass

    open inner class InnerClass
}

/* GENERATED_FIR_TAGS: annotationDeclaration, classDeclaration, companionObject, inner, nestedClass, objectDeclaration */
