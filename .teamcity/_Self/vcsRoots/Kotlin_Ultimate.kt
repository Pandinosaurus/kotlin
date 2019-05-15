package _Self.vcsRoots

import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.vcs.GitVcsRoot

object Kotlin_Ultimate : GitVcsRoot({
    name = "Kotlin_Ultimate_Release_dev"
    url = "ssh://git.jetbrains.team/kotlin-ultimate.git"
    branch = "master"
    branchSpec = """
        +:refs/heads/(master)
        +:refs/heads/(rr/*)
        +:refs/heads/(rrg/*)
        +:refs/heads/(bm/*)
        +:refs/heads/(rrinf/*)
    """.trimIndent()
    authMethod = uploadedKey {
        uploadedKey = "Access to KotlinUltimate"
    }
})
