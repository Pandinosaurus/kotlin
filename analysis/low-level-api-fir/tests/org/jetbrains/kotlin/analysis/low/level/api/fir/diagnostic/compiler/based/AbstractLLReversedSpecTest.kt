/*
 * Copyright 2010-2025 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.low.level.api.fir.diagnostic.compiler.based

import org.jetbrains.kotlin.analysis.low.level.api.fir.LLFirOnlyReversedTestSuppressor
import org.jetbrains.kotlin.analysis.low.level.api.fir.compiler.based.AbstractLLCompilerBasedTest
import org.jetbrains.kotlin.analysis.low.level.api.fir.diagnostic.compiler.based.facades.LLFirAnalyzerFacadeFactoryWithPreresolveInReversedOrder
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.configuration.baseFirDiagnosticTestConfiguration
import org.jetbrains.kotlin.utils.bind

abstract class AbstractLLReversedSpecTest : AbstractLLCompilerBasedTest() {
    override fun configure(builder: TestConfigurationBuilder) {
        with(builder) {
            baseFirDiagnosticTestConfiguration(
                frontendFacade = ::LowLevelFirFrontendFacade.bind(LLFirAnalyzerFacadeFactoryWithPreresolveInReversedOrder),
                testDataConsistencyHandler = ::ReversedFirIdenticalChecker,
            )

            baseFirSpecDiagnosticTestConfigurationForIde()
            useAfterAnalysisCheckers(::LLFirOnlyReversedTestSuppressor)
            useMetaTestConfigurators(::reversedDiagnosticsConfigurator)
        }
    }
}
