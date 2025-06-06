/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package androidx.compose.compiler.plugins.kotlin

import androidx.compose.compiler.plugins.kotlin.services.ComposeExtensionRegistrarConfigurator
import androidx.compose.compiler.plugins.kotlin.services.ComposePluginAnnotationsProvider
import org.jetbrains.kotlin.analysis.api.fir.test.configurators.AnalysisApiFirTestConfiguratorFactory.createConfigurator
import org.jetbrains.kotlin.analysis.api.impl.base.test.cases.components.compilerFacility.AbstractCompilerFacilityTest
import org.jetbrains.kotlin.analysis.test.framework.services.libraries.TestModuleCompiler.Directives.COMPILER_ARGUMENTS
import org.jetbrains.kotlin.analysis.test.framework.test.configurators.*
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.directives.ConfigurationDirectives.WITH_STDLIB
import org.jetbrains.kotlin.test.directives.DiagnosticsDirectives.RENDER_DIAGNOSTICS_FULL_TEXT
import org.jetbrains.kotlin.test.runners.AbstractPhasedJvmDiagnosticLightTreeTest
import java.io.File

abstract class AbstractCompilerFacilityTestForComposeCompilerPlugin : AbstractCompilerFacilityTest() {
    override val configurator: AnalysisApiTestConfigurator
        get() = createConfigurator(
            AnalysisApiTestConfiguratorFactoryData(
                FrontendKind.Fir,
                TestModuleKind.Source,
                AnalysisSessionMode.Normal,
                AnalysisApiMode.Ide
            )
        )

    override fun configureTest(builder: TestConfigurationBuilder) {
        super.configureTest(builder)
        builder.composeCompilerPluginConfiguration()
    }
}

open class AbstractPhasedJvmDiagnosticLightTreeForComposeTest : AbstractPhasedJvmDiagnosticLightTreeTest() {
    override fun configure(builder: TestConfigurationBuilder) {
        super.configure(builder)
        builder.defaultDirectives {
            +RENDER_DIAGNOSTICS_FULL_TEXT
            +WITH_STDLIB
        }

        builder.composeCompilerPluginConfiguration()
    }
}

fun TestConfigurationBuilder.composeCompilerPluginConfiguration() {
    defaultDirectives {
        COMPILER_ARGUMENTS with flagToEnableComposeCompilerPlugin()
    }

    useConfigurators(
        ::ComposeExtensionRegistrarConfigurator,
        ::ComposePluginAnnotationsProvider,
    )
}

private const val COMPOSE_COMPILER_PATH = "compose.compiler.hosted.jar.path"

private val composeCompilerPath by lazy {
    System.getProperty(COMPOSE_COMPILER_PATH) ?: error("System property \"$COMPOSE_COMPILER_PATH\" is not found")
}

private fun flagToEnableComposeCompilerPlugin(): String {
    val libFile = File(composeCompilerPath)
    if (!libFile.exists()) {
        error("No file \"$composeCompilerPath\" is found")
    }
    return "-Xplugin=${libFile.absolutePath}"
}
