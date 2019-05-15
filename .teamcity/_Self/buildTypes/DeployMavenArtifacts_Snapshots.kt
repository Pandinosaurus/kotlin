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
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.finishBuildTrigger

object DeployMavenArtifacts_Snapshots : BuildType({
    name = "Deploy Maven Artifacts (SNAPSHOTS)"
    description = "Automatically deploys every successfull build to sonatype"

    type = BuildTypeSettings.Type.DEPLOYMENT
    buildNumberPattern = "${Compiler_1.depParamRefs.buildNumber}"

    params {
        param("gradleParameters", "--info --full-stacktrace")
        text("requirement.jdk16", "%env.JDK_16%", display = ParameterDisplay.HIDDEN)
        param("system.deploy-url", "")
        text("DeployVersion", "1.3-SNAPSHOT")
        text("requirement.jdk18", "%env.JDK_18%", display = ParameterDisplay.HIDDEN)
        param("system.deployVersion", "%DeployVersion%")
        text("requirement.jdk17", "%env.JDK_17%", display = ParameterDisplay.HIDDEN)
        password("system.kotlin.sonatype.password", "credentialsJSON:1704d2fb-3d5c-4103-bcc3-1ae5a4697d38", display = ParameterDisplay.HIDDEN)
        password("system.kotlin.sonatype.user", "credentialsJSON:c4c48382-655f-414e-8d01-0da4e6960222", display = ParameterDisplay.HIDDEN)
        param("mavenParameters", "")
        param("system.deploy-repo", "sonatype-nexus-snapshots")
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
            name = "Setup latest kotlin native version"
            mode = antScript {
                content = """
                    <project name="Kotlin" default="setup-latest-kotlin-native-version">
                        <target name="setup-latest-kotlin-native-version">
                            <get src="https://buildserver.labs.intellij.net/guestAuth/app/rest/builds/buildType:(Kotlin_KotlinNative_Master_KotlinNativeCompilerPublish),status:success,count:1/number" dest="latest-kotlin-native-version.txt" />
                            <loadfile property="latest-kotlin-native-version" srcfile="latest-kotlin-native-version.txt"/>
                            <echo message="##teamcity[setParameter name='system.versions.kotlin-native' value='${'$'}{latest-kotlin-native-version}']" />
                            <delete file="latest-kotlin-native-version.txt"/>
                        </target>
                    </project>
                """.trimIndent()
            }
            jvmArgs = "-Djavax.net.ssl.keyStore=/usr/lib/jvm/java-8-oracle/jre/lib/security/cacerts -Djavax.net.ssl.keyStorePassword=changeit"
        }
        maven {
            name = "Set Version"
            goals = "versions:set"
            pomLocation = "libraries/pom.xml"
            runnerArgs = "-DnewVersion=%DeployVersion% -DgenerateBackupPoms=false -DprocessAllModules=true"
            mavenVersion = bundled_3_3()
            useOwnLocalRepo = true
            jdkHome = "%projectJDK%"
        }
        gradle {
            name = "Gradle install and publish"
            tasks = "install publish"
            buildFile = "build.gradle.kts"
            gradleParams = "%gradleParameters% -Dmaven.repo.local=%teamcity.maven.local.repository.path%"
            jdkHome = "%projectJDK%"
        }
        maven {
            name = "Maven publish"
            goals = "clean deploy"
            pomLocation = "libraries/pom.xml"
            runnerArgs = "-Dinvoker.skip=true -DskipTests --activate-profiles noTest -e %mavenParameters%"
            workingDir = "libraries"
            mavenVersion = bundled_3_3()
            userSettingsSelection = "userSettingsSelection:byPath"
            userSettingsPath = "%system.teamcity.build.checkoutDir%/libraries/maven-settings.xml"
            useOwnLocalRepo = true
            jdkHome = "%projectJDK%"
        }
    }

    triggers {
        finishBuildTrigger {
            buildType = "${Compiler_1.id}"
            successfulOnly = true
        }
    }

    failureConditions {
        executionTimeoutMin = 90
        errorMessage = true
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
        snapshot(Compiler_1) {
            onDependencyFailure = FailureAction.IGNORE
        }
    }

    requirements {
        noLessThan("teamcity.agent.hardware.memorySizeMb", "7000")
        noLessThan("teamcity.agent.work.dir.freeSpaceMb", "5000")
        contains("teamcity.agent.jvm.os.name", "Linux")
        equals("cloud.amazon.agent-name-prefix", "ubuntu-cert")
    }
})
