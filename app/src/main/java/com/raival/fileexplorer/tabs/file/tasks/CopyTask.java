package com.raival.fileexplorer.tabs.file.tasks;

import android.os.Handler;
import android.os.Looper;

import com.raival.fileexplorer.tabs.file.model.Task;
import com.raival.fileexplorer.utils.FileUtil;

import java.io.File;
import java.util.ArrayList;

public class CopyTask extends Task {
    private final ArrayList<File> filesToCopy;
    private File activeDirectory;

    public CopyTask(ArrayList<File> filesToCopy) {
        this.filesToCopy = filesToCopy;
    }

    public ArrayList<File> getFilesToCopy() {
        return filesToCopy;
    }

    @Override
    public String getName() {
        return "Copy";
    }

    @Override
    public String getDetails() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for (File file : filesToCopy) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(file.getName());
            first = false;
        }
        return sb.toString();
    }

    @Override
    public boolean isValid() {
        for (File file : filesToCopy) {
            if (!file.exists())
                return false;
        }
        return true;
    }

    @Override
    public void setActiveDirectory(File directory) {
        activeDirectory = directory;
    }

    @Override
    public void start(OnUpdateListener onUpdateListener, OnFinishListener onFinishListener) {
        new Thread(() -> {
            boolean error = false;
            try {
                int progress = 1;
                for(File file : filesToCopy){
                    try{
                        int finalProgress = progress;
                        new Handler(Looper.getMainLooper()).post(() -> {
                            onUpdateListener.onUpdate("["
                                    + finalProgress
                                    + "/"
                                    + filesToCopy.size()
                                    + "]"
                                    + "Copying "
                                    + file.getName());
                        });
                        FileUtil.copy(file, activeDirectory);
                    } catch (Exception exception){
                        error = true;
                    }
                    ++progress;
                }
                boolean finalError = error;
                new Handler(Looper.getMainLooper()).post(() -> onFinishListener.onFinish(finalError
                        ? "An error occurred, some files didn't get copied"
                        : "Files copied successfully"));
            } catch (Exception e) {
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() -> onFinishListener.onFinish(e.toString()));
            }
        }).start();
    }
}
