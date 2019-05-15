package _Self.buildTypes

import _Self.vcsRoots.Kotlin
import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.buildFeatures.Swabra
import jetbrains.buildServer.configs.kotlin.v2018_2.buildFeatures.freeDiskSpace
import jetbrains.buildServer.configs.kotlin.v2018_2.buildFeatures.perfmon
import jetbrains.buildServer.configs.kotlin.v2018_2.buildFeatures.swabra
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.ant
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.script

object CompilerAndPlugin_191 : BuildType({
    name = "IDEA Plugin Tests (191)"

    artifactRules = """
        build/repo => maven
        **/build/reports/tests/**=>internal/test_results.zip
        **/build/test-results/**=>internal/test_results.zip
        **/kotlin.test/js/it/build/*.log=>internal/test_results.zip
        **/build.log=>internal/test_results.zip
        **/hs_err*.log => internal
        **/*-method-count.txt => internal
        **/*.hprof => internal/hprof.zip
        build/internal/repo => internal/repo
    """.trimIndent()
    buildNumberPattern = "%build.number.default%"

    params {
        param("gradleParameters", "--info --full-stacktrace")
        param("kotlin.plugin.patch.number", "1")
        text("requirement.jdk16", "%env.JDK_16%", display = ParameterDisplay.HIDDEN)
        param("DeployVersion", "%build.number%")
        text("requirement.jdk18", "%env.JDK_18%", display = ParameterDisplay.HIDDEN)
        param("system.deployVersion", "%DeployVersion%")
        text("requirement.jdk17", "%env.JDK_17%", display = ParameterDisplay.HIDDEN)
        param("system.kotlin.suppress.expected.test.failures", "true")
        param("build.number.default", "${BuildNumber.depParamRefs.buildNumber}")
        param("mavenParameters", "")
    }

    vcs {
        root(_Self.vcsRoots.Kotlin)

        checkoutMode = CheckoutMode.ON_AGENT
        cleanCheckout = true
    }

    steps {
        script {
            name = "Set up Git"
            scriptContent = """
                "%env.TEAMCITY_GIT_PATH%" -C "%teamcity.build.checkoutDir%" config user.email teamcity-demo-noreply@jetbrains.com
                "%env.TEAMCITY_GIT_PATH%" -C "%teamcity.build.checkoutDir%" config user.name TeamCity
            """.trimIndent()
        }
        ant {
            name = "Download Bunch"
            mode = antScript {
                content = """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <project name="download-bunch" default="download-and-unzip">
                    	<target name="download-and-unzip">
                    		<get
                    		        src="https://cache-redirector.jetbrains.com/jitpack.io/com/github/JetBrains/bunches/v0.9.0/bunches-v0.9.0.zip"
                    			dest="bunch-0.9.0.zip" />
                    		<unzip src="bunch-0.9.0.zip" dest="." />
                    		<chmod dir="bunch-0.9.0/bin" includes="**/*" perm="ugo+rx"/>
                    	</target>
                    </project>
                """.trimIndent()
            }
        }
        gradle {
            name = "Core libraries and compiler dist"
            tasks = "dist"
            buildFile = "build.gradle.kts"
            gradleParams = "%gradleParameters%"
            jdkHome = "%projectJDK%"
        }
        script {
            name = "Bunch 191 switch (kotlin)"
            scriptContent = """bunch-0.9.0/bin/bunch switch "%teamcity.build.checkoutDir%" 191 --cleanup"""
        }
        gradle {
            name = "Clean dist/artifacts folder"
            tasks = "cleanupArtifacts"
            buildFile = "build.gradle.kts"
            gradleParams = "%gradleParameters%"
            jdkHome = "%projectJDK%"
        }
        gradle {
            name = "Plugin for IJ2019.1"
            tasks = "writePluginVersion ideaPlugin :ultimate:ideaUltimatePlugin"
            buildFile = "build.gradle.kts"
            gradleParams = "%gradleParameters% -PpluginVersion=%build.number%-IJ2019.1-%kotlin.plugin.patch.number% -x dist"
            jdkHome = "%projectJDK%"
        }
        gradle {
            name = "Plugin tests for IJ2019.1"
            tasks = "ideaPluginTest :ultimate:ideaUltimatePluginTest"
            buildFile = "build.gradle.kts"
            gradleParams = "%gradleParameters% --continue"
            jdkHome = "%projectJDK%"
        }
        script {
            name = "Revert 191 patchset (kotlin)"
            scriptContent = """"%env.TEAMCITY_GIT_PATH%" -C "%teamcity.build.checkoutDir%" reset --hard ${Kotlin.paramRefs.buildVcsNumber}"""
        }
    }

    failureConditions {
        executionTimeoutMin = 300
    }

    features {
        freeDiskSpace {
            requiredSpace = "5900mb"
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
            onDependencyFailure = FailureAction.FAIL_TO_START
        }
    }

    requirements {
        noLessThan("teamcity.agent.hardware.memorySizeMb", "7000")
    }
})
