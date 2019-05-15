package _Self

import _Self.buildTypes.*
import _Self.vcsRoots.*
import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.Project

object Project : Project({
    description = "Kotlin Compiler, IDEA plugin and tests: mainline development branches"

    vcsRoot(Kotlin)
    vcsRoot(Kotlin_Ultimate)

    buildType(BootstrapTest_progressive)
    buildType(CompilerAndPlugin_as35)
    buildType(CompilerAndPlugin_191)
    buildType(CompilerAndPlugin_192)
    buildType(CompilerAndPlugin_182)
    buildType(DeployMavenArtifacts_Snapshots)
    buildType(AggregateBranch)
    buildType(DeployMavenArtifacts_Bootstrap)
    buildType(DeployMavenArtifacts)
    buildType(Compiler_1)
    buildType(CompilerAndPlugin_183)
    buildType(CodegenTestsOnDifferentJDKs)
    buildType(BootstrapTest)
    buildType(CompilerAndPluginForBenchmarks)
    buildType(CompilerAndPlugin_as33)
    buildType(CompilerAndPlugin_as34)
    buildType(CompilerAllPlugins)
    buildType(NewInferenceTests)
    buildType(BuildNumber)
    buildType(DeployMavenArtifacts_Nightly)
    buildType(Aggregate)
    buildType(GradleIntegrationTests)

    params {
        param("build.number.prefix", "1.3.50-dev")
        text("teamcity.activeVcsBranch.age.days", "5", display = ParameterDisplay.HIDDEN)
        param("env.JAVA_HOME", "%projectJDK%")
        param("projectJDK", "%env.JDK_18_x64%")
        text("vcs.settings.branch", "master", display = ParameterDisplay.HIDDEN, allowEmpty = false)
    }

    features {
        feature {
            id = "PROJECT_EXT_318_dev"
            type = "project-graphs"
            param("series", """
                [
                  {
                    "type": "valueType",
                    "title": "kotlin-stdlib",
                    "sourceBuildTypeId": "Kotlin_dev_Compiler",
                    "key": "DexMethodCount_kotlin-stdlib"
                  },
                  {
                    "type": "valueType",
                    "title": "kotlin-reflect",
                    "sourceBuildTypeId": "Kotlin_dev_Compiler",
                    "key": "DexMethodCount_kotlin-reflect"
                  }
                ]
            """.trimIndent())
            param("format", "integer")
            param("title", "Method count")
            param("seriesTitle", "Serie")
        }
    }

    cleanup {
        artifacts(builds = 1, days = 7, artifactPatterns = """
            +:**/*
            +:.teamcity/logs/**
        """.trimIndent())
    }
    buildTypesOrder = arrayListOf(Aggregate, AggregateBranch, BuildNumber, Compiler_1, CompilerAllPlugins, CompilerAndPlugin_191, CompilerAndPlugin_183, CompilerAndPlugin_182, CompilerAndPlugin_as34, CompilerAndPlugin_as35, CompilerAndPlugin_as33, CompilerAndPlugin_192, DeployMavenArtifacts, DeployMavenArtifacts_Snapshots, DeployMavenArtifacts_Bootstrap, DeployMavenArtifacts_Nightly, GradleIntegrationTests, CodegenTestsOnDifferentJDKs, BootstrapTest, BootstrapTest_progressive, NewInferenceTests, CompilerAndPluginForBenchmarks)
})
