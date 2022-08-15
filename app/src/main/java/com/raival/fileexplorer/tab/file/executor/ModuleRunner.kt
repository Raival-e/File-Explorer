package com.raival.fileexplorer.tab.file.executor

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.raival.fileexplorer.App
import dalvik.system.DexClassLoader
import java.io.File
import java.lang.reflect.Modifier

class ModuleRunner(dexFile: File, private val activity: AppCompatActivity) {
    private val relatedDexFiles = ArrayList<File>()
    private var directory: File?

    init {
        directory = dexFile.parentFile
        assert(directory != null)

        val files = directory?.listFiles()
        val prefix = dexFile.name.substring(0, dexFile.name.indexOf(".extension"))
        if (files != null) {
            for (file in files) {
                if (file.name.endsWith(".extension")) {
                    if (file.name.startsWith(prefix)) relatedDexFiles.add(file)
                }
            }
        }
        addCommonDexFiles()
    }

    private fun addCommonDexFiles() {
        val commonLibs = File(App.appContext.getExternalFilesDir(null), "build/libs")
        if (commonLibs.exists() && commonLibs.isDirectory) {
            for (file in commonLibs.listFiles()!!) {
                if (file.isFile) {
                    if (file.name.endsWith(".dex")) {
                        relatedDexFiles.add(file)
                    }
                }
            }
        }
    }

    fun setProjectDir(file: File): ModuleRunner {
        if (file.isDirectory) directory = file
        return this
    }

    fun run() {
        val optimizedDir = App.appContext.codeCacheDir.absolutePath
        val dexClassLoader = DexClassLoader(
            dexFiles,
            optimizedDir,
            null,
            App.appContext.classLoader
        )
        val clazz = dexClassLoader.loadClass("com.main.Main")

        // Look for a public static method called `main` and invoke it
        for (method in clazz.methods) {
            if (method.name == "main" && method.modifiers > Modifier.STATIC) {
                val params = ArrayList<Any?>()
                for (obj in method.parameterTypes) {
                    when (obj) {
                        AppCompatActivity::class.java -> {
                            params.add(activity)
                        }
                        Activity::class.java -> {
                            params.add(activity)
                        }
                        Context::class.java -> {
                            params.add(activity)
                        }
                        File::class.java -> {
                            params.add(directory)
                        }
                        else -> {
                            params.add(null)
                        }
                    }
                }
                method.invoke(null, *params.toTypedArray())
                return
            }
        }

        // if the specified method doesn't exist, create an instance of the Main class instead
        val method = clazz.constructors[0]
        val params = ArrayList<Any?>()
        for (obj in method.parameterTypes) {
            when (obj) {
                AppCompatActivity::class.java -> {
                    params.add(activity)
                }
                Activity::class.java -> {
                    params.add(activity)
                }
                Context::class.java -> {
                    params.add(activity)
                }
                File::class.java -> {
                    params.add(directory)
                }
                else -> {
                    params.add(null)
                }
            }
        }
        method.newInstance(*params.toTypedArray())
    }

    private val dexFiles: String
        get() {
            val stringBuilder = StringBuilder()
            for (file in relatedDexFiles) {
                stringBuilder.append(":")
                stringBuilder.append(file.absolutePath)
            }
            return stringBuilder.substring(1)
        }

    fun setLibsDir(libs: File?): ModuleRunner {
        if (libs != null) {
            if (libs.isDirectory) {
                for (file in libs.listFiles()!!) {
                    if (file.isFile) {
                        if (file.name.endsWith(".dex")) relatedDexFiles.add(file)
                    }
                }
            }
        }
        return this
    }
}