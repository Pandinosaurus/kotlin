package _Self.buildTypes

import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.buildFeatures.Swabra
import jetbrains.buildServer.configs.kotlin.v2018_2.buildFeatures.freeDiskSpace
import jetbrains.buildServer.configs.kotlin.v2018_2.buildFeatures.perfmon
import jetbrains.buildServer.configs.kotlin.v2018_2.buildFeatures.swabra
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.ant
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.script

object Compiler_1 : BuildType({
    id("Compiler")
    name = "Compiler (191) with Tests"

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
        param("build.number.default", "${BuildNumber.depParamRefs.buildNumber}")
        param("mavenParameters", "")
        param("system.idl2k.deploy", "true")
        param("distTestTasks", "coreLibsTest gradlePluginTest distTest")
        text("requirement.jdk16", "%env.JDK_16%", display = ParameterDisplay.HIDDEN)
        param("system.deploy-url", "file://%teamcity.build.checkoutDir%/build/repo")
        text("requirement.jdk18", "%env.JDK_18%", display = ParameterDisplay.HIDDEN)
        param("system.deployVersion", "%DeployVersion%")
        text("requirement.jdk17", "%env.JDK_17%", display = ParameterDisplay.HIDDEN)
        param("system.kotlin.suppress.expected.test.failures", "true")
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
        gradle {
            name = "Additional common IDE artifacts"
            tasks = ":prepare:formatter:jar :prepare:ide-lazy-resolver:jar"
            buildFile = "build.gradle.kts"
            gradleParams = "%gradleParameters%"
            jdkHome = "%projectJDK%"
        }
        gradle {
            name = "Zip Artifacts (compiler)"
            tasks = "zipCompiler zipTestData"
            buildFile = "build.gradle.kts"
            gradleParams = "%gradleParameters%"
            jdkHome = "%projectJDK%"
        }
        ant {
            name = "Publish Artifacts (compiler)"
            mode = antScript {
                content = """
                    <project name="Publish artifacts" default="publish">
                    <target name="publish">
                    <echo message="##teamcity[publishArtifacts 'dist/kotlin-compiler*.zip']" />
                    <echo message="##teamcity[publishArtifacts 'dist/kotlin-test-data.zip=>internal']" />
                    <echo message="##teamcity[publishArtifacts 'prepare/ide-lazy-resolver/build/libs/kotlin-ide-common.jar=>internal']" />
                    <echo message="##teamcity[publishArtifacts 'prepare/formatter/build/libs/kotlin-formatter.jar=>internal']" />
                    <echo message="##teamcity[publishArtifacts 'dependencies/markdown.jar=>internal']" />
                    <echo message="##teamcity[publishArtifacts 'dependencies/dependencies.properties=>internal']" />
                    </target>
                    </project>
                """.trimIndent()
            }
            antArguments = "-v"
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
            tasks = "install publish"
            buildFile = "build.gradle.kts"
            gradleParams = "%gradleParameters% -Dmaven.repo.local=%teamcity.maven.local.repository.path%"
            jdkHome = "%projectJDK%"
        }
        gradle {
            name = "Core libraries, tools and compiler tests"
            tasks = "%distTestTasks%"
            buildFile = "build.gradle.kts"
            gradleParams = "%gradleParameters% --continue"
            jdkHome = "%projectJDK%"
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
            goals = "clean deploy"
            pomLocation = "libraries/pom.xml"
            runnerArgs = "-fae %mavenParameters%"
            workingDir = "libraries"
            mavenVersion = bundled_3_3()
            useOwnLocalRepo = true
            jdkHome = "%projectJDK%"
        }
    }

    failureConditions {
        executionTimeoutMin = 300
    }

    features {
        freeDiskSpace {
            requiredSpace = "2900mb"
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
