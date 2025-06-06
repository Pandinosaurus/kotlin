/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.klib.reader.tests

import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.klib.reader.testUtils.providedTestProjectKlib
import org.jetbrains.kotlin.analysis.api.standalone.buildStandaloneAnalysisAPISession
import org.jetbrains.kotlin.analysis.api.symbols.KaClassKind
import org.jetbrains.kotlin.analysis.api.symbols.KaPropertySymbol
import org.jetbrains.kotlin.analysis.api.symbols.nameOrAnonymous
import org.jetbrains.kotlin.analysis.api.projectStructure.KaLibraryModule
import org.jetbrains.kotlin.analysis.api.projectStructure.KaSourceModule
import org.jetbrains.kotlin.analysis.project.structure.builder.buildKtLibraryModule
import org.jetbrains.kotlin.analysis.project.structure.builder.buildKtSourceModule
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.native.analysis.api.*
import org.jetbrains.kotlin.platform.konan.NativePlatforms
import kotlin.io.path.nameWithoutExtension
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.fail

class GetSymbolsTest {
    @Test
    fun `test - getClassOrObjectSymbol - RootPkgClass`() {
        withTestProjectLibraryAnalysisSession { libraryModule ->
            val addresses = libraryModule.readKlibDeclarationAddresses()
                ?: error("Failed reading declaration addresses")

            val rootPkgClassAddress = addresses.filterIsInstance<KlibClassAddress>()
                .first { it.classId == ClassId.fromString("RootPkgClass") }


            val rootPkgClassSymbol = assertNotNull(rootPkgClassAddress.getClassOrObjectSymbol())

            assertEquals("RootPkgClass", rootPkgClassSymbol.nameOrAnonymous.asString())
        }
    }

    @Test
    fun `test - getClassOrObjectSymbol - AObject`() {
        withTestProjectLibraryAnalysisSession { libraryModule ->
            val addresses = libraryModule.readKlibDeclarationAddresses()
                ?: error("Failed reading declaration addresses")

            val aObjectAddress = addresses.filterIsInstance<KlibClassAddress>()
                .first { it.classId == ClassId.fromString("org/jetbrains/sample/a/AObject") }


            val aObjectSymbol = assertNotNull(aObjectAddress.getClassOrObjectSymbol())

            assertEquals("AObject", aObjectSymbol.nameOrAnonymous.asString())
            assertEquals(KaClassKind.OBJECT, aObjectSymbol.classKind)
        }
    }

    @Test
    fun `test - getSymbols - rootPkgProperty`() {
        withTestProjectLibraryAnalysisSession { libraryModule ->
            val addresses = libraryModule.readKlibDeclarationAddresses()
                ?: error("Failed reading declaration addresses")

            val rootPkgPropertyAddress = addresses.filterIsInstance<KlibPropertyAddress>()
                .first { it.callableName == Name.identifier("rootPkgProperty") }

            val rootPkgPropertySymbols = rootPkgPropertyAddress.getSymbols().toList()
            if (rootPkgPropertySymbols.size != 1) fail("Expected only a single 'rootPkgProperty' symbol. Found $rootPkgPropertySymbols")

            /* Check getSymbols and getPropertySymbols behave the same */
            assertEquals(
                rootPkgPropertySymbols,
                rootPkgPropertyAddress.getPropertySymbols().toList(),
                "Expected 'getSymbols' and 'getPropertySymbols' to be equal"
            )

            val symbol = rootPkgPropertySymbols.single() as KaPropertySymbol
            assertEquals("rootPkgProperty", symbol.name.asString())
        }
    }

    @Test
    fun `test - getSymbols - aFunction`() {
        withTestProjectLibraryAnalysisSession { libraryModule ->
            val addresses = libraryModule.readKlibDeclarationAddresses()
                ?: error("Failed reading declaration addresses")

            val aFunctionAddress = addresses.filterIsInstance<KlibFunctionAddress>()
                .first { it.callableName == Name.identifier("aFunction") }

            val aFunctionSymbols = aFunctionAddress.getFunctionSymbols().toList()
            if (aFunctionSymbols.size != 2) fail("Expected exactly 2 'aFunction' symbols. Found $aFunctionSymbols")

            /* Check: getFunctionSymbols and getSymbols behave the same */
            assertEquals(
                aFunctionSymbols,
                aFunctionAddress.getSymbols().toList(),
                "Expected 'getSymbols' and 'getFunctionSymbols' to be equal"
            )

            aFunctionSymbols.forEach { symbol ->
                assertEquals("aFunction", symbol.name.asString())
            }

            /* Check: One function does not have any value params, the other function has a single param with name 'seed' */
            assertEquals(
                setOf(emptyList(), listOf("seed")), aFunctionSymbols.map { it.valueParameters.map { it.name.asString() } }.toSet()
            )
        }
    }

    @Test
    fun `test - getTypeAlias - TypeAliasA`() {
        withTestProjectLibraryAnalysisSession { libraryModule ->
            val addresses = libraryModule.readKlibDeclarationAddresses() ?: fail("Failed reading addresses")
            val typeAliasAAddress = addresses.filterIsInstance<KlibTypeAliasAddress>()
                .find { it.classId == ClassId.fromString("org/jetbrains/sample/TypeAliasA") }
                ?: fail("Could not find TypeAliasA")

            val typeAliasASymbol = assertNotNull(typeAliasAAddress.getTypeAliasSymbol())
            assertEquals(Name.identifier("TypeAliasA"), typeAliasASymbol.name)
            assertEquals(Name.identifier("AClass"), typeAliasASymbol.expandedType.expandedSymbol?.name)
        }
    }

    @Test
    @OptIn(KaExperimentalApi::class)
    fun `test - filePrivateSymbolsClash - function`() {
        withTestProjectLibraryAnalysisSession { libraryModule ->
            val addresses = libraryModule.readKlibDeclarationAddresses() ?: fail("Failed reading addresses")
            val clashingAddresses = addresses.filterIsInstance<KlibFunctionAddress>()
                .filter { it.callableName == Name.identifier("foo") }

            val fooInAKt = clashingAddresses.find { it.sourceFileName == "A.kt" } ?: fail("Missing `fun foo()` in A.kt")
            val fooInBKt = clashingAddresses.find { it.sourceFileName == "B.kt" } ?: fail("Missing `fun foo()` in B.kt")

            val fooInASymbols = fooInAKt.getFunctionSymbols().toList()
            if (fooInASymbols.size != 1) fail(
                "Expected exactly one 'foo' symbol in A.kt. Found ${fooInASymbols.joinToString { it.render() }}"
            )

            val fooInBSymbols = fooInBKt.getFunctionSymbols().toList()
            if (fooInASymbols.size != 1) fail(
                "Expected exactly one 'foo' symbol in B.kt. Found ${fooInBSymbols.joinToString { it.render() }}"
            )

            val fooInASymbol = fooInASymbols.first()
            if (ClassId.fromString("org/jetbrains/sample/filePrivateSymbolsClash/A") !in fooInASymbol.annotations)
                fail("Missing annotation 'A' on 'fun foo()' in A.kt")

            val fooInBSymbol = fooInBSymbols.first()
            if (ClassId.fromString("org/jetbrains/sample/filePrivateSymbolsClash/B") !in fooInBSymbol.annotations)
                fail("Missing annotation 'B' on 'fun foo()' in B.kt")
        }
    }

    @Test
    @OptIn(KaExperimentalApi::class)
    fun `test - filePrivateSymbolsClash - property`() {
        withTestProjectLibraryAnalysisSession { libraryModule ->
            val addresses = libraryModule.readKlibDeclarationAddresses() ?: fail("Failed reading addresses")
            val clashingAddresses = addresses.filterIsInstance<KlibPropertyAddress>()
                .filter { it.callableName == Name.identifier("fooProperty") }

            val fooInAKt = clashingAddresses.find { it.sourceFileName == "A.kt" } ?: fail("Missing `val fooProperty` in A.kt")
            val fooInBKt = clashingAddresses.find { it.sourceFileName == "B.kt" } ?: fail("Missing `val fooProperty` in B.kt")

            val fooInASymbols = fooInAKt.getPropertySymbols().toList()
            if (fooInASymbols.size != 1) fail(
                "Expected exactly one 'fooProperty' symbol in A.kt. Found ${fooInASymbols.joinToString { it.render() }}"
            )

            val fooInBSymbols = fooInBKt.getPropertySymbols().toList()
            if (fooInASymbols.size != 1) fail(
                "Expected exactly one 'fooProperty' symbol in B.kt. Found ${fooInBSymbols.joinToString { it.render() }}"
            )

            val fooInASymbol = fooInASymbols.first()
            if (ClassId.fromString("org/jetbrains/sample/filePrivateSymbolsClash/A") !in fooInASymbol.annotations)
                fail("Missing annotation 'A' on 'val fooProperty' in A.kt")

            val fooInBSymbol = fooInBSymbols.first()
            if (ClassId.fromString("org/jetbrains/sample/filePrivateSymbolsClash/B") !in fooInBSymbol.annotations)
                fail("Missing annotation 'B' on 'val fooProperty' in B.kt")
        }
    }

    /**
     * Runs the given [block] in an analysis session that will have the built library in its dependencies.
     */
    @Suppress("CONTEXT_RECEIVERS_DEPRECATED")
    private fun <T> withTestProjectLibraryAnalysisSession(block: KaSession.(KaLibraryModule) -> T): T {
        var mainModule: KaSourceModule? = null
        var libraryModule: KaLibraryModule? = null

        buildStandaloneAnalysisAPISession {
            val currentArchitectureTarget = HostManager.host
            val nativePlatform = NativePlatforms.nativePlatformByTargets(listOf(currentArchitectureTarget))

            buildKtModuleProvider {
                platform = nativePlatform
                libraryModule = addModule(
                    buildKtLibraryModule {
                        addBinaryRoot(providedTestProjectKlib)
                        platform = nativePlatform
                        libraryName = providedTestProjectKlib.nameWithoutExtension
                    }
                )

                mainModule = addModule(
                    buildKtSourceModule {
                        moduleName = "main"
                        platform = nativePlatform
                        addRegularDependency(libraryModule)
                    }
                )
            }
        }

        require(mainModule != null && libraryModule != null) {
            "Failed to build a Standalone session with a main source module and a library module."
        }

        // We have to analyze the KLIB from a source use-site module because `KaLibraryModule`s aren't supported as use sites (KT-76042).
        return analyze(mainModule) {
            block(this, libraryModule)
        }
    }
}
