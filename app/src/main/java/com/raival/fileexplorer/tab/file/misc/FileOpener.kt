package com.raival.fileexplorer.tab.file.misc

import android.content.Intent
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.raival.fileexplorer.activity.MainActivity
import com.raival.fileexplorer.activity.TextEditorActivity
import com.raival.fileexplorer.extension.openFileWith
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class FileOpener(private val mainActivity: MainActivity) {
    fun openFile(file: File) {
        if (!handleKnownFileExtensions(file)) {
            file.openFileWith(false)
        }
    }

    private fun handleKnownFileExtensions(file: File): Boolean {
        if (FileMimeTypes.textType.contains(file.extension.lowercase())
            || FileMimeTypes.codeType.contains(file.extension.lowercase())
        ) {
            val intent = Intent()
            intent.setClass(mainActivity, TextEditorActivity::class.java)
            intent.putExtra("file", file.absolutePath)
            mainActivity.startActivity(intent)
            return true
        }
        if (file.extension == FileMimeTypes.apkType) {
            val dialog = MaterialAlertDialogBuilder(mainActivity)
                .setMessage("Do you want to install this app?")
                .setPositiveButton("Install") { _, _ -> file.openFileWith(false) }
            CoroutineScope(Dispatchers.Main).launch {
                dialog.setTitle(FileUtils.getApkName(file))
                dialog.setIcon(FileUtils.getApkIcon(file))
                dialog.show()
            }
            return true
        }
        return false
    }


    companion object {
        private val TAG = FileOpener::class.java.simpleName
    }
}