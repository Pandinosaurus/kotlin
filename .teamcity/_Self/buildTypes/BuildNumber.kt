package _Self.buildTypes

import jetbrains.buildServer.configs.kotlin.v2018_2.*

object BuildNumber : BuildType({
    name = "Build number"

    buildNumberPattern = "%chain.build.number%"

    params {
        param("chain.build.number", "%build.number.prefix%-%build.counter%")
    }

    vcs {
        root(_Self.vcsRoots.Kotlin)
        root(_Self.vcsRoots.Kotlin_Ultimate, "+:. => kotlin-ultimate")

        checkoutMode = CheckoutMode.MANUAL
        cleanCheckout = true
    }
})
