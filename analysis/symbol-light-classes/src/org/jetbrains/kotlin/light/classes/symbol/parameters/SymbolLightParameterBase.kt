/*
 * Copyright 2010-2025 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.light.classes.symbol.parameters

import com.intellij.navigation.NavigationItem
import com.intellij.psi.*
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.util.IncorrectOperationException
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.projectStructure.KaModule
import org.jetbrains.kotlin.analysis.api.types.KaType
import org.jetbrains.kotlin.analysis.api.types.KaTypeMappingMode
import org.jetbrains.kotlin.asJava.elements.*
import org.jetbrains.kotlin.light.classes.symbol.basicIsEquivalentTo
import org.jetbrains.kotlin.light.classes.symbol.classes.typeForValueClass
import org.jetbrains.kotlin.light.classes.symbol.invalidAccess
import org.jetbrains.kotlin.light.classes.symbol.isOriginEquivalentTo
import org.jetbrains.kotlin.light.classes.symbol.methods.SymbolLightMethodBase
import org.jetbrains.kotlin.psi.KtParameter

internal abstract class SymbolLightParameterBase(containingDeclaration: SymbolLightMethodBase) : PsiVariable, NavigationItem,
    KtLightElement<KtParameter, PsiParameter>, KtLightParameter, KtLightElementBase(containingDeclaration) {
    protected val ktModule: KaModule get() = method.ktModule

    override val givenAnnotations: List<KtLightAbstractAnnotation>
        get() = invalidAccess()

    override fun getTypeElement(): PsiTypeElement? = null
    override fun getInitializer(): PsiExpression? = null
    override fun hasInitializer(): Boolean = false
    override fun computeConstantValue(): Any? = null

    abstract override fun getNameIdentifier(): PsiIdentifier?

    abstract override fun getName(): String

    @Throws(IncorrectOperationException::class)
    override fun normalizeDeclaration() {
    }

    override fun setName(p0: String): PsiElement = TODO() //cannotModify()

    override val method: SymbolLightMethodBase = containingDeclaration

    override fun getDeclarationScope(): KtLightMethod = method

    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is JavaElementVisitor) {
            visitor.visitParameter(this)
        } else {
            visitor.visitElement(this)
        }
    }

    override fun toString(): String = "${this::class.simpleName}:$name"

    override fun isEquivalentTo(another: PsiElement?): Boolean =
        basicIsEquivalentTo(this, another as? PsiParameter) || isOriginEquivalentTo(another)

    override fun getNavigationElement(): PsiElement = kotlinOrigin ?: method.navigationElement

    override fun getUseScope(): SearchScope = kotlinOrigin?.useScope ?: LocalSearchScope(this)

    override fun isValid() = parent.isValid

    abstract override fun getType(): PsiType

    override fun getContainingFile(): PsiFile = method.containingFile

    override fun getParent(): PsiElement = method.parameterList

    abstract override fun equals(other: Any?): Boolean

    abstract override fun hashCode(): Int

    abstract override fun isVarArgs(): Boolean

    protected fun KaSession.getTypeMappingMode(type: KaType): KaTypeMappingMode = when {
        type.isSuspendFunctionType -> KaTypeMappingMode.DEFAULT
        method.isJvmExposedBoxed && typeForValueClass(type) -> KaTypeMappingMode.VALUE_PARAMETER_BOXED
        // TODO: extract type mapping mode from annotation?
        // TODO: methods with declaration site wildcards?
        else -> KaTypeMappingMode.VALUE_PARAMETER
    }
}