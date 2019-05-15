package _Self.buildTypes

import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.buildFeatures.Swabra
import jetbrains.buildServer.configs.kotlin.v2018_2.buildFeatures.freeDiskSpace
import jetbrains.buildServer.configs.kotlin.v2018_2.buildFeatures.perfmon
import jetbrains.buildServer.configs.kotlin.v2018_2.buildFeatures.swabra
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.finishBuildTrigger

object GradleIntegrationTests : BuildType({
    name = "Gradle Integration Tests"

    artifactRules = """
        **/build/reports/tests/**=>internal/test_results.zip
        **/build/test-results/**=>internal/test_results.zip
        **/hs_err*.log => internal
    """.trimIndent()
    buildNumberPattern = "%build.number.default%"

    params {
        param("gradleParameters", "--info --full-stacktrace")
        text("requirement.jdk16", "%env.JDK_16%", display = ParameterDisplay.HIDDEN)
        text("requirement.jdk18", "%env.JDK_18%", display = ParameterDisplay.HIDDEN)
        text("requirement.jdk17", "%env.JDK_17%", display = ParameterDisplay.HIDDEN)
        param("build.number.default", "${BuildNumber.depParamRefs.buildNumber}")
        param("mavenParameters", "")
        param("system.maven.repo.local", "%teamcity.build.checkoutDir%/dist/local-repo")
    }

    vcs {
        root(_Self.vcsRoots.Kotlin)

        checkoutMode = CheckoutMode.ON_AGENT
    }

    steps {
        gradle {
            name = "Build and install artifacts to local maven repo"
            tasks = "clean install"
            buildFile = "build.gradle.kts"
            gradleParams = "%gradleParameters%"
            jdkHome = "%projectJDK%"
        }
        gradle {
            name = "Gradle plugin integration tests"
            tasks = "gradlePluginIntegrationTest"
            buildFile = "build.gradle.kts"
            gradleParams = "%gradleParameters%"
            jdkHome = "%projectJDK%"
        }
    }

    triggers {
        finishBuildTrigger {
            buildType = "${BuildNumber.id}"
            successfulOnly = true
            branchFilter = "+:rr/gradle/*"
        }
    }

    failureConditions {
        executionTimeoutMin = 300
    }

    features {
        freeDiskSpace {
            requiredSpace = "10gb"
            failBuild = false
        }
        perfmon {
        }
        swabra {
            lockingProcesses = Swabra.LockingProcessPolicy.KILL
        }
    }

    dependencies {
        snapshot(BuildNumber) {
        }
    }

    requirements {
        noLessThan("teamcity.agent.work.dir.freeSpaceMb", "1000")
        noLessThan("teamcity.agent.hardware.memorySizeMb", "7000")
    }
})
