package com.raival.quicktools.tasks;

import com.raival.quicktools.interfaces.QTask;
import com.raival.quicktools.interfaces.RegularTask;

import java.io.File;
import java.util.ArrayList;

public class ExtractTask implements QTask, RegularTask {
    ArrayList<File> filesToExtract;

    public ArrayList<File> getFilesToExtract() {
        return filesToExtract;
    }

    public ExtractTask(ArrayList<File> filesToExtract) {
        this.filesToExtract = filesToExtract;
    }

    @Override
    public String getName() {
        return "Extract";
    }

    @Override
    public String getDetails() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for(File file : filesToExtract){
            if(!first){
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
