package com.raival.fileexplorer.tabs.file.tasks;

import android.os.Handler;
import android.os.Looper;

import com.raival.fileexplorer.tabs.file.model.Task;
import com.raival.fileexplorer.utils.ZipUtils;

import java.io.File;
import java.util.ArrayList;

public class CompressTask extends Task {
    private final ArrayList<File> filesToCompress;
    private File zipFile;

    public CompressTask(ArrayList<File> filesToCompress) {
        this.filesToCompress = filesToCompress;
    }

    public ArrayList<File> getFilesToCompress() {
        return filesToCompress;
    }

    @Override
    public String getName() {
        return "Compress";
    }

    @Override
    public String getDetails() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for (File file : filesToCompress) {
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
        for (File file : filesToCompress) {
            if (!file.exists())
                return false;
        }
        return true;
    }

    @Override
    public void setActiveDirectory(File directory) {
        zipFile = directory;
    }

    @Override
    public void start(OnUpdateListener onUpdateListener, OnFinishListener onFinishListener) {
        new Thread(() -> {
            new Handler(Looper.getMainLooper()).post(() -> onUpdateListener.onUpdate("Compressing...."));
            try {
                ZipUtils.archive(filesToCompress, zipFile);
                new Handler(Looper.getMainLooper()).post(() -> onFinishListener.onFinish("Files have been compressed successfully"));
            } catch (Exception e) {
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() -> onFinishListener.onFinish(e.toString()));
            }
        }).start();
    }
}
