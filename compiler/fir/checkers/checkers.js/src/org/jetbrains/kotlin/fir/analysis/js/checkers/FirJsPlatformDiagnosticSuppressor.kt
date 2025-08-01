/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.js.checkers

import org.jetbrains.kotlin.fir.analysis.checkers.FirPlatformDiagnosticSuppressor
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.hasAnnotationOrInsideAnnotatedClass
import org.jetbrains.kotlin.fir.declarations.FirCallableDeclaration
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.name.JsStandardClassIds

context(context: CheckerContext)
private fun FirDeclaration.isLexicallyInsideJsNative(): Boolean {
    return JsStandardClassIds.Annotations.nativeAnnotations.any { hasAnnotationOrInsideAnnotatedClass(it, context.session) }
}

class FirJsPlatformDiagnosticSuppressor : FirPlatformDiagnosticSuppressor {
    context(context: CheckerContext)
    override fun shouldReportNoBody(declaration: FirCallableDeclaration): Boolean =
        !declaration.isLexicallyInsideJsNative()
}
