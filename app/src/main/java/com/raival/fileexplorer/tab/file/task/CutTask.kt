package com.raival.fileexplorer.tab.file.task

import com.raival.fileexplorer.tab.file.misc.FileUtils
import com.raival.fileexplorer.tab.file.model.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class CutTask(private val filesToCut: ArrayList<File>) : Task() {
    private var activeDirectory: File? = null
    override val name: String
        get() = "Cut"
    override val details: String
        get() {
            val sb = StringBuilder()
            var first = true
            for (file in filesToCut) {
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
            for (file in filesToCut) {
                if (!file.exists()) return false
            }
            return true
        }

    override fun setActiveDirectory(directory: File) {
        activeDirectory = directory
    }

    override fun start(onUpdate: OnUpdateListener, onFinish: OnFinishListener) {
        CoroutineScope(Dispatchers.IO).launch {
            var error = false
            try {
                var progress = 1
                for (file in filesToCut) {
                    try {
                        val finalProgress = progress
                        withContext(Dispatchers.Main) {
                            onUpdate.onUpdate(
                                "["
                                        + finalProgress
                                        + "/"
                                        + filesToCut.size
                                        + "]"
                                        + "Moving "
                                        + file.name
                            )
                        }
                        FileUtils.move(file, activeDirectory)
                    } catch (exception: Exception) {
                        error = true
                    }
                    ++progress
                }
                val finalError = error
                withContext(Dispatchers.Main) { onFinish.onFinish(if (finalError) "An error occurred, some files haven't been moved" else "Files have been moved successfully") }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) { onFinish.onFinish(e.toString()) }
            }
        }
    }
}