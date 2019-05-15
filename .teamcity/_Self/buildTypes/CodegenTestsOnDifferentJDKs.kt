package _Self.buildTypes

import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.buildFeatures.Swabra
import jetbrains.buildServer.configs.kotlin.v2018_2.buildFeatures.freeDiskSpace
import jetbrains.buildServer.configs.kotlin.v2018_2.buildFeatures.perfmon
import jetbrains.buildServer.configs.kotlin.v2018_2.buildFeatures.swabra
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.gradle

object CodegenTestsOnDifferentJDKs : BuildType({
    name = "Test Codegen on different platforms (JDKs/Android)"

    artifactRules = """
        **/build/reports/tests/**=>internal/test_results.zip
        **/build/test-results/**=>internal/test_results.zip
        **/hs_err*.log => internal
    """.trimIndent()
    buildNumberPattern = "%build.number.default%"

    params {
        param("build.number.default", "${BuildNumber.depParamRefs.buildNumber}")
        param("gradleParameters", "--info --full-stacktrace")
        text("requirement.jdk16", "%env.JDK_16%", display = ParameterDisplay.HIDDEN)
        text("requirement.jdk18", "%env.JDK_18%", display = ParameterDisplay.HIDDEN)
        param("requirement.jdk9", "%env.JDK_19%")
        text("requirement.jdk17", "%env.JDK_17%", display = ParameterDisplay.HIDDEN)
    }

    vcs {
        root(_Self.vcsRoots.Kotlin)

        checkoutMode = CheckoutMode.ON_AGENT
        cleanCheckout = true
    }

    steps {
        gradle {
            name = "Codegen for jvmTarget=6, run tests on JVM 6"
            executionMode = BuildStep.ExecutionMode.RUN_ON_FAILURE
            tasks = ":compiler:tests-different-jdk:codegenTarget6Jvm6Test"
            buildFile = "build.gradle.kts"
            gradleParams = "%gradleParameters%"
            jdkHome = "%projectJDK%"
        }
        gradle {
            name = "Codegen for jvmTarget=8, run tests on JVM 8"
            executionMode = BuildStep.ExecutionMode.RUN_ON_FAILURE
            tasks = ":compiler:tests-different-jdk:codegenTarget8Jvm8Test"
            buildFile = "build.gradle.kts"
            gradleParams = "%gradleParameters%"
            jdkHome = "%projectJDK%"
        }
        gradle {
            name = "Codegen for jvmTarget=6, run tests on JVM 9"
            executionMode = BuildStep.ExecutionMode.RUN_ON_FAILURE
            tasks = ":compiler:tests-different-jdk:codegenTarget6Jvm9Test"
            buildFile = "build.gradle.kts"
            gradleParams = "%gradleParameters%"
            jdkHome = "%projectJDK%"
        }
        gradle {
            name = "Codegen for jvmTarget=8, run tests on JVM 9"
            executionMode = BuildStep.ExecutionMode.RUN_ON_FAILURE
            tasks = ":compiler:tests-different-jdk:codegenTarget8Jvm9Test"
            buildFile = "build.gradle.kts"
            gradleParams = "%gradleParameters%"
            jdkHome = "%projectJDK%"
        }
        gradle {
            name = "Codegen for jvmTarget=9, run tests on JVM 9"
            executionMode = BuildStep.ExecutionMode.RUN_ON_FAILURE
            tasks = ":compiler:tests-different-jdk:codegenTarget9Jvm9Test"
            buildFile = "build.gradle.kts"
            gradleParams = "%gradleParameters%"
            jdkHome = "%projectJDK%"
        }
        gradle {
            name = "Codegen for Android"
            executionMode = BuildStep.ExecutionMode.RUN_ON_FAILURE
            tasks = ":compiler:android-tests:test"
            buildFile = "build.gradle.kts"
            gradleParams = "%gradleParameters%"
            jdkHome = "%projectJDK%"
        }
    }

    failureConditions {
        executionTimeoutMin = 180
    }

    features {
        freeDiskSpace {
            requiredSpace = "10gb"
            failBuild = true
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
        noLessThan("teamcity.agent.hardware.memorySizeMb", "7500")
    }
})
