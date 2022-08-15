package com.raival.fileexplorer.tab.file.executor

import androidx.appcompat.app.AppCompatActivity
import com.android.tools.r8.D8
import com.raival.fileexplorer.App
import com.raival.fileexplorer.extension.getAllFilesInDir
import com.raival.fileexplorer.tab.file.misc.BuildUtils
import com.raival.fileexplorer.tab.file.misc.FileUtils
import com.raival.fileexplorer.util.Log
import org.eclipse.jdt.internal.compiler.batch.Main
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.config.Services
import java.io.File
import java.io.OutputStream
import java.io.PrintWriter
import java.util.*

class Executor(folder: File, activity: AppCompatActivity) {
    private val javaFiles = ArrayList<File>()
    private val kotlinFiles = ArrayList<File>()
    private val jarFiles = ArrayList<File>()
    private lateinit var project: File
    private lateinit var output: File
    private lateinit var libs: File
    private lateinit var activity: AppCompatActivity

    fun execute() {
        if (!clearOutput()) {
            throw Exception("Failed cleaning output folder")
        }
        if (kotlinFiles.size > 0) {
            compileKotlin()
        }
        if (javaFiles.size > 0) {
            runECJ()
        }
        runD8()
    }

    operator fun invoke() {
        ModuleRunner(File(output, "classes.extension"), activity).apply {
            setProjectDir(project)
            if (this@Executor::libs.isInitialized) setLibsDir(libs)
            run()
        }
    }

    private fun runD8() {
        val opt = ArrayList<String>()
        opt.add("--intermediate")
        opt.add("--lib")
        opt.add(BuildUtils.rtJarFile.absolutePath)
        opt.add("--output")
        opt.add(output.absolutePath)
        val classes = File(output, "classes").getAllFilesInDir("class")
        if (classes.size > 0) opt.addAll(classes)
        D8.main(opt.toTypedArray())

        // Rename dex files to .extension to be able to run them with ExtensionRunner
        for (file in output.listFiles()!!) {
            if (file.isFile) {
                if (file.name.endsWith(".dex")) {
                    val path = file.absolutePath
                    file.renameTo(File(path.substring(0, path.length - 3) + "extension"))
                }
            }
        }
    }

    /**
     * Sources:
     * - https://github.com/Sketchware-Pro/Sketchware-Pro/tree/main/app/src/minApi26/java/mod/hey/studios/compiler/kotlin
     * - https://github.com/MikeAndrson/kotlinc-android
     */
    private fun compileKotlin() {
        val ktHome = File(output, "ktHome")
        if (!ktHome.mkdir()) {
            throw Exception(Log.UNABLE_TO + " " + FileUtils.CREATE_FILE + ": " + ktHome)
        }
        val classes = File(output, "classes")
        if (!classes.mkdir()) {
            throw Exception(Log.UNABLE_TO + " " + FileUtils.CREATE_FILE + ": " + classes)
        }
        val k2JVMCompiler = K2JVMCompiler()
        val messageCollector = DiagnosticCollector()
        val args = ArrayList<String>()
        args.add("-cp")
        val sb = StringBuilder()
        for (jar in jarFiles) sb.append(":").append(jar.absolutePath)
        sb.append(":").append(BuildUtils.rtJarFile.absolutePath)
        args.add(sb.substring(1))
        for (file in kotlinFiles) {
            args.add(file.absolutePath)
        }
        for (file in javaFiles) {
            args.add(file.absolutePath)
        }
        val compilerArguments = K2JVMCompilerArguments().apply {
            compileJava = false
            includeRuntime = false
            noJdk = true
            noReflect = true
            noStdlib = true
            kotlinHome = ktHome.absolutePath
            destination = classes.absolutePath
        }
        k2JVMCompiler.parseArguments(args.toTypedArray(), compilerArguments)
        k2JVMCompiler.exec(messageCollector, Services.EMPTY, compilerArguments)

        val file = File(classes, "META-INF")
        if (file.exists()) FileUtils.deleteFile(file)
        if (messageCollector.hasErrors()) {
            throw Exception(messageCollector.getDiagnostics())
        }
    }

    private fun runECJ() {
        val opt = ArrayList<String>()
        val classes = File(output, "classes")
        if (!classes.mkdir()) {
            throw Exception(Log.UNABLE_TO + " " + FileUtils.CREATE_FILE + ": " + classes)
        }

        opt.add("-11")
        opt.add("-nowarn")
        opt.add("-deprecation")
        opt.add("-d")
        opt.add(classes.absolutePath)
        opt.add("-cp")

        val sb = StringBuilder()
        for (jar in jarFiles) sb.append(":").append(jar.absolutePath)

        sb.append(":")
            .append(classes.absolutePath)
            .append(":")
            .append(BuildUtils.lambdaStubsJarFile.absolutePath)
            .append(":")
            .append(BuildUtils.rtJarFile.absolutePath)

        opt.add(sb.substring(1))
        opt.add("-proc:none")
        opt.add("-sourcepath")
        opt.add(" ")

        for (file in javaFiles) opt.add(file.absolutePath)
        opt.add("-warn:none")

        val printWriter = PrintWriter(object : OutputStream() {
            override fun write(i: Int) {}
        })
        val errors = StringBuilder()
        val printWriter1 = PrintWriter(object : OutputStream() {
            override fun write(i: Int) {
                errors.append(i.toChar())
            }
        })
        val main = Main(
            printWriter,
            printWriter1,
            false,
            null,
            null
        )
        main.compile(opt.toTypedArray())
        if (main.globalErrorsCount > 0) {
            throw Exception(errors.toString())
        }
    }

    private fun clearOutput(): Boolean {
        if (!this::output.isInitialized) {
            output = File(project, "output")
            return output.mkdir()
        }
        FileUtils.deleteFile(output)
        return output.mkdir()
    }

    private val commonLibs: Unit
        get() {
            val libs = File(App.appContext.getExternalFilesDir(null), "build/libs")
            if (libs.exists() && libs.isDirectory) {
                for (file in libs.listFiles()!!) {
                    if (file.isFile && file.extension == "jar") {
                        jarFiles.add(file)
                    }
                }
            }
        }

    private fun parseInputFolder(input: File) {
        for (file in input.listFiles()!!) {
            if (file.isFile) {
                if (file.name.lowercase(Locale.getDefault()).endsWith(".java")) {
                    javaFiles.add(file)
                } else if (file.name.lowercase(Locale.getDefault()).endsWith(".kt")) {
                    kotlinFiles.add(file)
                }
            } else {
                if (file.name == "output") {
                    output = file
                } else if (file.name == "libs") {
                    libs = file
                    for (subFile in file.listFiles()!!) {
                        if (subFile.isFile) {
                            if (subFile.name.lowercase(Locale.getDefault()).endsWith(".jar")) {
                                jarFiles.add(subFile)
                            }
                        }
                    }
                }
            }
        }
        addCommonLibs()
    }

    private fun addCommonLibs() {
        val commonLibs = File(App.appContext.getExternalFilesDir(null), "build/libs")
        if (commonLibs.exists() && commonLibs.isDirectory) {
            for (file in commonLibs.listFiles()!!) {
                if (file.isFile) {
                    if (file.name.endsWith(".jar")) {
                        jarFiles.add(file)
                    }
                }
            }
        }
    }

    /**
     * Sources:
     * - https://github.com/Sketchware-Pro/Sketchware-Pro/tree/main/app/src/minApi26/java/mod/hey/studios/compiler/kotlin
     * - https://github.com/MikeAndrson/kotlinc-android
     */
    private class DiagnosticCollector : MessageCollector {
        private val diagnostics = ArrayList<Diagnostic>()
        override fun clear() {
            diagnostics.clear()
        }

        override fun report(
            severity: CompilerMessageSeverity,
            message: String,
            location: CompilerMessageSourceLocation?
        ) {
            diagnostics.add(Diagnostic(severity, message, location))
        }

        override fun hasErrors(): Boolean {
            for (diagnostic in diagnostics) {
                if (diagnostic.compilerMessageSeverity.isError) return true
            }
            return false
        }

        fun getDiagnostics(): String {
            val sb = StringBuilder()
            var i = 1
            for (diagnostic in diagnostics) {
                if (diagnostic.compilerMessageSeverity.isError) {
                    sb.apply {
                        append("----------")
                        append(System.lineSeparator())
                        append(i)
                        append(". ")
                        append(diagnostic)
                        append(System.lineSeparator())
                    }
                    i++
                }
            }
            return sb.toString()
        }

        override fun toString(): String {
            return getDiagnostics()
        }
    }

    /**
     * Sources:
     * - https://github.com/Sketchware-Pro/Sketchware-Pro/tree/main/app/src/minApi26/java/mod/hey/studios/compiler/kotlin
     * - https://github.com/MikeAndrson/kotlinc-android
     */
    private class Diagnostic(
        val compilerMessageSeverity: CompilerMessageSeverity,
        val message: String,
        val compilerMessageSourceLocation: CompilerMessageSourceLocation?
    ) {
        override fun toString(): String {
            val stringBuilder = StringBuilder()
            stringBuilder.append(compilerMessageSeverity.presentableName)
            if (compilerMessageSourceLocation != null) {
                stringBuilder.append(" in ")
                    .append(compilerMessageSourceLocation.path)
                    .append(" (at ")
                    .append(compilerMessageSourceLocation.line)
                    .append(":")
                    .append(compilerMessageSourceLocation.column)
                    .append(")")
                    .append(System.lineSeparator())
                val content = compilerMessageSourceLocation.lineContent
                if (content != null) stringBuilder.append(content)
            }
            stringBuilder.append(System.lineSeparator()).append(message)
            return stringBuilder.toString()
        }
    }

    init {
        if (!folder.isFile) {
            this.activity = activity
            project = folder
            parseInputFolder(project)
            commonLibs
        }
    }
}