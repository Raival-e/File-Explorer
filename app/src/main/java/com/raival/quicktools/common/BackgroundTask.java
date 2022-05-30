package com.raival.quicktools.common;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.raival.quicktools.R;

public class BackgroundTask {
    Runnable preTask, task, postTask;
    AlertDialog alertDialog;

    public BackgroundTask() {

    }

    public void setTasks(Runnable preTask, Runnable task, Runnable postTask) {
        this.preTask = preTask;
        this.task = task;
        this.postTask = postTask;
    }

    public BackgroundTask run() {
        new Thread(() -> {
            new Handler(Looper.getMainLooper()).post(preTask);
            task.run();
            new Handler(Looper.getMainLooper()).post(postTask);
        }).start();
        return this;
    }

    public void dismiss() {
        if (alertDialog != null)
            alertDialog.dismiss();
    }

    @SuppressLint("ResourceType")
    public void showProgressDialog(String msg, Activity activity) {
        alertDialog = new MaterialAlertDialogBuilder(activity)
                .setCancelable(false)
                .setView(getProgressView(msg, activity))
                .show();
    }

    private View getProgressView(String msg, Activity activity) {
        View v = activity.getLayoutInflater().inflate(R.layout.progress_view, null);
        ((TextView) v.findViewById(R.id.msg)).setText(msg);
        return v;
    }
}
