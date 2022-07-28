package com.raival.fileexplorer.tab.file.task

import android.os.Handler
import android.os.Looper
import com.raival.fileexplorer.tab.file.model.Task
import com.raival.fileexplorer.tab.file.util.archive
import java.io.File

class CompressTask(private val filesToCompress: ArrayList<File>) : Task() {
    private var zipFile: File? = null
    override val name: String
        get() = "Compress"
    override val details: String
        get() {
            val sb = StringBuilder()
            var first = true
            for (file in filesToCompress) {
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
            for (file in filesToCompress) {
                if (!file.exists()) return false
            }
            return true
        }

    override fun setActiveDirectory(directory: File) {
        zipFile = directory
    }

    override fun start(onUpdate: OnUpdateListener, onFinish: OnFinishListener) {
        Thread {
            Handler(Looper.getMainLooper()).post { onUpdate.onUpdate("Compressing....") }
            try {
                archive(filesToCompress, zipFile)
                Handler(Looper.getMainLooper()).post { onFinish.onFinish("Files have been compressed successfully") }
            } catch (e: Exception) {
                e.printStackTrace()
                Handler(Looper.getMainLooper()).post { onFinish.onFinish(e.toString()) }
            }
        }.start()
    }
}