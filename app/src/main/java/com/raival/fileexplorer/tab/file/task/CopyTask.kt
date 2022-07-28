package com.raival.fileexplorer.tab.file.task

import android.os.Handler
import android.os.Looper
import com.raival.fileexplorer.tab.file.model.Task
import com.raival.fileexplorer.tab.file.util.FileUtils
import java.io.File

class CopyTask(private val filesToCopy: ArrayList<File>) : Task() {
    private var activeDirectory: File? = null
    override val name: String
        get() = "Copy"
    override val details: String
        get() {
            val sb = StringBuilder()
            var first = true
            for (file in filesToCopy) {
                if (!first) {
                    sb.append(", ")
                }
                sb.append(file.name)
                first = false
            }
            return sb.toString()
        }
    override val isValid: Boolean
        get() {
            for (file in filesToCopy) {
                if (!file.exists()) return false
            }
            return true
        }

    override fun setActiveDirectory(directory: File) {
        activeDirectory = directory
    }

    override fun start(onUpdate: OnUpdateListener, onFinish: OnFinishListener) {
        Thread {
            var error = false
            try {
                var progress = 1
                for (file in filesToCopy) {
                    try {
                        val finalProgress = progress
                        Handler(Looper.getMainLooper()).post {
                            onUpdate.onUpdate(
                                "["
                                        + finalProgress
                                        + "/"
                                        + filesToCopy.size
                                        + "]"
                                        + "Copying "
                                        + file.name
                            )
                        }
                        FileUtils.copy(file, activeDirectory!!, true)
                    } catch (exception: Exception) {
                        error = true
                    }
                    ++progress
                }
                val finalError = error
                Handler(Looper.getMainLooper()).post { onFinish.onFinish(if (finalError) "An error occurred, some files didn't get copied" else "Files copied successfully") }
            } catch (e: Exception) {
                e.printStackTrace()
                Handler(Looper.getMainLooper()).post { onFinish.onFinish(e.toString()) }
            }
        }.start()
    }
}