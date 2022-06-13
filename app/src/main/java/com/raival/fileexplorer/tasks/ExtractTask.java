package com.raival.fileexplorer.tasks;

import com.raival.fileexplorer.interfaces.QTask;
import com.raival.fileexplorer.interfaces.RegularTask;

import java.io.File;
import java.util.ArrayList;

public class ExtractTask implements QTask, RegularTask {
    private final ArrayList<File> filesToExtract;

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
    public ArrayList<File> getFilesList() {
        return getFilesToExtract();
    }
}
