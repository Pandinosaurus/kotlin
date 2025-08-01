// RUN_PIPELINE_TILL: BACKEND
// FIR_IDENTICAL
// OPT_IN: kotlin.RequiresOptIn
// FILE: api.kt

package feature.experimental.self

@RequiresOptIn
@Retention(AnnotationRetention.BINARY)
annotation class ImportedMarker

@ImportedMarker
object ImportedClass {
    @ImportedMarker
    fun importedObjectMember() {}
}

@ImportedMarker
fun importedFunction() {}

@ImportedMarker
val importedProperty = Unit

// FILE: usage.kt

import feature.experimental.self.ImportedMarker
import feature.experimental.self.ImportedClass
import feature.experimental.self.importedFunction
import feature.experimental.self.importedProperty
import feature.experimental.self.ImportedClass.importedObjectMember

/* GENERATED_FIR_TAGS: annotationDeclaration, functionDeclaration, objectDeclaration, propertyDeclaration */
