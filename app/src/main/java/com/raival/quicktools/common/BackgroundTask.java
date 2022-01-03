package com.raival.quicktools.common;

import android.os.Handler;
import android.os.Looper;

public class BackgroundTask {
    Runnable preTask, task, postTask;

    public BackgroundTask(Runnable preTask, Runnable task, Runnable postTask){
        this.preTask = preTask;
        this.task = task;
        this.postTask = postTask;
    }

    public BackgroundTask run(){
        new Thread(()->{
            new Handler(Looper.getMainLooper()).post(preTask);
            task.run();
            new Handler(Looper.getMainLooper()).post(postTask);
        }).start();
        return this;
    }
}
