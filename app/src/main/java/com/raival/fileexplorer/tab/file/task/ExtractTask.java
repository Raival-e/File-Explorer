package com.raival.fileexplorer.tab.file.task;

import android.os.Handler;
import android.os.Looper;

import com.raival.fileexplorer.tab.file.model.Task;
import com.raival.fileexplorer.util.ZipUtils;

import java.io.File;
import java.util.ArrayList;

public class ExtractTask extends Task {
    private final ArrayList<File> filesToExtract;
    private File activeDirectory;

    public ExtractTask(ArrayList<File> filesToExtract) {
        this.filesToExtract = filesToExtract;
    }

    public ArrayList<File> getFilesToExtract() {
        return filesToExtract;
    }

    @Override
    public String getName() {
        return "Extract";
    }

    @Override
    public String getDetails() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for (File file : filesToExtract) {
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
        for (File file : filesToExtract) {
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
            new Handler(Looper.getMainLooper()).post(() -> onUpdateListener.onUpdate("Extracting...."));
            try {
                ZipUtils.extract(filesToExtract, activeDirectory);
                new Handler(Looper.getMainLooper()).post(() -> onFinishListener.onFinish("Files have been extracted successfully"));
            } catch (Exception e) {
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() -> onFinishListener.onFinish(e.toString()));
            }
        }).start();
    }
}
