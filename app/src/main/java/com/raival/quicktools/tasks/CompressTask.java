package com.raival.quicktools.tasks;

import com.raival.quicktools.interfaces.QTask;
import com.raival.quicktools.interfaces.RegularTask;

import java.io.File;
import java.util.ArrayList;

public class CompressTask implements QTask, RegularTask {
    ArrayList<File> filesToCompress;

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
    public ArrayList<File> getFilesList() {
        return getFilesToCompress();
    }
}
