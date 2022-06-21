package com.raival.fileexplorer.tabs.file.tasks;

import android.os.Handler;
import android.os.Looper;

import com.raival.fileexplorer.tabs.file.model.Task;
import com.raival.fileexplorer.utils.FileUtil;

import java.io.File;
import java.util.ArrayList;

public class CutTask extends Task {
    private final ArrayList<File> filesToCut;
    private File activeDirectory;

    public CutTask(ArrayList<File> selectedFiles) {
        filesToCut = selectedFiles;
    }

    public ArrayList<File> getFilesToCut() {
        return filesToCut;
    }

    @Override
    public String getName() {
        return "Cut";
    }

    @Override
    public String getDetails() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for (File file : filesToCut) {
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
        for (File file : filesToCut) {
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
                for(File file : filesToCut){
                    try{
                        int finalProgress = progress;
                        new Handler(Looper.getMainLooper()).post(() -> {
                            onUpdateListener.onUpdate("["
                                    + finalProgress
                                    + "/"
                                    + filesToCut.size()
                                    + "]"
                                    + "Moving "
                                    + file.getName());
                        });
                        FileUtil.move(file, activeDirectory);
                    } catch (Exception exception){
                        error = true;
                    }
                    ++progress;
                }
                boolean finalError = error;
                new Handler(Looper.getMainLooper()).post(() -> onFinishListener.onFinish(finalError
                        ? "An error occurred, some files haven't been moved"
                        : "Files have been moved successfully"));
            } catch (Exception e) {
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() -> onFinishListener.onFinish(e.toString()));
            }
        }).start();
    }
}
