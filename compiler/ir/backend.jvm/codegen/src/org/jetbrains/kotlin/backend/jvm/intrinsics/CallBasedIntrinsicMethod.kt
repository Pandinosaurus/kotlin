/*
 * Copyright 2010-2025 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.jvm.intrinsics

import org.jetbrains.kotlin.backend.jvm.codegen.*
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.kotlin.resolve.jvm.jvmSignature.JvmMethodSignature

abstract class CallBasedIntrinsicMethod : IntrinsicMethod() {
    abstract fun toCallable(
        expression: IrFunctionAccessExpression, signature: JvmMethodSignature, classCodegen: ClassCodegen,
    ): IntrinsicFunction

    final override fun invoke(expression: IrFunctionAccessExpression, codegen: ExpressionCodegen, data: BlockInfo): PromisedValue? {
        val descriptor = codegen.methodSignatureMapper.mapSignatureSkipGeneric(expression.symbol.owner)
        val callable = toCallable(expression, descriptor, codegen.classCodegen)
        callable.invoke(codegen.mv, codegen, data, expression)
        return MaterialValue(codegen, callable.signature.returnType, expression.type)
    }
}
