/*
 * Copyright 2010-2025 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.export.utilities

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.symbols.KaClassSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedFunctionSymbol
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.StandardClassIds

public fun KaSession.isImplementsCloneable(symbol: KaClassSymbol): Boolean {
    return symbol.superTypes.any {
        it.expandedSymbol?.isCloneable ?: false
    }
}

public val KaClassSymbol.isCloneable: Boolean
    get() {
        return classId?.isCloneable ?: false
    }

public val ClassId.isCloneable: Boolean
    get() {
        return asSingleFqName() == StandardNames.FqNames.cloneable.toSafe()
    }

public fun KaSession.isClone(symbol: KaNamedFunctionSymbol): Boolean {
    val cloneCallableId = CallableId(StandardClassIds.Cloneable, Name.identifier("clone"))
    if (symbol.callableId == cloneCallableId) return true

    return symbol.allOverriddenSymbols.any { it.callableId == cloneCallableId }
}
