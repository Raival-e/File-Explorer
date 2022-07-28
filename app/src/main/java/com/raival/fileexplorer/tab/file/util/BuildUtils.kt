package com.raival.fileexplorer.tab.file.util

import android.content.Context
import android.util.Log
import com.raival.fileexplorer.App
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object BuildUtils {
    private const val BUFFER_SIZE = 1024 * 10
    private const val TAG = "Decompress"
    val lambdaStubsJarFile: File
        get() {
            val check = File(App.appContext.filesDir.toString() + "/build/core-lambda-stubs.jar")
            if (check.exists()) {
                return check
            }
            unzipFromAssets(
                App.appContext,
                "build/lambda-stubs.zip",
                Objects.requireNonNull(check.parentFile).absolutePath
            )
            return check
        }
    val rtJarFile: File
        get() {
            val customRt = File(App.appContext.getExternalFilesDir(null), "build/rt.jar")
            if (customRt.exists() && customRt.isFile) {
                return customRt
            }
            val check = File(App.appContext.filesDir.toString() + "/build/rt.jar")
            if (check.exists()) {
                return check
            }
            unzipFromAssets(
                App.appContext,
                "build/rt.zip",
                Objects.requireNonNull(check.parentFile).absolutePath
            )
            return check
        }

    @JvmStatic
    fun unzipFromAssets(context: Context, zipFile: String, destination: String?) {
        var des = destination
        try {
            if (des == null || des.isEmpty()) des =
                context.filesDir.absolutePath
            val stream = context.assets.open(zipFile)
            unzip(stream, des)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun dirChecker(destination: String?, dir: String) {
        val f = File(destination, dir)
        if (!f.isDirectory) {
            val success = f.mkdirs()
            if (!success) {
                Log.w(TAG, "Failed to create folder " + f.name)
            }
        }
    }

    private fun unzip(stream: InputStream, destination: String?) {
        dirChecker(destination, "")
        val buffer = ByteArray(BUFFER_SIZE)
        try {
            val zin = ZipInputStream(stream)
            var ze: ZipEntry
            while (zin.nextEntry.also { ze = it } != null) {
                Log.v(TAG, "Unzipping " + ze.name)
                if (ze.isDirectory) {
                    dirChecker(destination, ze.name)
                } else {
                    val f = File(destination, ze.name)
                    if (!f.exists()) {
                        val success = f.createNewFile()
                        if (!success) {
                            Log.w(
                                TAG,
                                com.raival.fileexplorer.util.Log.UNABLE_TO + " " + FileUtils.CREATE_FILE + " " + f.name
                            )
                            continue
                        }
                        val fileOutputStream = FileOutputStream(f)
                        var count: Int
                        while (zin.read(buffer).also { count = it } != -1) {
                            fileOutputStream.write(buffer, 0, count)
                        }
                        zin.closeEntry()
                        fileOutputStream.close()
                    }
                }
            }
            zin.close()
        } catch (e: Exception) {
            Log.e(TAG, "unzip", e)
        }
    }
}