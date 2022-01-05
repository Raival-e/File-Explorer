package com.raival.quicktools.common;

import android.os.Handler;
import android.os.Looper;

public class BackgroundTask {
    Runnable preTask, task, postTask;
    String name;
    String details;

    public BackgroundTask(String name, String details){
        this.name = name;
        this.details = details;
    }

    public BackgroundTask setTasks(Runnable preTask, Runnable task, Runnable postTask){
        this.preTask = preTask;
        this.task = task;
        this.postTask = postTask;
        return this;
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
