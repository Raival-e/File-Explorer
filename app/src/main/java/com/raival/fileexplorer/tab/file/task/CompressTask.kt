package com.raival.fileexplorer.tab.file.task

import com.raival.fileexplorer.tab.file.misc.archive
import com.raival.fileexplorer.tab.file.model.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                onUpdate.onUpdate("Compressing....")
            }
            try {
                archive(filesToCompress, zipFile)
                withContext(Dispatchers.Main) {
                    onFinish.onFinish("Files have been compressed successfully")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onFinish.onFinish(e.toString())
                }
            }
        }
    }
}