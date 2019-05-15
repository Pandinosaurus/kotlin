package _Self.buildTypes

import _Self.vcsRoots.Kotlin
import _Self.vcsRoots.Kotlin_Ultimate
import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.buildFeatures.vcsLabeling
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.vcs

object Aggregate : BuildType({
    name = "Aggregate builds (master)"

    type = BuildTypeSettings.Type.COMPOSITE
    buildNumberPattern = "${BuildNumber.depParamRefs.buildNumber}"

    vcs {
        root(_Self.vcsRoots.Kotlin)
        root(_Self.vcsRoots.Kotlin_Ultimate, "+:. => kotlin-ultimate")

        checkoutMode = CheckoutMode.MANUAL
    }

    triggers {
        vcs {
            triggerRules = "-:ChangeLog.md"
            branchFilter = "+:<default>"
        }
    }

    features {
        vcsLabeling {
            vcsRootId = "${Kotlin.id}"
        }
        vcsLabeling {
            vcsRootId = "${Kotlin_Ultimate.id}"
        }
    }

    dependencies {
        snapshot(BootstrapTest) {
        }
        snapshot(BootstrapTest_progressive) {
        }
        snapshot(CodegenTestsOnDifferentJDKs) {
        }
        snapshot(Compiler_1) {
        }
        snapshot(CompilerAllPlugins) {
        }
        snapshot(CompilerAndPlugin_182) {
        }
        snapshot(CompilerAndPlugin_183) {
        }
        snapshot(CompilerAndPlugin_191) {
        }
        snapshot(CompilerAndPlugin_192) {
        }
        snapshot(CompilerAndPlugin_as33) {
        }
        snapshot(CompilerAndPlugin_as34) {
        }
        snapshot(CompilerAndPlugin_as35) {
        }
        snapshot(GradleIntegrationTests) {
        }
    }
})
