import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    kotlin("jvm")
}

val embeddableTestRuntime by configurations.creating {
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
    }
}

val testJsr223Runtime by configurations.creating {
    extendsFrom(configurations["testRuntimeClasspath"])
}

val testCompilationClasspath by configurations.creating

dependencies {
    testApi(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testApi(libs.junit.platform.launcher)
    testCompileOnly(project(":kotlin-scripting-jvm-host-unshaded"))
    testCompileOnly(project(":compiler:cli"))
    testCompileOnly(project(":core:util.runtime"))

    testImplementation(testFixtures(project(":compiler:test-infrastructure-utils")))
    testImplementation(projectTests(":kotlin-scripting-compiler"))

    testRuntimeOnly(project(":kotlin-scripting-jsr223-unshaded"))
    testRuntimeOnly(project(":kotlin-compiler"))

    embeddableTestRuntime(libs.junit.jupiter.engine)
    embeddableTestRuntime(project(":kotlin-scripting-jsr223"))
    embeddableTestRuntime(project(":kotlin-scripting-compiler-embeddable"))
    embeddableTestRuntime(testSourceSet.output)

    testCompilationClasspath(kotlinStdlib())
}

sourceSets {
    "main" {}
    "test" { projectDefault() }
}

tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions.freeCompilerArgs.add("-Xallow-kotlin-package")
}

projectTest(parallel = true, jUnitMode = JUnitMode.JUnit5, defineJDKEnvVariables = listOf(JdkMajorVersion.JDK_17_0)) {
    dependsOn(":dist")
    workingDir = rootDir
    val testRuntimeProvider = project.provider { testJsr223Runtime.asPath }
    val testCompilationClasspathProvider = project.provider { testCompilationClasspath.asPath }
    doFirst {
        systemProperty("testJsr223RuntimeClasspath", testRuntimeProvider.get())
        systemProperty("testCompilationClasspath", testCompilationClasspathProvider.get())
        systemProperty("kotlin.script.base.compiler.arguments", "-language-version 1.9")
    }
}

projectTest(taskName = "embeddableTest", jUnitMode = JUnitMode.JUnit5, parallel = true) {
    workingDir = rootDir
    dependsOn(embeddableTestRuntime)
    classpath = embeddableTestRuntime
    val testRuntimeProvider = project.provider { embeddableTestRuntime.asPath }
    val testCompilationClasspathProvider = project.provider { testCompilationClasspath.asPath }
    doFirst {
        systemProperty("testJsr223RuntimeClasspath", testRuntimeProvider.get())
        systemProperty("testCompilationClasspath", testCompilationClasspathProvider.get())
        systemProperty("kotlin.script.base.compiler.arguments", "-language-version 1.9")
    }
}
