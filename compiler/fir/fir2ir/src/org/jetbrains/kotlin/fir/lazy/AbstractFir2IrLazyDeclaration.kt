/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.lazy

import org.jetbrains.kotlin.fir.FirEvaluatorResult
import org.jetbrains.kotlin.fir.backend.Fir2IrComponents
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirResolvePhase
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirExpressionEvaluator
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotationArgumentMapping
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotationCopy
import org.jetbrains.kotlin.fir.symbols.lazyResolveToPhase
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrFactory
import org.jetbrains.kotlin.ir.declarations.impl.IrFactoryImpl
import org.jetbrains.kotlin.ir.declarations.lazy.IrLazyDeclarationBase
import org.jetbrains.kotlin.ir.declarations.lazy.lazyVar
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import kotlin.properties.ReadWriteProperty

interface AbstractFir2IrLazyDeclaration<F> :
    IrDeclaration, IrLazyDeclarationBase, Fir2IrComponents where F : FirDeclaration {

    val fir: F

    override val factory: IrFactory
        get() = IrFactoryImpl

    override fun createLazyAnnotations(): ReadWriteProperty<Any?, List<IrConstructorCall>> = lazyVar(lock) {
        // Normally lazy resolve would be not necessary here,
        // but in context of Kotlin project itself opened in IDE we can have here
        // an annotated built-in function in sources, like arrayOfNull (KT-70856).
        // For any other project, built-ins functions come from libraries and it's not actual.
        fir.lazyResolveToPhase(FirResolvePhase.ANNOTATION_ARGUMENTS)
        fir.annotations.mapNotNull { annotation ->
            val effectiveAnnotation = evaluateAnnotationArguments(annotation)
            callGenerator.convertToIrConstructorCall(effectiveAnnotation) as? IrConstructorCall
        }
    }

    private fun evaluateAnnotationArguments(annotation: FirAnnotation): FirAnnotation {
        val evaluationResult = FirExpressionEvaluator.evaluateAnnotationArguments(annotation, session) ?: return annotation

        return buildAnnotationCopy(annotation) {
            argumentMapping = buildAnnotationArgumentMapping {
                source = annotation.argumentMapping.source

                for ((name, expression) in annotation.argumentMapping.mapping) {
                    val evaluatedExpression = (evaluationResult[name] as? FirEvaluatorResult.Evaluated)?.result as? FirExpression
                    mapping[name] = evaluatedExpression ?: expression
                }
            }
        }
    }
}

internal fun mutationNotSupported(): Nothing =
    error("Mutation of Fir2Ir lazy elements is not possible")
