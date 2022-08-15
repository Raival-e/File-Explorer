package com.raival.fileexplorer.tab.file.task

import android.os.Handler
import android.os.Looper
import com.raival.fileexplorer.tab.file.misc.extract
import com.raival.fileexplorer.tab.file.model.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ExtractTask(private val filesToExtract: ArrayList<File>) : Task() {
    private var activeDirectory: File? = null
    override val name: String
        get() = "Extract"
    override val details: String
        get() {
            val sb = StringBuilder()
            var first = true
            for (file in filesToExtract) {
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
            for (file in filesToExtract) {
                if (!file.exists()) return false
            }
            return true
        }

    override fun setActiveDirectory(directory: File) {
        activeDirectory = directory
    }

    override fun start(onUpdate: OnUpdateListener, onFinish: OnFinishListener) {
        CoroutineScope(Dispatchers.IO).launch {
            Handler(Looper.getMainLooper()).post { onUpdate.onUpdate("Extracting....") }
            try {
                extract(filesToExtract, activeDirectory!!)
                withContext(Dispatchers.Main) { onFinish.onFinish("Files have been extracted successfully") }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) { onFinish.onFinish(e.toString()) }
            }
        }.start()
    }
}