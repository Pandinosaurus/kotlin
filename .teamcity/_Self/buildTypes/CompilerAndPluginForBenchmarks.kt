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
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.finishBuildTrigger
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.vcs

object CompilerAndPluginForBenchmarks : BuildType({
    name = "Compiler and IDEA Plugin for Benchmarks (191)"

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
        param("system.deploy-url", "file://%teamcity.build.checkoutDir%/build/repo")
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
            name = "Zip Artifacts (IJ2019.1)"
            tasks = "zipPlugin"
            buildFile = "build.gradle.kts"
            gradleParams = "%gradleParameters% -PpluginArtifactDir=KotlinUltimate -PpluginZipPath=dist/artifacts/kotlin-plugin-%build.number%-IJ2019.1-%kotlin.plugin.patch.number%.zip"
            jdkHome = "%projectJDK%"
        }
        ant {
            name = "Publish Artifacts (IJ2019.1)"
            mode = antScript {
                content = """
                    <project name="Publish artifacts" default="publish">
                    <target name="publish">
                    <echo message="##teamcity[publishArtifacts 'dist/artifacts/kotlin-plugin-%build.number%-IJ2019.1-%kotlin.plugin.patch.number%.zip']" />
                    </target>
                    </project>
                """.trimIndent()
            }
            antArguments = "-v"
            jdkHome = "%projectJDK%"
        }
        ant {
            name = "Generate and publish updatePlugins.xml (IJ2019.1)"
            mode = antScript {
                content = """
                    <project name="Generate and publish updatePlugins.xml" default="generate">
                      <target name="generate">
                        <!-- LEGACY: remove after all branches reabsed on master -->
                        <loadresource property="since.version" quiet="true">
                          <file file="idea/src/META-INF/plugin.xml"/>
                          <filterchain>
                            <tokenfilter>
                              <filetokenizer/>
                              <replaceregex pattern="^(.*)\ssince-build=&quot;([\d\.\*]+)&quot;(.*)${'$'}" replace="\2" flags="s"/>
                            </tokenfilter>
                          </filterchain>
                        </loadresource>
                    
                        <loadresource property="until.version" quiet="true">
                          <file file="idea/src/META-INF/plugin.xml"/>
                          <filterchain>
                            <tokenfilter>
                              <filetokenizer/>
                              <replaceregex pattern="^(.*)\suntil-build=&quot;([\d\.\*]+)&quot;(.*)${'$'}" replace="\2" flags="s"/>
                            </tokenfilter>
                          </filterchain>
                        </loadresource>
                        <!-- /LEGACY: remove after all branches reabsed on master -->
                    
                        <loadresource property="since.version" quiet="true">
                          <file file="idea/resources/META-INF/plugin.xml"/>
                          <filterchain>
                            <tokenfilter>
                              <filetokenizer/>
                              <replaceregex pattern="^(.*)\ssince-build=&quot;([\d\.\*]+)&quot;(.*)${'$'}" replace="\2" flags="s"/>
                            </tokenfilter>
                          </filterchain>
                        </loadresource>
                    
                        <loadresource property="until.version" quiet="true">
                          <file file="idea/resources/META-INF/plugin.xml"/>
                          <filterchain>
                            <tokenfilter>
                              <filetokenizer/>
                              <replaceregex pattern="^(.*)\suntil-build=&quot;([\d\.\*]+)&quot;(.*)${'$'}" replace="\2" flags="s"/>
                            </tokenfilter>
                          </filterchain>
                        </loadresource>
                    
                        <fail unless="since.version" message="Property 'since.version' is not set: Cannot find 'plugin.xml' in 'idea/resources' or in 'idea/src'"/>
                        <fail unless="until.version" message="Property 'until.version' is not set: Cannot find 'plugin.xml' in 'idea/resources' or in 'idea/src'"/>
                    
                        <echoxml file="updatePlugins-IJ2019.1.xml">
                          <plugins>
                            <plugin id="org.jetbrains.kotlin" url="%teamcity.serverUrl%/guestAuth/repository/download/%system.teamcity.buildType.id%/%teamcity.build.id%:id/kotlin-plugin-%build.number%-IJ2019.1-%kotlin.plugin.patch.number%.zip" version="%build.number%-IJ2019.1-%kotlin.plugin.patch.number%">
                                <idea-version since-build="${'$'}{since.version}" until-build="${'$'}{until.version}"/>
                                <description>Kotlin language support</description>
                            </plugin>
                          </plugins>
                        </echoxml>
                    
                        <echo message="##teamcity[publishArtifacts 'updatePlugins-IJ2019.1.xml']" />
                      </target>
                    </project>
                """.trimIndent()
            }
            antArguments = "-v"
            jdkHome = "%projectJDK%"
        }
        script {
            name = "Revert 191 patchset (kotlin)"
            scriptContent = """"%env.TEAMCITY_GIT_PATH%" -C "%teamcity.build.checkoutDir%" reset --hard ${Kotlin.paramRefs.buildVcsNumber}"""
        }
    }

    triggers {
        vcs {
            branchFilter = "+:bm/*"
        }
        finishBuildTrigger {
            buildType = "${BuildNumber.id}"
            branchFilter = """
                +:<default>
                +:rr/bm/*
                +:rrg/bm/*
            """.trimIndent()
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
