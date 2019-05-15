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

object CompilerAllPlugins : BuildType({
    name = "Compiler and all IDE Plugins (191+cidr191, 183+cidr183, 182, as34, as35, as33, 192)"

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
        param("system.idl2k.deploy", "true")
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
        gradle {
            name = "Zip Artifacts (IJ2019.1 + CIDR2019.1)"
            tasks = "zipPlugin zipCLionPlugin zipAppCodePlugin"
            buildFile = "build.gradle.kts"
            gradleParams = "%gradleParameters% -PpluginArtifactDir=KotlinUltimate -PpluginZipPath=dist/artifacts/kotlin-plugin-%build.number%-IJ2019.1-%kotlin.plugin.patch.number%.zip -PcidrPluginsEnabled=true -PclionPluginNumber=%kotlin.plugin.patch.number% -PappcodePluginNumber=%kotlin.plugin.patch.number%"
            jdkHome = "%projectJDK%"
            jvmArgs = "-Djavax.net.ssl.keyStore=/usr/lib/jvm/java-8-oracle/jre/lib/security/cacerts -Djavax.net.ssl.keyStorePassword=changeit"
        }
        ant {
            name = "Publish Artifacts (IJ2019.1 + CIDR2019.1)"
            mode = antScript {
                content = """
                    <project name="Publish artifacts" default="publish">
                    <target name="publish">
                    <echo message="##teamcity[publishArtifacts 'dist/artifacts/kotlin-plugin-%build.number%-IJ2019.1-%kotlin.plugin.patch.number%.zip']" />
                    <echo message="##teamcity[publishArtifacts 'dist/artifacts/kotlin-plugin-*-CLion-*.zip']" />
                    <echo message="##teamcity[publishArtifacts 'dist/artifacts/kotlin-plugin-*-AppCode-*.zip']" />
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
        gradle {
            name = "Generate updatePlugins.xml (CIDR2019.1)"
            tasks = "clionUpdatePluginsXml appcodeUpdatePluginsXml"
            buildFile = "build.gradle.kts"
            gradleParams = "%gradleParameters%  -PcidrPluginsEnabled=true -PclionPluginNumber=%kotlin.plugin.patch.number% -PappcodePluginNumber=%kotlin.plugin.patch.number% -PclionPluginRepoUrl=%teamcity.serverUrl%/guestAuth/repository/download/%system.teamcity.buildType.id%/%teamcity.build.id%:id/ -PappcodePluginRepoUrl=%teamcity.serverUrl%/guestAuth/repository/download/%system.teamcity.buildType.id%/%teamcity.build.id%:id/"
            jdkHome = "%projectJDK%"
            jvmArgs = "-Djavax.net.ssl.keyStore=/usr/lib/jvm/java-8-oracle/jre/lib/security/cacerts -Djavax.net.ssl.keyStorePassword=changeit"
        }
        ant {
            name = "Publish updatePlugins.xml (CIDR2019.1)"
            mode = antScript {
                content = """
                    <project name="Publish artifacts" default="publish">
                    <target name="publish">
                    <echo message="##teamcity[publishArtifacts 'dist/artifacts/updatePlugins-CLion*.xml']" />
                    <echo message="##teamcity[publishArtifacts 'dist/artifacts/updatePlugins-AppCode*.xml']" />
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
        script {
            name = "Revert cidr191 patchset (kotlin-ultimate)"
            scriptContent = """"%env.TEAMCITY_GIT_PATH%" -C "%teamcity.build.checkoutDir%/kotlin-ultimate" reset --hard ${Kotlin_Ultimate.paramRefs.buildVcsNumber}"""
        }
        script {
            name = "Bunch 183 switch (kotlin)"
            scriptContent = """bunch-0.9.0/bin/bunch switch "%teamcity.build.checkoutDir%" 183 --cleanup"""
        }
        script {
            name = "Bunch cidr183 switch (kotlin-ultimate)"
            scriptContent = """bunch-0.9.0/bin/bunch switch "%teamcity.build.checkoutDir%/kotlin-ultimate" cidr183 --cleanup"""
        }
        gradle {
            name = "Clean dist/artifacts folder"
            tasks = "cleanupArtifacts"
            buildFile = "build.gradle.kts"
            gradleParams = "%gradleParameters%"
            jdkHome = "%projectJDK%"
        }
        gradle {
            name = "Plugin for IJ2018.3 + CIDR2018.3"
            tasks = "writePluginVersion ideaPlugin :ultimate:ideaUltimatePlugin clionPlugin appcodePlugin"
            buildFile = "build.gradle.kts"
            gradleParams = "%gradleParameters% -PpluginVersion=%build.number%-IJ2018.3-%kotlin.plugin.patch.number% -PcidrPluginsEnabled=true -PclionPluginNumber=%kotlin.plugin.patch.number% -PappcodePluginNumber=%kotlin.plugin.patch.number% -x dist"
            jdkHome = "%projectJDK%"
            jvmArgs = "-Djavax.net.ssl.keyStore=/usr/lib/jvm/java-8-oracle/jre/lib/security/cacerts -Djavax.net.ssl.keyStorePassword=changeit"
        }
        gradle {
            name = "Zip Artifacts (IJ2018.3 + CIDR2018.3)"
            tasks = "zipPlugin zipCLionPlugin zipAppCodePlugin"
            buildFile = "build.gradle.kts"
            gradleParams = "%gradleParameters% -PpluginArtifactDir=KotlinUltimate -PpluginZipPath=dist/artifacts/kotlin-plugin-%build.number%-IJ2018.3-%kotlin.plugin.patch.number%.zip -PcidrPluginsEnabled=true -PclionPluginNumber=%kotlin.plugin.patch.number% -PappcodePluginNumber=%kotlin.plugin.patch.number%"
            jdkHome = "%projectJDK%"
            jvmArgs = "-Djavax.net.ssl.keyStore=/usr/lib/jvm/java-8-oracle/jre/lib/security/cacerts -Djavax.net.ssl.keyStorePassword=changeit"
        }
        ant {
            name = "Publish Artifacts (IJ2018.3 + CIDR2018.3)"
            mode = antScript {
                content = """
                    <project name="Publish artifacts" default="publish">
                    <target name="publish">
                    <echo message="##teamcity[publishArtifacts 'dist/artifacts/kotlin-plugin-%build.number%-IJ2018.3-%kotlin.plugin.patch.number%.zip']" />
                    <echo message="##teamcity[publishArtifacts 'dist/artifacts/kotlin-plugin-*-CLion-*.zip']" />
                    <echo message="##teamcity[publishArtifacts 'dist/artifacts/kotlin-plugin-*-AppCode-*.zip']" />
                    </target>
                    </project>
                """.trimIndent()
            }
            antArguments = "-v"
            jdkHome = "%projectJDK%"
        }
        ant {
            name = "Generate and publish updatePlugins.xml (IJ2018.3)"
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
                    
                        <echoxml file="updatePlugins-IJ2018.3.xml">
                          <plugins>
                            <plugin id="org.jetbrains.kotlin" url="%teamcity.serverUrl%/guestAuth/repository/download/%system.teamcity.buildType.id%/%teamcity.build.id%:id/kotlin-plugin-%build.number%-IJ2018.3-%kotlin.plugin.patch.number%.zip" version="%build.number%-IJ2018.3-%kotlin.plugin.patch.number%">
                                <idea-version since-build="${'$'}{since.version}" until-build="${'$'}{until.version}"/>
                                <description>Kotlin language support</description>
                            </plugin>
                          </plugins>
                        </echoxml>
                    
                        <echo message="##teamcity[publishArtifacts 'updatePlugins-IJ2018.3.xml']" />
                      </target>
                    </project>
                """.trimIndent()
            }
            antArguments = "-v"
            jdkHome = "%projectJDK%"
        }
        gradle {
            name = "Generate updatePlugins.xml (CIDR2018.3)"
            tasks = "clionUpdatePluginsXml appcodeUpdatePluginsXml"
            buildFile = "build.gradle.kts"
            gradleParams = "%gradleParameters%  -PcidrPluginsEnabled=true -PclionPluginNumber=%kotlin.plugin.patch.number% -PappcodePluginNumber=%kotlin.plugin.patch.number% -PclionPluginRepoUrl=%teamcity.serverUrl%/guestAuth/repository/download/%system.teamcity.buildType.id%/%teamcity.build.id%:id/ -PappcodePluginRepoUrl=%teamcity.serverUrl%/guestAuth/repository/download/%system.teamcity.buildType.id%/%teamcity.build.id%:id/"
            jdkHome = "%projectJDK%"
            jvmArgs = "-Djavax.net.ssl.keyStore=/usr/lib/jvm/java-8-oracle/jre/lib/security/cacerts -Djavax.net.ssl.keyStorePassword=changeit"
        }
        ant {
            name = "Publish updatePlugins.xml (CIDR2018.3)"
            mode = antScript {
                content = """
                    <project name="Publish artifacts" default="publish">
                    <target name="publish">
                    <echo message="##teamcity[publishArtifacts 'dist/artifacts/updatePlugins-CLion*.xml']" />
                    <echo message="##teamcity[publishArtifacts 'dist/artifacts/updatePlugins-AppCode*.xml']" />
                    </target>
                    </project>
                """.trimIndent()
            }
            antArguments = "-v"
            jdkHome = "%projectJDK%"
        }
        script {
            name = "Revert 183 patchset (kotlin)"
            scriptContent = """"%env.TEAMCITY_GIT_PATH%" -C "%teamcity.build.checkoutDir%" reset --hard ${Kotlin.paramRefs.buildVcsNumber}"""
        }
        script {
            name = "Revert cidr183 patchset (kotlin-ultimate)"
            scriptContent = """"%env.TEAMCITY_GIT_PATH%" -C "%teamcity.build.checkoutDir%/kotlin-ultimate" reset --hard ${Kotlin_Ultimate.paramRefs.buildVcsNumber}"""
        }
        script {
            name = "Bunch 182 switch (kotlin)"
            scriptContent = """bunch-0.9.0/bin/bunch switch "%teamcity.build.checkoutDir%" 182 --cleanup"""
        }
        gradle {
            name = "Clean dist/artifacts folder"
            tasks = "cleanupArtifacts"
            buildFile = "build.gradle.kts"
            gradleParams = "%gradleParameters%"
            jdkHome = "%projectJDK%"
        }
        gradle {
            name = "Plugin for IJ2018.2"
            tasks = "writePluginVersion ideaPlugin :ultimate:ideaUltimatePlugin"
            buildFile = "build.gradle.kts"
            gradleParams = "%gradleParameters% -PpluginVersion=%build.number%-IJ2018.2-%kotlin.plugin.patch.number% -x dist"
            jdkHome = "%projectJDK%"
        }
        gradle {
            name = "Zip Artifacts (IJ2018.2)"
            tasks = "zipPlugin"
            buildFile = "build.gradle.kts"
            gradleParams = "%gradleParameters% -PpluginArtifactDir=KotlinUltimate -PpluginZipPath=dist/artifacts/kotlin-plugin-%build.number%-IJ2018.2-%kotlin.plugin.patch.number%.zip"
            jdkHome = "%projectJDK%"
        }
        ant {
            name = "Publish Artifacts (IJ2018.2)"
            mode = antScript {
                content = """
                    <project name="Publish artifacts" default="publish">
                    <target name="publish">
                    <echo message="##teamcity[publishArtifacts 'dist/artifacts/kotlin-plugin-%build.number%-IJ2018.2-%kotlin.plugin.patch.number%.zip']" />
                    </target>
                    </project>
                """.trimIndent()
            }
            antArguments = "-v"
            jdkHome = "%projectJDK%"
        }
        ant {
            name = "Generate and publish updatePlugins.xml (IJ2018.2)"
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
                    
                        <echoxml file="updatePlugins-IJ2018.2.xml">
                          <plugins>
                            <plugin id="org.jetbrains.kotlin" url="%teamcity.serverUrl%/guestAuth/repository/download/%system.teamcity.buildType.id%/%teamcity.build.id%:id/kotlin-plugin-%build.number%-IJ2018.2-%kotlin.plugin.patch.number%.zip" version="%build.number%-IJ2018.2-%kotlin.plugin.patch.number%">
                                <idea-version since-build="${'$'}{since.version}" until-build="${'$'}{until.version}"/>
                                <description>Kotlin language support</description>
                            </plugin>
                          </plugins>
                        </echoxml>
                    
                        <echo message="##teamcity[publishArtifacts 'updatePlugins-IJ2018.2.xml']" />
                      </target>
                    </project>
                """.trimIndent()
            }
            antArguments = "-v"
            jdkHome = "%projectJDK%"
        }
        script {
            name = "Revert 182 patchset (kotlin)"
            scriptContent = """"%env.TEAMCITY_GIT_PATH%" -C "%teamcity.build.checkoutDir%" reset --hard ${Kotlin.paramRefs.buildVcsNumber}"""
        }
        script {
            name = "Bunch as34 switch (kotlin)"
            scriptContent = """bunch-0.9.0/bin/bunch switch "%teamcity.build.checkoutDir%" as34 --cleanup"""
        }
        gradle {
            name = "Clean dist/artifacts folder"
            tasks = "cleanupArtifacts"
            buildFile = "build.gradle.kts"
            gradleParams = "%gradleParameters%"
            jdkHome = "%projectJDK%"
        }
        gradle {
            name = "Plugin for Studio3.4"
            tasks = "writePluginVersion ideaPlugin"
            buildFile = "build.gradle.kts"
            gradleParams = "%gradleParameters% -PpluginVersion=%build.number%-Studio3.4-%kotlin.plugin.patch.number% -x dist"
            jdkHome = "%projectJDK%"
        }
        gradle {
            name = "Zip Artifacts (Studio3.4)"
            tasks = "zipPlugin"
            buildFile = "build.gradle.kts"
            gradleParams = "%gradleParameters% -PpluginArtifactDir=Kotlin -PpluginZipPath=dist/artifacts/kotlin-plugin-%build.number%-Studio3.4-%kotlin.plugin.patch.number%.zip"
            jdkHome = "%projectJDK%"
        }
        ant {
            name = "Publish Artifacts (Studio3.4)"
            mode = antScript {
                content = """
                    <project name="Publish artifacts" default="publish">
                    <target name="publish">
                    <echo message="##teamcity[publishArtifacts 'dist/artifacts/kotlin-plugin-%build.number%-Studio3.4-%kotlin.plugin.patch.number%.zip']" />
                    </target>
                    </project>
                """.trimIndent()
            }
            antArguments = "-v"
            jdkHome = "%projectJDK%"
        }
        ant {
            name = "Generate and publish updatePlugins.xml (Studio3.4)"
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
                    
                        <echoxml file="updatePlugins-Studio3.4.xml">
                          <plugins>
                            <plugin id="org.jetbrains.kotlin" url="%teamcity.serverUrl%/guestAuth/repository/download/%system.teamcity.buildType.id%/%teamcity.build.id%:id/kotlin-plugin-%build.number%-Studio3.4-%kotlin.plugin.patch.number%.zip" version="%build.number%-Studio3.4-%kotlin.plugin.patch.number%">
                                <idea-version since-build="${'$'}{since.version}" until-build="${'$'}{until.version}"/>
                                <description>Kotlin language support</description>
                            </plugin>
                          </plugins>
                        </echoxml>
                    
                        <echo message="##teamcity[publishArtifacts 'updatePlugins-Studio3.4.xml']" />
                      </target>
                    </project>
                """.trimIndent()
            }
            antArguments = "-v"
            jdkHome = "%projectJDK%"
        }
        script {
            name = "Revert as34 patchset (kotlin)"
            scriptContent = """"%env.TEAMCITY_GIT_PATH%" -C "%teamcity.build.checkoutDir%" reset --hard ${Kotlin.paramRefs.buildVcsNumber}"""
        }
        script {
            name = "Bunch as35 switch (kotlin)"
            scriptContent = """bunch-0.9.0/bin/bunch switch "%teamcity.build.checkoutDir%" as35 --cleanup"""
        }
        gradle {
            name = "Clean dist/artifacts folder"
            tasks = "cleanupArtifacts"
            buildFile = "build.gradle.kts"
            gradleParams = "%gradleParameters%"
            jdkHome = "%projectJDK%"
        }
        gradle {
            name = "Plugin for Studio3.5"
            tasks = "writePluginVersion ideaPlugin"
            buildFile = "build.gradle.kts"
            gradleParams = "%gradleParameters% -PpluginVersion=%build.number%-Studio3.5-%kotlin.plugin.patch.number% -x dist"
            jdkHome = "%projectJDK%"
        }
        gradle {
            name = "Zip Artifacts (Studio3.5)"
            tasks = "zipPlugin"
            buildFile = "build.gradle.kts"
            gradleParams = "%gradleParameters% -PpluginArtifactDir=Kotlin -PpluginZipPath=dist/artifacts/kotlin-plugin-%build.number%-Studio3.5-%kotlin.plugin.patch.number%.zip"
            jdkHome = "%projectJDK%"
        }
        ant {
            name = "Publish Artifacts (Studio3.5)"
            mode = antScript {
                content = """
                    <project name="Publish artifacts" default="publish">
                    <target name="publish">
                    <echo message="##teamcity[publishArtifacts 'dist/artifacts/kotlin-plugin-%build.number%-Studio3.5-%kotlin.plugin.patch.number%.zip']" />
                    </target>
                    </project>
                """.trimIndent()
            }
            antArguments = "-v"
            jdkHome = "%projectJDK%"
        }
        ant {
            name = "Generate and publish updatePlugins.xml (Studio3.5)"
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
                    
                        <echoxml file="updatePlugins-Studio3.5.xml">
                          <plugins>
                            <plugin id="org.jetbrains.kotlin" url="%teamcity.serverUrl%/guestAuth/repository/download/%system.teamcity.buildType.id%/%teamcity.build.id%:id/kotlin-plugin-%build.number%-Studio3.5-%kotlin.plugin.patch.number%.zip" version="%build.number%-Studio3.5-%kotlin.plugin.patch.number%">
                                <idea-version since-build="${'$'}{since.version}" until-build="${'$'}{until.version}"/>
                                <description>Kotlin language support</description>
                            </plugin>
                          </plugins>
                        </echoxml>
                    
                        <echo message="##teamcity[publishArtifacts 'updatePlugins-Studio3.5.xml']" />
                      </target>
                    </project>
                """.trimIndent()
            }
            antArguments = "-v"
            jdkHome = "%projectJDK%"
        }
        script {
            name = "Revert as35 patchset (kotlin)"
            scriptContent = """"%env.TEAMCITY_GIT_PATH%" -C "%teamcity.build.checkoutDir%" reset --hard ${Kotlin.paramRefs.buildVcsNumber}"""
        }
        script {
            name = "Bunch as33 switch (kotlin)"
            scriptContent = """bunch-0.9.0/bin/bunch switch "%teamcity.build.checkoutDir%" as33 --cleanup"""
        }
        gradle {
            name = "Clean dist/artifacts folder"
            tasks = "cleanupArtifacts"
            buildFile = "build.gradle.kts"
            gradleParams = "%gradleParameters%"
            jdkHome = "%projectJDK%"
        }
        gradle {
            name = "Plugin for Studio3.3"
            tasks = "writePluginVersion ideaPlugin"
            buildFile = "build.gradle.kts"
            gradleParams = "%gradleParameters% -PpluginVersion=%build.number%-Studio3.3-%kotlin.plugin.patch.number% -x dist"
            jdkHome = "%projectJDK%"
        }
        gradle {
            name = "Zip Artifacts (Studio3.3)"
            tasks = "zipPlugin"
            buildFile = "build.gradle.kts"
            gradleParams = "%gradleParameters% -PpluginArtifactDir=Kotlin -PpluginZipPath=dist/artifacts/kotlin-plugin-%build.number%-Studio3.3-%kotlin.plugin.patch.number%.zip"
            jdkHome = "%projectJDK%"
        }
        ant {
            name = "Publish Artifacts (Studio3.3)"
            mode = antScript {
                content = """
                    <project name="Publish artifacts" default="publish">
                    <target name="publish">
                    <echo message="##teamcity[publishArtifacts 'dist/artifacts/kotlin-plugin-%build.number%-Studio3.3-%kotlin.plugin.patch.number%.zip']" />
                    </target>
                    </project>
                """.trimIndent()
            }
            antArguments = "-v"
            jdkHome = "%projectJDK%"
        }
        ant {
            name = "Generate and publish updatePlugins.xml (Studio3.3)"
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
                    
                        <echoxml file="updatePlugins-Studio3.3.xml">
                          <plugins>
                            <plugin id="org.jetbrains.kotlin" url="%teamcity.serverUrl%/guestAuth/repository/download/%system.teamcity.buildType.id%/%teamcity.build.id%:id/kotlin-plugin-%build.number%-Studio3.3-%kotlin.plugin.patch.number%.zip" version="%build.number%-Studio3.3-%kotlin.plugin.patch.number%">
                                <idea-version since-build="${'$'}{since.version}" until-build="${'$'}{until.version}"/>
                                <description>Kotlin language support</description>
                            </plugin>
                          </plugins>
                        </echoxml>
                    
                        <echo message="##teamcity[publishArtifacts 'updatePlugins-Studio3.3.xml']" />
                      </target>
                    </project>
                """.trimIndent()
            }
            antArguments = "-v"
            jdkHome = "%projectJDK%"
        }
        script {
            name = "Revert as33 patchset (kotlin)"
            scriptContent = """"%env.TEAMCITY_GIT_PATH%" -C "%teamcity.build.checkoutDir%" reset --hard ${Kotlin.paramRefs.buildVcsNumber}"""
        }
        script {
            name = "Bunch 192 switch (kotlin)"
            scriptContent = """bunch-0.9.0/bin/bunch switch "%teamcity.build.checkoutDir%" 192 --cleanup"""
        }
        gradle {
            name = "Clean dist/artifacts folder"
            tasks = "cleanupArtifacts"
            buildFile = "build.gradle.kts"
            gradleParams = "%gradleParameters%"
            jdkHome = "%projectJDK%"
        }
        gradle {
            name = "Plugin for IJ2019.2"
            tasks = "writePluginVersion ideaPlugin :ultimate:ideaUltimatePlugin"
            buildFile = "build.gradle.kts"
            gradleParams = "%gradleParameters% -PpluginVersion=%build.number%-IJ2019.2-%kotlin.plugin.patch.number% -x dist"
            jdkHome = "%projectJDK%"
        }
        gradle {
            name = "Zip Artifacts (IJ2019.2)"
            tasks = "zipPlugin"
            buildFile = "build.gradle.kts"
            gradleParams = "%gradleParameters% -PpluginArtifactDir=KotlinUltimate -PpluginZipPath=dist/artifacts/kotlin-plugin-%build.number%-IJ2019.2-%kotlin.plugin.patch.number%.zip"
            jdkHome = "%projectJDK%"
        }
        ant {
            name = "Publish Artifacts (IJ2019.2)"
            mode = antScript {
                content = """
                    <project name="Publish artifacts" default="publish">
                    <target name="publish">
                    <echo message="##teamcity[publishArtifacts 'dist/artifacts/kotlin-plugin-%build.number%-IJ2019.2-%kotlin.plugin.patch.number%.zip']" />
                    </target>
                    </project>
                """.trimIndent()
            }
            antArguments = "-v"
            jdkHome = "%projectJDK%"
        }
        ant {
            name = "Generate and publish updatePlugins.xml (IJ2019.2)"
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
                    
                        <echoxml file="updatePlugins-IJ2019.2.xml">
                          <plugins>
                            <plugin id="org.jetbrains.kotlin" url="%teamcity.serverUrl%/guestAuth/repository/download/%system.teamcity.buildType.id%/%teamcity.build.id%:id/kotlin-plugin-%build.number%-IJ2019.2-%kotlin.plugin.patch.number%.zip" version="%build.number%-IJ2019.2-%kotlin.plugin.patch.number%">
                                <idea-version since-build="${'$'}{since.version}" until-build="${'$'}{until.version}"/>
                                <description>Kotlin language support</description>
                            </plugin>
                          </plugins>
                        </echoxml>
                    
                        <echo message="##teamcity[publishArtifacts 'updatePlugins-IJ2019.2.xml']" />
                      </target>
                    </project>
                """.trimIndent()
            }
            antArguments = "-v"
            jdkHome = "%projectJDK%"
        }
        script {
            name = "Revert 192 patchset (kotlin)"
            scriptContent = """"%env.TEAMCITY_GIT_PATH%" -C "%teamcity.build.checkoutDir%" reset --hard ${Kotlin.paramRefs.buildVcsNumber}"""
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
            runnerArgs = "-DskipTests %mavenParameters%"
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
            requiredSpace = "28000mb"
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
        equals("teamcity.agent.jvm.os.name", "Linux")
        equals("cloud.amazon.agent-name-prefix", "ubuntu-cert")
        noLessThan("teamcity.agent.hardware.memorySizeMb", "7000")
    }
})
