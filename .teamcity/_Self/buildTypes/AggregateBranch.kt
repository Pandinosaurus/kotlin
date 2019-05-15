package _Self.buildTypes

import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.vcs

object AggregateBranch : BuildType({
    name = "Aggregate builds (rr/*)"

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
            branchFilter = """
                +:rr/*
                +:rrg/*
            """.trimIndent()
        }
    }

    dependencies {
        snapshot(BootstrapTest) {
        }
        snapshot(BootstrapTest_progressive) {
        }
        snapshot(Compiler_1) {
        }
        snapshot(CompilerAllPlugins) {
        }
        snapshot(CompilerAndPlugin_191) {
        }
        snapshot(CompilerAndPlugin_as33) {
        }
    }
})
