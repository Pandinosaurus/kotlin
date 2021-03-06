/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.declarations.utils

import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.fir.symbols.impl.*

// ---------------------- callables with status ----------------------

inline val FirCallableSymbol<*>.modality: Modality? get() = resolvedStatus.modality
inline val FirCallableSymbol<*>.isAbstract: Boolean get() = resolvedStatus.modality == Modality.ABSTRACT
inline val FirCallableSymbol<*>.isOpen: Boolean get() = resolvedStatus.modality == Modality.OPEN
inline val FirCallableSymbol<*>.isFinal: Boolean
    get() {
        // member with unspecified modality is final
        val modality = resolvedStatus.modality ?: return true
        return modality == Modality.FINAL
    }

inline val FirCallableSymbol<*>.visibility: Visibility get() = resolvedStatus.visibility
inline val FirCallableSymbol<*>.effectiveVisibility: EffectiveVisibility get() = resolvedStatus.effectiveVisibility

inline val FirCallableSymbol<*>.allowsToHaveFakeOverride: Boolean
    get() = !Visibilities.isPrivate(visibility) && visibility != Visibilities.InvisibleFake

inline val FirCallableSymbol<*>.isActual: Boolean get() = resolvedStatus.isActual
inline val FirCallableSymbol<*>.isExpect: Boolean get() = resolvedStatus.isExpect
inline val FirCallableSymbol<*>.isInner: Boolean get() = resolvedStatus.isInner
inline val FirCallableSymbol<*>.isStatic: Boolean get() = resolvedStatus.isStatic
inline val FirCallableSymbol<*>.isOverride: Boolean get() = resolvedStatus.isOverride
inline val FirCallableSymbol<*>.isOperator: Boolean get() = resolvedStatus.isOperator
inline val FirCallableSymbol<*>.isInfix: Boolean get() = resolvedStatus.isInfix
inline val FirCallableSymbol<*>.isInline: Boolean get() = resolvedStatus.isInline
inline val FirCallableSymbol<*>.isTailRec: Boolean get() = resolvedStatus.isTailRec
inline val FirCallableSymbol<*>.isExternal: Boolean get() = resolvedStatus.isExternal
inline val FirCallableSymbol<*>.isSuspend: Boolean get() = resolvedStatus.isSuspend
inline val FirCallableSymbol<*>.isConst: Boolean get() = resolvedStatus.isConst
inline val FirCallableSymbol<*>.isLateInit: Boolean get() = resolvedStatus.isLateInit
inline val FirCallableSymbol<*>.isFromSealedClass: Boolean get() = resolvedStatus.isFromSealedClass
inline val FirCallableSymbol<*>.isFromEnumClass: Boolean get() = resolvedStatus.isFromEnumClass
inline val FirCallableSymbol<*>.isFun: Boolean get() = resolvedStatus.isFun

// ---------------------- regular with status ----------------------

inline val FirRegularClassSymbol.modality: Modality? get() = resolvedStatus.modality
inline val FirRegularClassSymbol.isAbstract: Boolean get() = resolvedStatus.modality == Modality.ABSTRACT
inline val FirRegularClassSymbol.isOpen: Boolean get() = resolvedStatus.modality == Modality.OPEN
inline val FirRegularClassSymbol.isFinal: Boolean
    get() {
        // member with unspecified modality is final
        val modality = resolvedStatus.modality ?: return true
        return modality == Modality.FINAL
    }

inline val FirRegularClassSymbol.visibility: Visibility get() = resolvedStatus.visibility
inline val FirRegularClassSymbol.effectiveVisibility: EffectiveVisibility get() = resolvedStatus.effectiveVisibility

inline val FirRegularClassSymbol.isActual: Boolean get() = resolvedStatus.isActual
inline val FirRegularClassSymbol.isExpect: Boolean get() = resolvedStatus.isExpect
inline val FirRegularClassSymbol.isInner: Boolean get() = resolvedStatus.isInner
inline val FirRegularClassSymbol.isStatic: Boolean get() = resolvedStatus.isStatic
inline val FirRegularClassSymbol.isInline: Boolean get() = resolvedStatus.isInline
inline val FirRegularClassSymbol.isExternal: Boolean get() = resolvedStatus.isExternal
inline val FirRegularClassSymbol.isFromSealedClass: Boolean get() = resolvedStatus.isFromSealedClass
inline val FirRegularClassSymbol.isFromEnumClass: Boolean get() = resolvedStatus.isFromEnumClass
inline val FirRegularClassSymbol.isFun: Boolean get() = resolvedStatus.isFun
inline val FirRegularClassSymbol.isCompanion: Boolean get() = resolvedStatus.isCompanion
inline val FirRegularClassSymbol.isData: Boolean get() = resolvedStatus.isData
inline val FirRegularClassSymbol.isSealed: Boolean get() = resolvedStatus.modality == Modality.SEALED

inline val FirRegularClassSymbol.canHaveAbstractDeclaration: Boolean
    get() = isAbstract || isSealed || isEnumClass

// ---------------------- type aliases with status ----------------------

inline val FirTypeAliasSymbol.modality: Modality? get() = resolvedStatus.modality
inline val FirTypeAliasSymbol.isAbstract: Boolean get() = resolvedStatus.modality == Modality.ABSTRACT
inline val FirTypeAliasSymbol.isOpen: Boolean get() = resolvedStatus.modality == Modality.OPEN
inline val FirTypeAliasSymbol.isFinal: Boolean
    get() {
        // member with unspecified modality is final
        val modality = resolvedStatus.modality ?: return true
        return modality == Modality.FINAL
    }

inline val FirTypeAliasSymbol.visibility: Visibility get() = resolvedStatus.visibility
inline val FirTypeAliasSymbol.effectiveVisibility: EffectiveVisibility
    get() = resolvedStatus.effectiveVisibility

inline val FirTypeAliasSymbol.isActual: Boolean get() = resolvedStatus.isActual
inline val FirTypeAliasSymbol.isExpect: Boolean get() = resolvedStatus.isExpect
inline val FirTypeAliasSymbol.isInner: Boolean get() = resolvedStatus.isInner
inline val FirTypeAliasSymbol.isStatic: Boolean get() = resolvedStatus.isStatic
inline val FirTypeAliasSymbol.isInline: Boolean get() = resolvedStatus.isInline
inline val FirTypeAliasSymbol.isExternal: Boolean get() = resolvedStatus.isExternal
inline val FirTypeAliasSymbol.isFromSealedClass: Boolean get() = resolvedStatus.isFromSealedClass
inline val FirTypeAliasSymbol.isFromEnumClass: Boolean get() = resolvedStatus.isFromEnumClass
inline val FirTypeAliasSymbol.isFun: Boolean get() = resolvedStatus.isFun

// ---------------------- common classes ----------------------

inline val FirClassLikeSymbol<*>.isLocal: Boolean get() = classId.isLocal

inline val FirClassSymbol<*>.isLocalClassOrAnonymousObject: Boolean
    get() = classId.isLocal || this is FirAnonymousObjectSymbol

inline val FirClassSymbol<*>.isInterface: Boolean
    get() = classKind.isInterface

inline val FirClassSymbol<*>.isEnumClass: Boolean
    get() = classKind.isEnumClass

// ---------------------- specific callables ----------------------

inline val FirPropertyAccessorSymbol.allowsToHaveFakeOverride: Boolean get() = visibility.allowsToHaveFakeOverride

inline val FirPropertySymbol.allowsToHaveFakeOverride: Boolean get() = visibility.allowsToHaveFakeOverride

inline val FirNamedFunctionSymbol.isLocal: Boolean get() = visibility == Visibilities.Local
