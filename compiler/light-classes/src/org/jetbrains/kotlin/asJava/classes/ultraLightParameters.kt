/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.asJava.classes

import com.intellij.navigation.ItemPresentation
import com.intellij.navigation.ItemPresentationProviders
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.util.TypeConversionUtil
import org.jetbrains.annotations.NonNls
import org.jetbrains.kotlin.asJava.elements.*
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.builtins.isSuspendFunctionType
import org.jetbrains.kotlin.codegen.AsmUtil
import org.jetbrains.kotlin.codegen.AsmUtil.LABELED_THIS_PARAMETER
import org.jetbrains.kotlin.codegen.AsmUtil.RECEIVER_PARAMETER_NAME
import org.jetbrains.kotlin.codegen.coroutines.SUSPEND_FUNCTION_COMPLETION_PARAMETER_NAME
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.AnnotationUseSiteTarget
import org.jetbrains.kotlin.load.kotlin.TypeMappingMode
import org.jetbrains.kotlin.psi.KtCallableDeclaration
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.psiUtil.isPrivate
import org.jetbrains.kotlin.resolve.indexOrMinusOne
import org.jetbrains.kotlin.types.KotlinType

internal class KtUltraLightSuspendContinuationParameter(
    private val ktFunction: KtFunction,
    private val support: KtUltraLightSupport,
    method: KtLightMethod
) : LightParameter(SUSPEND_FUNCTION_COMPLETION_PARAMETER_NAME, PsiTypes.nullType(), method, method.language),
    KtLightParameter,
    KtUltraLightElementWithNullabilityAnnotationDescriptorBased<KtParameter, PsiParameter> {

    override val qualifiedNameForNullabilityAnnotation: String?
        get() = if (!ktFunction.isPrivate()) computeQualifiedNameForNullabilityAnnotation(ktType) else null

    override val psiTypeForNullabilityAnnotation: PsiType? get() = psiType
    override val kotlinOrigin: KtParameter? = null

    private val ktType: KotlinType?
        get() {
            val descriptor = ktFunction.resolve() as? FunctionDescriptor
            val returnType = descriptor?.returnType ?: return null
            return support.moduleDescriptor.getContinuationOfTypeOrAny(returnType)
        }

    private val psiType by lazyPub {
        ktType?.asPsiType(support, TypeMappingMode.DEFAULT, method) ?: PsiTypes.nullType()
    }

    private val lightModifierList by lazyPub { KtUltraLightSimpleModifierList(this, emptySet()) }

    override fun getType(): PsiType = psiType

    override fun equals(other: Any?): Boolean = other === this ||
            other is KtUltraLightSuspendContinuationParameter &&
            other.ktFunction === this.ktFunction

    override fun isVarArgs(): Boolean = false
    override fun hashCode(): Int = name.hashCode()
    override fun getModifierList(): PsiModifierList = lightModifierList
    override fun getNavigationElement(): PsiElement = ktFunction.navigationElement
    override fun getUseScope(): SearchScope = ktFunction.useScope
    override fun isValid() = ktFunction.isValid
    override fun getContainingFile(): PsiFile = ktFunction.containingFile
    override fun getParent(): PsiElement = method.parameterList

    override fun isEquivalentTo(another: PsiElement?): Boolean =
        another is KtUltraLightSuspendContinuationParameter && another.psiType == this.psiType

    override fun copy(): PsiElement = KtUltraLightSuspendContinuationParameter(ktFunction, support, method)
}

internal abstract class KtUltraLightParameter(
    name: String,
    override val kotlinOrigin: KtParameter?,
    protected val support: KtUltraLightSupport,
    private val ultraLightMethod: KtUltraLightMethod
) : LightParameter(
    name,
    PsiTypes.nullType(),
    ultraLightMethod,
    ultraLightMethod.language
), KtUltraLightElementWithNullabilityAnnotationDescriptorBased<KtParameter, PsiParameter>, KtLightParameter {
    override fun isEquivalentTo(another: PsiElement?): Boolean {
        return another is KtParameter && kotlinOrigin?.isEquivalentTo(another) == true || this == another
    }

    private val lightModifierList by lazyPub { KtUltraLightSimpleModifierList(this, emptySet()) }

    override fun getModifierList(): PsiModifierList = lightModifierList

    override fun getNavigationElement(): PsiElement = kotlinOrigin ?: method.navigationElement
    override fun getUseScope(): SearchScope = kotlinOrigin?.useScope ?: LocalSearchScope(this)

    override fun getText(): String? = kotlinOrigin?.text.orEmpty()
    override fun getTextRange(): TextRange? = kotlinOrigin?.textRange
    override fun getTextOffset(): Int = kotlinOrigin?.textOffset ?: super.getTextOffset()

    override fun isValid() = parent.isValid

    override fun computeQualifiedNameForNullabilityAnnotation(kotlinType: KotlinType?): String? {
        val typeForAnnotation =
            if (isVarArgs && kotlinType != null && KotlinBuiltIns.isArray(kotlinType)) kotlinType.arguments[0].type else kotlinType
        return super.computeQualifiedNameForNullabilityAnnotation(typeForAnnotation)
    }

    override val psiTypeForNullabilityAnnotation: PsiType?
        get() = type

    protected fun computeParameterType(kotlinType: KotlinType?, containingDeclaration: CallableDescriptor?): PsiType {
        kotlinType ?: return PsiTypes.nullType()

        if (kotlinType.isSuspendFunctionType) {
            return kotlinType.asPsiType(support, TypeMappingMode.DEFAULT, this)
        } else {
            val containingDescriptor = containingDeclaration ?: return PsiTypes.nullType()
            val mappedType = support.mapType(kotlinType, this) { typeMapper, sw ->
                typeMapper.writeParameterType(sw, kotlinType, containingDescriptor)
            }

            return if (ultraLightMethod.checkNeedToErasureParametersTypes) TypeConversionUtil.erasure(mappedType) else mappedType
        }
    }

    abstract override fun getType(): PsiType

    override fun getContainingFile(): PsiFile = method.containingFile
    override fun getParent(): PsiElement = method.parameterList

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is KtUltraLightParameter || other.javaClass != this.javaClass || other.name != this.name) return false
        if (other.kotlinOrigin != null) {
            return other.kotlinOrigin == this.kotlinOrigin
        }

        return this.kotlinOrigin == null && other.ultraLightMethod == this.ultraLightMethod
    }

    override fun hashCode(): Int = name.hashCode()

    abstract override fun isVarArgs(): Boolean
}

internal abstract class KtAbstractUltraLightParameterForDeclaration(
    name: String,
    kotlinOrigin: KtParameter?,
    support: KtUltraLightSupport,
    method: KtUltraLightMethod,
    protected val containingDeclaration: KtCallableDeclaration
) : KtUltraLightParameter(name, kotlinOrigin, support, method) {

    protected fun tryGetContainingDescriptor(): CallableDescriptor? =
        containingDeclaration.resolve() as? CallableDescriptor

    protected abstract fun tryGetKotlinType(): KotlinType?

    private val _parameterType: PsiType by lazyPub {
        computeParameterType(tryGetKotlinType(), tryGetContainingDescriptor())
    }

    override fun getType(): PsiType = _parameterType

    override val qualifiedNameForNullabilityAnnotation: String? by lazyPub {
        computeQualifiedNameForNullabilityAnnotation(tryGetKotlinType())
    }
}

internal class KtUltraLightParameterForSource(
    name: String,
    override val kotlinOrigin: KtParameter,
    support: KtUltraLightSupport,
    method: KtUltraLightMethod,
    containingDeclaration: KtCallableDeclaration
) : KtAbstractUltraLightParameterForDeclaration(name, kotlinOrigin, support, method, containingDeclaration) {

    override fun tryGetKotlinType(): KotlinType? = kotlinOrigin.getKotlinType()

    override fun isVarArgs(): Boolean = kotlinOrigin.isVarArg && method.parameterList.parameters.last() == this

    override fun setName(@NonNls name: String): PsiElement {
        kotlinOrigin.setName(name)
        return this
    }

    override val givenAnnotations: List<KtLightAbstractAnnotation>?
        get() {
            return if (kotlinOrigin.hasValOrVar()) {
                val entriesWithoutJvmField = kotlinOrigin.annotationEntries.filter { it.shortName?.identifier != "JvmField" }
                entriesWithoutJvmField.toLightAnnotations(this, null) +
                        entriesWithoutJvmField.toLightAnnotations(this, AnnotationUseSiteTarget.CONSTRUCTOR_PARAMETER)
            } else {
                kotlinOrigin.annotationEntries.toLightAnnotations(this, null)
            }
        }

    override fun getStartOffsetInParent(): Int = kotlinOrigin.startOffsetInParent
    override fun isWritable(): Boolean = kotlinOrigin.isWritable
    override fun getNavigationElement(): PsiElement = kotlinOrigin.navigationElement
    override fun getContainingFile(): PsiFile = parent.containingFile
    override fun getPresentation(): ItemPresentation? = kotlinOrigin.let { ItemPresentationProviders.getItemPresentation(it) }
    override fun findElementAt(offset: Int): PsiElement? = kotlinOrigin.findElementAt(offset)
}

internal class KtUltraLightParameterForSetterParameter(
    name: String,
    // KtProperty or KtParameter from primary constructor
    private val property: KtDeclaration,
    support: KtUltraLightSupport,
    method: KtUltraLightMethod,
    containingDeclaration: KtCallableDeclaration
) : KtAbstractUltraLightParameterForDeclaration(name, null, support, method, containingDeclaration) {

    override fun tryGetKotlinType(): KotlinType? = property.getKotlinType()

    override val givenAnnotations: List<KtLightAbstractAnnotation>?
        get() = property.annotationEntries.toLightAnnotations(this, AnnotationUseSiteTarget.SETTER_PARAMETER)

    override fun isVarArgs(): Boolean = false

    override fun equals(other: Any?): Boolean = other === this ||
            other is KtUltraLightParameterForSetterParameter &&
            other.name == this.name &&
            other.property == this.property

    override fun hashCode(): Int = name.hashCode()
}

internal class KtUltraLightReceiverParameter(
    containingDeclaration: KtCallableDeclaration,
    support: KtUltraLightSupport,
    method: KtUltraLightMethod
) : KtAbstractUltraLightParameterForDeclaration(
    // See `ExpressionCodegen.getNameForReceiverParameter`.
    name = AsmUtil.getLabeledThisName(containingDeclaration.name ?: method.name, LABELED_THIS_PARAMETER, RECEIVER_PARAMETER_NAME),
    kotlinOrigin = null,
    support = support,
    method = method,
    containingDeclaration = containingDeclaration
) {

    override val givenAnnotations: List<KtLightAbstractAnnotation>? =
        containingDeclaration
            .receiverTypeReference
            ?.modifierList
            ?.annotationEntries
            ?.toLightAnnotations(this, AnnotationUseSiteTarget.RECEIVER)
            ?: emptyList()

    override fun isVarArgs(): Boolean = false

    override fun tryGetKotlinType(): KotlinType? =
        tryGetContainingDescriptor()?.extensionReceiverParameter?.type
}

internal class KtUltraLightParameterForDescriptor(
    descriptor: ParameterDescriptor,
    support: KtUltraLightSupport,
    method: KtUltraLightMethod
) : KtUltraLightParameter(
    if (descriptor.name.isSpecial) "\$self" else descriptor.name.identifier,
    null, support, method
) {
    // This is greedy realization of UL class.
    // This means that all data that depends on descriptor evaluated in ctor so the descriptor will be released on the end.
    // Be aware to save descriptor in class instance or any depending references

    private val lazyInitializers = mutableListOf<Lazy<*>>()
    private inline fun <T> getAndAddLazy(crossinline initializer: () -> T): Lazy<T> =
        lazyPub { initializer() }.also { lazyInitializers.add(it) }

    override val qualifiedNameForNullabilityAnnotation: String? by getAndAddLazy {
        computeQualifiedNameForNullabilityAnnotation(descriptor.type)
    }

    private val _isVarArgs: Boolean by getAndAddLazy {
        (descriptor as? ValueParameterDescriptor)?.varargElementType != null
    }

    override fun isVarArgs() = _isVarArgs

    override val givenAnnotations: List<KtLightAbstractAnnotation> by getAndAddLazy {
        descriptor.obtainLightAnnotations(this)
    }

    private val _parameterType by getAndAddLazy {
        computeParameterType(descriptor.type, descriptor.containingDeclaration as? CallableMemberDescriptor)
    }

    private val _index: Int by getAndAddLazy {
        descriptor.indexOrMinusOne()
    }

    override fun getType(): PsiType = _parameterType

    override fun equals(other: Any?): Boolean = other === this ||
            other is KtUltraLightParameterForDescriptor &&
            other.name == this.name &&
            other._index == this._index &&
            other.method == this.method

    override fun hashCode(): Int = name.hashCode()

    init {
        //We should force computations on all lazy delegates to release descriptor on the end of ctor call
        with(lazyInitializers) {
            forEach { it.value }
            clear()
        }
    }
}
