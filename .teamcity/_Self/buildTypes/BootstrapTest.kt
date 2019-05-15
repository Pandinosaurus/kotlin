package _Self.buildTypes

import _Self.vcsRoots.Kotlin
import _Self.vcsRoots.Kotlin_Ultimate
import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.buildFeatures.Swabra
import jetbrains.buildServer.configs.kotlin.v2018_2.buildFeatures.freeDiskSpace
import jetbrains.buildServer.configs.kotlin.v2018_2.buildFeatures.perfmon
import jetbrains.buildServer.configs.kotlin.v2018_2.buildFeatures.swabra
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.ant
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.script

object BootstrapTest : BuildType({
    name = "Test Compiler Bootstrapping"

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
        param("DeployVersion", "%build.number%")
        param("bootstrapTestTasks", "coreLibsTest")
        param("build.number.default", "${Compiler_1.depParamRefs.buildNumber}-bootstrap")
        param("mavenParameters", "")
        param("system.idl2k.deploy", "true")
        param("system.bootstrap.teamcity.kotlin.version", "${Compiler_1.depParamRefs["DeployVersion"]}")
        param("system.bootstrap.teamcity.build.number", "${Compiler_1.depParamRefs.buildNumber}")
        text("requirement.jdk16", "%env.JDK_16%", display = ParameterDisplay.HIDDEN)
        text("requirement.jdk18", "%env.JDK_18%", display = ParameterDisplay.HIDDEN)
        param("system.deployVersion", "%DeployVersion%")
        text("requirement.jdk17", "%env.JDK_17%", display = ParameterDisplay.HIDDEN)
        param("system.kotlin.suppress.expected.test.failures", "true")
        param("system.bootstrap.teamcity.project", "Kotlin_dev_Compiler")
    }

    vcs {
        root(_Self.vcsRoots.Kotlin)
        root(_Self.vcsRoots.Kotlin_Ultimate, "+:. => kotlin-ultimate")

        checkoutMode = CheckoutMode.ON_AGENT
        cleanCheckout = true
    }

    steps {
        script {
            name = "Set up Git"
            scriptContent = """
                "%env.TEAMCITY_GIT_PATH%" -C "%teamcity.build.checkoutDir%" config user.email teamcity-demo-noreply@jetbrains.com
                "%env.TEAMCITY_GIT_PATH%" -C "%teamcity.build.checkoutDir%" config user.name TeamCity
                "%env.TEAMCITY_GIT_PATH%" -C "%teamcity.build.checkoutDir%/kotlin-ultimate" config user.email teamcity-demo-noreply@jetbrains.com
                "%env.TEAMCITY_GIT_PATH%" -C "%teamcity.build.checkoutDir%/kotlin-ultimate" config user.name TeamCity
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
        maven {
            name = "Get private local maven repo path"
            executionMode = BuildStep.ExecutionMode.RUN_ON_FAILURE
            goals = ""
            pomLocation = "libraries/pom.xml"
            runnerArgs = "-version"
            mavenVersion = bundled_3_3()
            useOwnLocalRepo = true
            jdkHome = "%projectJDK%"
        }
        gradle {
            name = "Install artifacts to local maven repo"
            executionMode = BuildStep.ExecutionMode.RUN_ON_FAILURE
            tasks = "install"
            buildFile = "build.gradle.kts"
            gradleParams = "%gradleParameters% -Dmaven.repo.local=%teamcity.maven.local.repository.path%"
            jdkHome = "%projectJDK%"
        }
        script {
            name = "Bunch 191 switch (kotlin)"
            scriptContent = """bunch-0.9.0/bin/bunch switch "%teamcity.build.checkoutDir%" 191 --cleanup"""
        }
        script {
            name = "Bunch cidr191 switch (kotlin-ultimate)"
            scriptContent = """bunch-0.9.0/bin/bunch switch "%teamcity.build.checkoutDir%/kotlin-ultimate" cidr191 --cleanup"""
        }
        gradle {
            name = "Clean dist/artifacts folder"
            tasks = "cleanupArtifacts"
            buildFile = "build.gradle.kts"
            gradleParams = "%gradleParameters%"
            jdkHome = "%projectJDK%"
        }
        gradle {
            name = "Plugin for IJ2019.1 + CIDR2019.1"
            tasks = "writePluginVersion ideaPlugin :ultimate:ideaUltimatePlugin clionPlugin appcodePlugin"
            buildFile = "build.gradle.kts"
            gradleParams = "%gradleParameters% -PpluginVersion=%build.number%-IJ2019.1-%kotlin.plugin.patch.number% -PcidrPluginsEnabled=true -PclionPluginNumber=%kotlin.plugin.patch.number% -PappcodePluginNumber=%kotlin.plugin.patch.number% -x dist"
            jdkHome = "%projectJDK%"
            jvmArgs = "-Djavax.net.ssl.keyStore=/usr/lib/jvm/java-8-oracle/jre/lib/security/cacerts -Djavax.net.ssl.keyStorePassword=changeit"
        }
        script {
            name = "Revert 191 patchset (kotlin)"
            scriptContent = """"%env.TEAMCITY_GIT_PATH%" -C "%teamcity.build.checkoutDir%" reset --hard ${Kotlin.paramRefs.buildVcsNumber}"""
        }
        script {
            name = "Revert cidr191 patchset (kotlin-ultimate)"
            scriptContent = """"%env.TEAMCITY_GIT_PATH%" -C "%teamcity.build.checkoutDir%/kotlin-ultimate" reset --hard ${Kotlin_Ultimate.paramRefs.buildVcsNumber}"""
        }
        maven {
            name = "Set Version"
            executionMode = BuildStep.ExecutionMode.RUN_ON_FAILURE
            goals = "versions:set"
            pomLocation = "libraries/pom.xml"
            runnerArgs = "-DnewVersion=%DeployVersion% -DgenerateBackupPoms=false -DprocessAllModules=true"
            mavenVersion = bundled_3_3()
            useOwnLocalRepo = true
            jdkHome = "%projectJDK%"
        }
        maven {
            name = "Maven build"
            executionMode = BuildStep.ExecutionMode.RUN_ON_FAILURE
            goals = "clean install"
            pomLocation = "libraries/pom.xml"
            runnerArgs = "-DskipTests %mavenParameters%"
            workingDir = "libraries"
            mavenVersion = bundled_3_3()
            useOwnLocalRepo = true
            jdkHome = "%projectJDK%"
        }
        gradle {
            name = "Core libraries tests"
            tasks = "%bootstrapTestTasks%"
            buildFile = "build.gradle.kts"
            gradleParams = "%gradleParameters% --continue"
            jdkHome = "%projectJDK%"
        }
    }

    failureConditions {
        executionTimeoutMin = 300
    }

    features {
        freeDiskSpace {
            requiredSpace = "6600mb"
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
        snapshot(Compiler_1) {
            onDependencyFailure = FailureAction.IGNORE
        }
    }

    requirements {
        equals("teamcity.agent.jvm.os.name", "Linux")
        equals("cloud.amazon.agent-name-prefix", "ubuntu-cert")
        noLessThan("teamcity.agent.hardware.memorySizeMb", "7000")
    }
})
