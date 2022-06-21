package com.raival.fileexplorer.tabs.file.model;

import java.io.File;
import java.util.ArrayList;

public abstract class Task {
    public interface OnUpdateListener{
        void onUpdate(String progress);
    }
    public interface OnFinishListener{
        void onFinish(String result);
    }

    public abstract String getName();
    public abstract String getDetails();
    public abstract boolean isValid();
    public abstract void setActiveDirectory(File directory);
    public abstract void start(OnUpdateListener onUpdate, OnFinishListener onFinish);
}
