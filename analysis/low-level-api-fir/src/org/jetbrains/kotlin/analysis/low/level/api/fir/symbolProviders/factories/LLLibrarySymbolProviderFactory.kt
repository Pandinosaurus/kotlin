/*
 * Copyright 2010-2025 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.low.level.api.fir.symbolProviders.factories

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.analysis.api.platform.KotlinDeserializedDeclarationsOrigin
import org.jetbrains.kotlin.analysis.api.platform.KotlinPlatformSettings
import org.jetbrains.kotlin.analysis.low.level.api.fir.sessions.LLFirSession
import org.jetbrains.kotlin.fir.java.FirJavaFacade
import org.jetbrains.kotlin.fir.resolve.providers.FirSymbolProvider
import org.jetbrains.kotlin.load.kotlin.PackagePartProvider

/**
 * [LLLibrarySymbolProviderFactory] creates symbol providers in accordance with [KotlinPlatformSettings.deserializedDeclarationsOrigin].
 * Its implementations should be lightweight as the factory is neither a service nor cached.
 */
internal interface LLLibrarySymbolProviderFactory {
    fun createJvmLibrarySymbolProvider(
        session: LLFirSession,
        firJavaFacade: FirJavaFacade,
        packagePartProvider: PackagePartProvider,
        scope: GlobalSearchScope,
    ): List<FirSymbolProvider>

    fun createCommonLibrarySymbolProvider(
        session: LLFirSession,
        packagePartProvider: PackagePartProvider,
        scope: GlobalSearchScope,
    ): List<FirSymbolProvider>

    fun createNativeLibrarySymbolProvider(
        session: LLFirSession,
        scope: GlobalSearchScope,
    ): List<FirSymbolProvider>

    fun createJsLibrarySymbolProvider(
        session: LLFirSession,
        scope: GlobalSearchScope,
    ): List<FirSymbolProvider>

    fun createWasmLibrarySymbolProvider(
        session: LLFirSession,
        scope: GlobalSearchScope,
    ): List<FirSymbolProvider>

    /**
     * Creates a symbol provider for a [fallback builtins module][org.jetbrains.kotlin.analysis.api.projectStructure.KaBuiltinsModule].
     *
     * Since fallback builtins don't have any class ID ambiguities, their symbol providers don't have to implement
     * [LLPsiAwareSymbolProvider][org.jetbrains.kotlin.analysis.low.level.api.fir.symbolProviders.LLPsiAwareSymbolProvider].
     */
    fun createBuiltinsSymbolProvider(session: LLFirSession): List<FirSymbolProvider>

    companion object {
        fun fromSettings(project: Project): LLLibrarySymbolProviderFactory {
            val platformSettings = KotlinPlatformSettings.getInstance(project)
            return when (platformSettings.deserializedDeclarationsOrigin) {
                KotlinDeserializedDeclarationsOrigin.BINARIES -> LLBinaryOriginLibrarySymbolProviderFactory
                KotlinDeserializedDeclarationsOrigin.STUBS -> LLStubOriginLibrarySymbolProviderFactory
            }
        }
    }
}
