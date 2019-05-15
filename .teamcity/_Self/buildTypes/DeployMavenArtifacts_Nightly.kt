package _Self.buildTypes

import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.buildFeatures.Swabra
import jetbrains.buildServer.configs.kotlin.v2018_2.buildFeatures.freeDiskSpace
import jetbrains.buildServer.configs.kotlin.v2018_2.buildFeatures.perfmon
import jetbrains.buildServer.configs.kotlin.v2018_2.buildFeatures.swabra
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.script
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.ScheduleTrigger
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.schedule

object DeployMavenArtifacts_Nightly : BuildType({
    name = "Deploy Maven Artifacts (NIGHTLY)"
    description = "Automatically deploys last successfull build to bintray"

    type = BuildTypeSettings.Type.DEPLOYMENT
    buildNumberPattern = "${Compiler_1.depParamRefs.buildNumber}"

    params {
        param("gradleParameters", "--info --full-stacktrace")
        text("requirement.jdk16", "%env.JDK_16%", display = ParameterDisplay.HIDDEN)
        param("system.deploy-url", "https://api.bintray.com/maven/kotlin/kotlin-dev/kotlin")
        text("DeployVersion", "${Compiler_1.depParamRefs.buildNumber}")
        text("requirement.jdk18", "%env.JDK_18%", display = ParameterDisplay.HIDDEN)
        param("system.deployVersion", "%DeployVersion%")
        text("requirement.jdk17", "%env.JDK_17%", display = ParameterDisplay.HIDDEN)
        param("mavenParameters", "")
        password("system.kotlin.bintray.password", "credentialsJSON:f81e345e-6628-4bde-8190-95d96105a7cc", display = ParameterDisplay.HIDDEN)
        param("system.deploy-repo", "bintray")
        password("system.kotlin.bintray.user", "credentialsJSON:2ad8bafb-8e7e-4520-afdb-8449e0d4a8bf", display = ParameterDisplay.HIDDEN)
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
        script {
            name = "Finalize publishing"
            scriptContent = "curl -D - -X POST --basic -u %system.kotlin.bintray.user%:%system.kotlin.bintray.password% https://api.bintray.com/content/kotlin/kotlin-dev/kotlin/%DeployVersion%/publish"
        }
        script {
            name = "Set deploymentKind attribute"
            scriptContent = """curl -D - -X PATCH --basic -u %system.kotlin.bintray.user%:%system.kotlin.bintray.password% https://api.bintray.com/packages/kotlin/kotlin-dev/kotlin/versions/%DeployVersion%/attributes -H 'Content-Type: application/json' -d '[{"name":"DeploymentKind","type":"string","values":["Nightly"]}]'"""
        }
    }

    triggers {
        schedule {
            schedulingPolicy = daily {
                hour = 1
            }
            branchFilter = "+:<default>"
            triggerBuild = onWatchedBuildChange {
                buildType = "${Compiler_1.id}"
                watchedBuildRule = ScheduleTrigger.WatchedBuildRule.LAST_SUCCESSFUL
            }
            withPendingChangesOnly = false
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
