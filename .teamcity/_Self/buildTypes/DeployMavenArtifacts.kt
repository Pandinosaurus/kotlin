package _Self.buildTypes

import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.buildFeatures.Swabra
import jetbrains.buildServer.configs.kotlin.v2018_2.buildFeatures.freeDiskSpace
import jetbrains.buildServer.configs.kotlin.v2018_2.buildFeatures.perfmon
import jetbrains.buildServer.configs.kotlin.v2018_2.buildFeatures.swabra
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.script

object DeployMavenArtifacts : BuildType({
    name = "Deploy Maven Artifacts (MANUAL)"
    description = "Deploys arbitrary build to bintray or sonatype"

    type = BuildTypeSettings.Type.DEPLOYMENT
    buildNumberPattern = "${Compiler_1.depParamRefs.buildNumber}"

    params {
        param("gradleParameters", "--info --full-stacktrace")
        text("DeployVersion", "${Compiler_1.depParamRefs.buildNumber}", display = ParameterDisplay.PROMPT)
        password("system.kotlin.key.passphrase", "credentialsJSON:bb92a8ed-6aca-44ae-b8c9-cb2292303779", display = ParameterDisplay.HIDDEN)
        password("system.kotlin.sonatype.user", "credentialsJSON:c4c48382-655f-414e-8d01-0da4e6960222", display = ParameterDisplay.HIDDEN)
        param("mavenParameters", "")
        text("system.signing.secretKeyRingFile", "%teamcity.build.checkoutDir%/libraries/.gnupg/secring.gpg", display = ParameterDisplay.HIDDEN)
        password("kotlin.key", "credentialsJSON:59044490-d28c-45a8-a987-14e839e489d0", display = ParameterDisplay.HIDDEN)
        select("system.deploy-repo", "bintray", display = ParameterDisplay.PROMPT,
                options = listOf("sonatype-nexus-snapshots-repo" to "sonatype-nexus-snapshots", "bintray-repo" to "bintray"))
        password("system.kotlin.bintray.user", "credentialsJSON:2ad8bafb-8e7e-4520-afdb-8449e0d4a8bf", display = ParameterDisplay.HIDDEN)
        text("requirement.jdk16", "%env.JDK_16%", display = ParameterDisplay.HIDDEN)
        select("system.deploy-url", "https://api.bintray.com/maven/kotlin/kotlin-dev/kotlin", display = ParameterDisplay.PROMPT,
                options = listOf("sonatype-url (maven central)" to "https://oss.sonatype.org/service/local/staging/deploy/maven2/", "bintray-dev-url (publish manually)" to "https://api.bintray.com/maven/kotlin/kotlin-dev/kotlin", "bintray-dev-url (publish automatically)" to "https://api.bintray.com/maven/kotlin/kotlin-dev/kotlin/;publish=1", "bintray-eap-url (publish manually)" to "https://api.bintray.com/maven/kotlin/kotlin-eap/kotlin"))
        text("requirement.jdk18", "%env.JDK_18%", display = ParameterDisplay.HIDDEN)
        param("system.deployVersion", "%DeployVersion%")
        text("requirement.jdk17", "%env.JDK_17%", display = ParameterDisplay.HIDDEN)
        password("system.kotlin.sonatype.password", "credentialsJSON:1704d2fb-3d5c-4103-bcc3-1ae5a4697d38", display = ParameterDisplay.HIDDEN)
        param("system.kotlin.key.name", "74CE0A0B")
        password("system.kotlin.bintray.password", "credentialsJSON:f81e345e-6628-4bde-8190-95d96105a7cc", display = ParameterDisplay.HIDDEN)
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
        script {
            name = "Prepare gnupg"
            scriptContent = """
                cd libraries
                export HOME=${'$'}(pwd)
                rm -rf .gnupg
                cat >keyfile <<EOT
                %kotlin.key%
                EOT
                gpg --allow-secret-key-import --import keyfile
                rm -v keyfile
            """.trimIndent()
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
            runnerArgs = "-Dinvoker.skip=true -DskipTests --activate-profiles noTest,sign-artifacts -e %mavenParameters%"
            workingDir = "libraries"
            mavenVersion = bundled_3_3()
            userSettingsSelection = "userSettingsSelection:byPath"
            userSettingsPath = "%system.teamcity.build.checkoutDir%/libraries/maven-settings.xml"
            useOwnLocalRepo = true
            jdkHome = "%projectJDK%"
        }
        script {
            name = "Cleanup"
            executionMode = BuildStep.ExecutionMode.ALWAYS
            scriptContent = """
                cd libraries
                rm -rf .gnupg
            """.trimIndent()
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
    }
})
