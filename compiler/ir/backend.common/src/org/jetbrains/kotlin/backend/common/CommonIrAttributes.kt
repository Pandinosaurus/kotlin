/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common

import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.irAttribute
import org.jetbrains.kotlin.ir.irFlag
import org.jetbrains.kotlin.name.Name

var IrFunction.defaultArgumentsDispatchFunction: IrFunction? by irAttribute(copyByDefault = false)

var IrClass.capturedFields: Collection<IrField>? by irAttribute(copyByDefault = false)

var IrClass.functionReferenceLinkageError: String? by irAttribute(copyByDefault = false)
var IrClass.functionReferenceReflectedName: String? by irAttribute(copyByDefault = false)

/**
 * If this is a `suspend` function, returns its corresponding function with continuations.
 */
var IrSimpleFunction.functionWithContinuations: IrSimpleFunction? by irAttribute(copyByDefault = false)

/**
 * If this is a function with continuation, returns its corresponding `suspend` function.
 */
var IrSimpleFunction.suspendFunction: IrSimpleFunction? by irAttribute(copyByDefault = false)

var IrFunction.defaultArgumentsOriginalFunction: IrFunction? by irAttribute(copyByDefault = true)

var IrConstructor.capturedConstructor: IrConstructor? by irAttribute(copyByDefault = false)

/**
 * A name that is going to be returned by `KFunction.name`, if it's different than `IrSimpleFunction.name`.
 */
var IrSimpleFunction.customNameInReflection: Name? by irAttribute(copyByDefault = false)

/**
 * Flags calls which are the result of an implicit `invoke` operator call.
 */
var IrCall.implicitInvoke: Boolean by irFlag(copyByDefault = false)
