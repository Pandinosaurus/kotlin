/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.idea.frontend.api.components

import com.intellij.psi.PsiType
import org.jetbrains.kotlin.load.kotlin.TypeMappingMode
import org.jetbrains.kotlin.psi.KtDoubleColonExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtTypeReference

public abstract class KtPsiTypeProvider : KtAnalysisSessionComponent() {
    public abstract fun getPsiTypeForKtExpression(expression: KtExpression, mode: TypeMappingMode): PsiType
    public abstract fun getPsiTypeForKtTypeReference(ktTypeReference: KtTypeReference, mode: TypeMappingMode): PsiType
    public abstract fun getPsiTypeForReceiverOfDoubleColonExpression(
        ktDoubleColonExpression: KtDoubleColonExpression,
        mode: TypeMappingMode
    ): PsiType?
}

public interface KtPsiTypeProviderMixIn : KtAnalysisSessionMixIn {
    public fun KtExpression.getPsiType(mode: TypeMappingMode = TypeMappingMode.DEFAULT): PsiType =
        analysisSession.psiTypeProvider.getPsiTypeForKtExpression(this, mode)

    public fun KtTypeReference.getPsiType(mode: TypeMappingMode = TypeMappingMode.DEFAULT): PsiType =
        analysisSession.psiTypeProvider.getPsiTypeForKtTypeReference(this, mode)

    public fun KtDoubleColonExpression.getReceiverPsiType(mode: TypeMappingMode = TypeMappingMode.DEFAULT): PsiType? =
        analysisSession.psiTypeProvider.getPsiTypeForReceiverOfDoubleColonExpression(this, mode)
}
