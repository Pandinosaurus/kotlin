import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform")
}

kotlin {
    linuxX64 {
        binaries {
            sharedLib(namePrefix = "shared")
            staticLib(namePrefix = "static")
            executable(namePrefix = "executable") { entryPoint = "main" }
        }
    }
    iosX64 {
        binaries {
            framework {
                baseName = "libFramework"
            }
        }
    }
    iosArm64 {
        binaries {
            framework {
                baseName = "libFramework"
            }
        }
    }
    iosSimulatorArm64()

    targets.filterIsInstance<KotlinNativeTarget>().forEach {
        it.compilations {
            val main by getting {
                cinterops {
                    val myCinterop by creating {
                        defFile(project.file("src/my.def"))
                    }
                }
            }
        }
    }

    sourceSets {
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

kotlinArtifacts {
    Native.Library("mylib") {
        target = linuxX64
        toolOptions {
            freeCompilerArgs.add("-Xmen=pool")
        }
    }
    Native.Library("myslib") {
        target = linuxX64
        isStatic = false
        modes(DEBUG)
        toolOptions {
            verbose.set(false)
            freeCompilerArgs.set(emptyList())
        }
    }
    Native.Framework("myframe") {
        modes(DEBUG, RELEASE)
        target = iosArm64
        isStatic = false
        toolOptions {
            verbose.set(false)
        }
    }
    Native.FatFramework("myfatframe") {
        targets(iosX64, iosSimulatorArm64)
        toolOptions {
            suppressWarnings.set(false)
        }
    }
    Native.XCFramework {
        targets(iosX64, iosArm64, iosSimulatorArm64)
        setModules(
            project(":lib")
        )
    }
}