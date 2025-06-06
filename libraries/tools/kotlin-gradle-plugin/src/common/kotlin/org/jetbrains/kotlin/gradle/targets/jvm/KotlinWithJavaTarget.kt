/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("PackageDirectoryMismatch") // Old package for compatibility
package org.jetbrains.kotlin.gradle.plugin.mpp

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.Provider
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.gradle.tasks.KOTLIN_BUILD_DIR_NAME
import org.jetbrains.kotlin.gradle.utils.KotlinJvmCompilerOptionsDefault
import org.jetbrains.kotlin.gradle.utils.newInstance
import java.io.File
import javax.inject.Inject

@Suppress("UNCHECKED_CAST", "TYPEALIAS_EXPANSION_DEPRECATION_ERROR", "DEPRECATION_ERROR")
internal fun ObjectFactory.KotlinWithJavaTargetForJvm(
    project: Project,
    targetName: String = "",
): KotlinWithJavaTarget<KotlinJvmOptions, KotlinJvmCompilerOptions> = (newInstance(
    KotlinWithJavaTarget::class.java,
    project,
    KotlinPlatformType.jvm,
    targetName,
    {
        object : DeprecatedHasCompilerOptions<KotlinJvmCompilerOptions> {
            override val options: KotlinJvmCompilerOptions =
                project.objects.KotlinJvmCompilerOptionsDefault(project)
        }
    },
    { compilerOptions: KotlinJvmCompilerOptions ->
        object : KotlinJvmOptions {
            @OptIn(org.jetbrains.kotlin.gradle.InternalKotlinGradlePluginApi::class)
            @Deprecated(
                message = org.jetbrains.kotlin.gradle.dsl.KOTLIN_OPTIONS_DEPRECATION_MESSAGE,
                level = DeprecationLevel.ERROR,
            )
            override val options: KotlinJvmCompilerOptions get() = compilerOptions
        }
    }
) as KotlinWithJavaTarget<KotlinJvmOptions, KotlinJvmCompilerOptions>)

@Suppress("DEPRECATION")
abstract class KotlinWithJavaTarget<KotlinOptionsType : Any, CO : KotlinCommonCompilerOptions> @Inject constructor(
    project: Project,
    override val platformType: KotlinPlatformType,
    override val targetName: String,
    @Suppress("TYPEALIAS_EXPANSION_DEPRECATION_ERROR") compilerOptionsFactory: () -> DeprecatedHasCompilerOptions<CO>,
    kotlinOptionsFactory: (CO) -> KotlinOptionsType
) : AbstractKotlinTarget(project),
    HasConfigurableKotlinCompilerOptions<KotlinJvmCompilerOptions> {
    override var disambiguationClassifier: String? = null
        internal set

    override val apiElementsConfigurationName: String
        get() = JavaPlugin.API_ELEMENTS_CONFIGURATION_NAME

    override val runtimeElementsConfigurationName: String
        get() = JavaPlugin.RUNTIME_ELEMENTS_CONFIGURATION_NAME

    override val artifactsTaskName: String
        get() = JavaPlugin.JAR_TASK_NAME

    override val compilations: NamedDomainObjectContainer<KotlinWithJavaCompilation<KotlinOptionsType, CO>> =
        @Suppress("UNCHECKED_CAST")
        project.container(
            KotlinWithJavaCompilation::class.java as Class<KotlinWithJavaCompilation<KotlinOptionsType, CO>>,
            KotlinWithJavaCompilationFactory(this, compilerOptionsFactory, kotlinOptionsFactory)
        )

    private val layout = project.layout

    internal val defaultArtifactClassesListFile: Provider<File> =
        layout.buildDirectory.dir(KOTLIN_BUILD_DIR_NAME).map {
            val jarTask = project.tasks.getByName(artifactsTaskName) as Jar
            it.file("${sanitizeFileName(jarTask.archiveFileName.get())}-classes.txt").asFile
        }

    internal val buildDir: Provider<Directory> = layout.buildDirectory.dir(KOTLIN_BUILD_DIR_NAME)

    override val compilerOptions: KotlinJvmCompilerOptions = project.objects
        .newInstance<KotlinJvmCompilerOptionsDefault>()
}

private fun sanitizeFileName(candidate: String): String = candidate.filter { it.isLetterOrDigit() }
