/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.scripting.compiler.plugin

import com.intellij.openapi.util.SystemInfo
import org.jetbrains.kotlin.cli.common.CLICompiler
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.common.arguments.cliArgument
import org.jetbrains.kotlin.cli.common.environment.setIdeaIoUseFallback
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.scripting.compiler.test.linesSplitTrim
import java.io.File
import java.net.URLClassLoader
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScriptingWithCliCompilerTest {

    companion object {
        const val TEST_DATA_DIR = "plugins/scripting/scripting-compiler/testData"
        val SIMPLE_TEST_SCRIPT = "$TEST_DATA_DIR/compiler/mixedCompilation/simpleScript.main.kts"
    }

    init {
        setIdeaIoUseFallback()
    }

    @Test
    fun testResultValue() {
        runWithK2JVMCompiler("$TEST_DATA_DIR/integration/intResult.kts", listOf("10"))
    }

    @Test
    fun testResultValueViaKotlinc() {
        runWithKotlinc("$TEST_DATA_DIR/integration/intResult.kts", listOf("10"))
    }

    @Test
    fun testStandardScriptWithDeps() {
        runWithK2JVMCompiler(
            "$TEST_DATA_DIR/integration/withDependencyOnCompileClassPath.kts", listOf("Hello from standard kts!"),
            classpath = getMainKtsClassPath()
        )
    }

    @Test
    fun testCompileMainKtsWithDependsOn() {
        withTempDir { tmpdir ->
            runWithK2JVMCompiler(
                arrayOf(
                    K2JVMCompilerArguments::destination.cliArgument,
                    tmpdir.absolutePath,
                    K2JVMCompilerArguments::classpath.cliArgument,
                    getMainKtsClassPath().joinToString(File.pathSeparator),
                    K2JVMCompilerArguments::allowAnyScriptsInSourceRoots.cliArgument,
                    K2JVMCompilerArguments::useFirLT.cliArgument("false"),
                    "$TEST_DATA_DIR/integration/hello-resolve-junit.main.kts",
                ),
            )
        }
    }

    @Test
    fun testStandardScriptWithDepsViaKotlinc() {
        runWithKotlinc(
            "$TEST_DATA_DIR/integration/withDependencyOnCompileClassPath.kts", listOf("Hello from standard kts!"),
            classpath = getMainKtsClassPath()
        )
    }

    @Test
    fun testExpression() {
        runWithK2JVMCompiler(
            arrayOf(
                "-expression",
                "val x = 7; println(x * 6); for (arg in args) println(arg)",
                "--",
                "hi",
                "there"
            ),
            listOf("42", "hi", "there")
        )
    }

    @Test
    fun testExpressionAsMainKts() {
        // testing that without specifying default to .main.kts, the annotation is unresolved
        runWithK2JVMCompiler(
            arrayOf(
                K2JVMCompilerArguments::classpath.cliArgument, getMainKtsClassPath().joinToString(File.pathSeparator),
                "-expression=@file:CompilerOptions(\"-Xunknown1\")"
            ),
            expectedExitCode = 1,
            expectedSomeErrPatterns = listOf(
                "unresolved reference\\W*CompilerOptions"
            ),
        )
        runWithK2JVMCompiler(
            arrayOf(
                K2JVMCompilerArguments::classpath.cliArgument, getMainKtsClassPath().joinToString(File.pathSeparator),
                K2JVMCompilerArguments::defaultScriptExtension.cliArgument(".main.kts"),
                "-expression=@file:CompilerOptions(\"-Xunknown1\")"
            ),
            expectedExitCode = 0,
        )
    }

    @Test
    fun testScriptAsMainKts() {
        val scriptFile = Files.createTempFile("someScript", "").toFile()
        scriptFile.writeText("@file:CompilerOptions(\"-abracadabra\")\n42")

        // testing that without specifying default to .main.kts the script with extension .txt is not recognized
        runWithK2JVMCompiler(
            arrayOf(
                K2JVMCompilerArguments::classpath.cliArgument, getMainKtsClassPath().joinToString(File.pathSeparator),
                K2JVMCompilerArguments::script.cliArgument,
                scriptFile.path
            ),
            expectedExitCode = 1,
            expectedSomeErrPatterns = listOf(
                "unrecognized script type: someScript.+"
            )
        )
        runWithK2JVMCompiler(
            arrayOf(
                K2JVMCompilerArguments::classpath.cliArgument, getMainKtsClassPath().joinToString(File.pathSeparator),
                K2JVMCompilerArguments::defaultScriptExtension.cliArgument(".main.kts"),
                K2JVMCompilerArguments::script.cliArgument,
                scriptFile.path
            ),
            expectedExitCode = 1,
            expectedSomeErrPatterns = listOf(
                "error: invalid argument: -abracadabra"
            )
        )
        runWithK2JVMCompiler(
            arrayOf(
                K2JVMCompilerArguments::classpath.cliArgument, getMainKtsClassPath().joinToString(File.pathSeparator),
                K2JVMCompilerArguments::defaultScriptExtension.cliArgument("main.kts"),
                K2JVMCompilerArguments::script.cliArgument,
                scriptFile.path
            ),
            expectedExitCode = 1,
            expectedSomeErrPatterns = listOf(
                "error: invalid argument: -abracadabra"
            )
        )
    }

    @Test
    fun testExpressionWithComma() {
        runWithK2JVMCompiler(
            arrayOf(
                "-expression",
                "listOf(1,2)"
            ),
            listOf("\\[1, 2\\]"),
        )
    }

    @Test
    fun testJdkModules() {
        // actually tests anything on JDKs 9+, on pre-9 it always works because JDK is not modularized anyway
        runWithKotlinc(
            arrayOf(
                "-Xadd-modules=java.sql",
                "-expression",
                "println(javax.sql.DataSource::class.java)"
            ),
            listOf("interface javax.sql.DataSource")
        )
    }

    @Test
    fun testExceptionWithCause() {
        val (_, err, _) = captureOutErrRet {
            CLICompiler.doMainNoExit(
                K2JVMCompiler(),
                arrayOf(
                    K2JVMCompilerArguments::script.cliArgument,
                    "$TEST_DATA_DIR/integration/exceptionWithCause.kts"
                )
            )
        }
        val filteredErr = err.linesSplitTrim().filterNot { it.startsWith("WARN: ") }
        assertContentEquals(
            """
                java.lang.Exception: Top
	                    at ExceptionWithCause.<init>(exceptionWithCause.kts:8)
                Caused by: java.lang.Exception: Oh no
	                    at ExceptionWithCause.<init>(exceptionWithCause.kts:5)
                Caused by: java.lang.Exception: Error!
	                    at ExceptionWithCause.<init>(exceptionWithCause.kts:3)
            """.trimIndent().linesSplitTrim(),
            filteredErr
        )
    }

    @Test
    fun testAccessRegularSourceFromScript() {
        withTempDir { tmpdir ->
            val scriptPath = "$TEST_DATA_DIR/compiler/mixedCompilation/scriptAccessingNonScript.main.kts"
            val ret =
                CLICompiler.doMainNoExit(
                    K2JVMCompiler(),
                    arrayOf(
                        "-P", "plugin:kotlin.scripting:disable-script-definitions-autoloading=true",
                        K2JVMCompilerArguments::classpath.cliArgument, getMainKtsClassPath().joinToString(File.pathSeparator), K2JVMCompilerArguments::destination.cliArgument, tmpdir.path,
                        K2JVMCompilerArguments::useFirLT.cliArgument("false"),
                        K2JVMCompilerArguments::allowAnyScriptsInSourceRoots.cliArgument,
                        K2JVMCompilerArguments::verbose.cliArgument,
                        "$TEST_DATA_DIR/compiler/mixedCompilation/nonScript.kt",
                        scriptPath,
                    )
                )
            assertEquals(ExitCode.OK.code, ret.code)
            val (out, _, _) = captureOutErrRet {
                val cl = URLClassLoader((getMainKtsClassPath() + tmpdir).map { it.toURI().toURL() }.toTypedArray())
                val klass = cl.loadClass("ScriptAccessingNonScript_main")
                val ctor = klass.constructors.single()
                ctor.newInstance(arrayOf<String>(), File(scriptPath))
            }
            assertEquals("OK", out.trim())
        }
    }

    @Test
    fun testAccessScriptFromRegularSource() {
        withTempDir { tmpdir ->
            val (_, err, ret) = captureOutErrRet {
                CLICompiler.doMainNoExit(
                    K2JVMCompiler(),
                    arrayOf(
                        "-P", "plugin:kotlin.scripting:disable-script-definitions-autoloading=true",
                        K2JVMCompilerArguments::classpath.cliArgument, getMainKtsClassPath().joinToString(File.pathSeparator), K2JVMCompilerArguments::destination.cliArgument, tmpdir.path,
                        K2JVMCompilerArguments::useFirLT.cliArgument("false"),
                        K2JVMCompilerArguments::allowAnyScriptsInSourceRoots.cliArgument,
                        K2JVMCompilerArguments::verbose.cliArgument,
                        "$TEST_DATA_DIR/compiler/mixedCompilation/nonScriptAccessingScript.kt",
                        SIMPLE_TEST_SCRIPT
                    )
                )
            }
            assertTrue(err.contains("error: unresolved reference 'SimpleScript_main'"), "Expecting an error about unresolved 'SimpleScript_main', got:\n$err")
            assertTrue(err.contains("error: unresolved reference 'ok'"), "Expecting an error about unresolved 'ok', got:\n$err")
            assertEquals(ExitCode.COMPILATION_ERROR.code, ret.code)
        }
    }

    @Test
    fun testScriptMainKtsDiscovery() {
        withTempDir { tmpdir ->

            fun compileSuccessfullyGetStdErr(fileArg: String): List<String> {
                val (_, err, ret) = captureOutErrRet {
                    CLICompiler.doMainNoExit(
                        K2JVMCompiler(),
                        arrayOf(
                            "-P",
                            "plugin:kotlin.scripting:disable-script-definitions-autoloading=true",
                            K2JVMCompilerArguments::classpath.cliArgument,
                            getMainKtsClassPath().joinToString(File.pathSeparator),
                            K2JVMCompilerArguments::destination.cliArgument,
                            tmpdir.path,
                            K2JVMCompilerArguments::useFirLT.cliArgument("false"),
                            K2JVMCompilerArguments::allowAnyScriptsInSourceRoots.cliArgument,
                            K2JVMCompilerArguments::verbose.cliArgument,
                            fileArg
                        )
                    )
                }
                assertEquals(ExitCode.OK.code, ret.code)
                return err.linesSplitTrim()
            }

            val loadMainKtsMessage = "logging: configure scripting: loading script definition class org.jetbrains.kotlin.mainKts.MainKtsScript using classpath"

            val res1 = compileSuccessfullyGetStdErr("$TEST_DATA_DIR/compiler/mixedCompilation/nonScript.kt")
            assertTrue(res1.none { it.startsWith(loadMainKtsMessage) })

            val res2 = compileSuccessfullyGetStdErr(SIMPLE_TEST_SCRIPT)
            assertTrue(res2.any { it.startsWith(loadMainKtsMessage) })
        }
    }

    @Test
    fun testWithAllOpenViaLegacyPluginOptions() {
        // fails on K1, see KT-74390
        if (System.getProperty(SCRIPT_TEST_BASE_COMPILER_ARGUMENTS_PROPERTY)?.contains("-language-version 1.9") == true) return

        val quoteForWin = if (SystemInfo.isWindows) "\"" else ""
        runWithKotlinc(
            arrayOf(
                "-Xplugin=dist/kotlinc/lib/allopen-compiler-plugin.jar",
                "-P", "${quoteForWin}plugin:org.jetbrains.kotlin.allopen:annotation=AllOpen$quoteForWin",
                "-script", "$TEST_DATA_DIR/integration/withAllOpenPlugin.kts",
            ), listOf("OK")
        )
    }

    @Test
    fun testWithAllOpen() {
        // this plugin syntax is not supported in K1
        if (System.getProperty(SCRIPT_TEST_BASE_COMPILER_ARGUMENTS_PROPERTY)?.contains("-language-version 1.9") == true) return

        val quoteForWin = if (SystemInfo.isWindows) "\"" else ""
        runWithKotlinc(
            arrayOf(
                "-Xcompiler-plugin=${quoteForWin}dist/kotlinc/lib/allopen-compiler-plugin.jar=annotation=AllOpen$quoteForWin",
                "-script", "$TEST_DATA_DIR/integration/withAllOpenPlugin.kts",
            ), listOf("OK")
        )
    }

    private fun getMainKtsClassPath(): List<File> {
        return listOf(
            File("dist/kotlinc/lib/kotlin-main-kts.jar").also {
                assertTrue(it.exists(), "kotlin-main-kts.jar not found, run dist task: ${it.absolutePath}")
            }
        )
    }
}

