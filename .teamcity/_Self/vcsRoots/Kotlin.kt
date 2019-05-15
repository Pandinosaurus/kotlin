package _Self.vcsRoots

import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.vcs.GitVcsRoot

object Kotlin : GitVcsRoot({
    name = "Kotlin_Release_dev"
    url = "https://github.com/JetBrains/kotlin.git"
    branch = "master"
    branchSpec = """
        +:refs/heads/(master)
        +:refs/heads/(rr/*)
        +:refs/heads/(rrg/*)
        +:refs/heads/(bm/*)
        +:refs/heads/(rrinf/*)
    """.trimIndent()
    authMethod = password {
        userName = "KotlinBuild"
        password = "credentialsJSON:5b84d1c9-93ba-4d48-877d-0ef35c9436f5"
    }
})
