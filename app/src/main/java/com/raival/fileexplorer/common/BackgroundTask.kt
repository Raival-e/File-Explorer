package com.raival.fileexplorer.common

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.raival.fileexplorer.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BackgroundTask {
    private lateinit var preTask: Runnable
    private lateinit var task: Runnable
    private lateinit var postTask: Runnable
    private lateinit var alertDialog: AlertDialog

    fun setTasks(preTask: Runnable, task: Runnable, postTask: Runnable) {
        this.preTask = preTask
        this.task = task
        this.postTask = postTask
    }

    fun run() {
        CoroutineScope(Dispatchers.IO).launch {
            Handler(Looper.getMainLooper()).post(preTask)
            task.run()
            Handler(Looper.getMainLooper()).post(postTask)
        }.start()
    }

    fun dismiss() {
        if (this::alertDialog.isInitialized) alertDialog.dismiss()
    }

    @SuppressLint("ResourceType")
    fun showProgressDialog(msg: String, activity: Activity) {
        alertDialog = MaterialAlertDialogBuilder(activity)
            .setCancelable(false)
            .setView(getProgressView(msg, activity))
            .show()
    }

    private fun getProgressView(msg: String, activity: Activity): View {
        @SuppressLint("InflateParams") val v =
            activity.layoutInflater.inflate(R.layout.progress_view, null)
        (v.findViewById<View>(R.id.msg) as TextView).text = msg
        return v
    }
}