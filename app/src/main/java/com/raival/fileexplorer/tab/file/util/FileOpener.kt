package com.raival.fileexplorer.tab.file.util

import android.content.Intent
import com.raival.fileexplorer.App.Companion.showMsg
import com.raival.fileexplorer.activity.MainActivity
import com.raival.fileexplorer.activity.TextEditorActivity
import com.raival.fileexplorer.tab.file.executor.ModuleRunner
import com.raival.fileexplorer.tab.file.extension.isCodeFile
import com.raival.fileexplorer.tab.file.extension.isTextFile
import com.raival.fileexplorer.tab.file.extension.openFileWith
import com.raival.fileexplorer.util.Log
import java.io.File
import java.util.*

class FileOpener(private val mainActivity: MainActivity) {
    fun openFile(file: File) {
        if (!handleKnownFileExtensions(file)) {
            file.openFileWith(false)
        }
    }

    private fun handleKnownFileExtensions(file: File): Boolean {
        if (file.isTextFile() || file.isCodeFile()) {
            val intent = Intent()
            intent.setClass(mainActivity, TextEditorActivity::class.java)
            intent.putExtra("file", file.absolutePath)
            mainActivity.startActivity(intent)
            return true
        }
        if (file.name.lowercase(Locale.getDefault()).endsWith(".extension")) {
            try {
                ModuleRunner(file, mainActivity).run()
            } catch (e: Exception) {
                Log.e(TAG, e)
                showMsg(Log.SOMETHING_WENT_WRONG + ", check logs for more details")
            }
            return true
        }
        return false
    }

    companion object {
        private val TAG = FileOpener::class.java.simpleName
    }
}