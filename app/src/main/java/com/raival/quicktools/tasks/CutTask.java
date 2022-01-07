package com.raival.quicktools.tasks;

import com.raival.quicktools.interfaces.QTask;

import java.io.File;
import java.util.ArrayList;

public class CutTask implements QTask {
    ArrayList<File> filesToCut;

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
        if(filesToCut.size() == 1){
            return "\"" + filesToCut.get(0).getName() + "\"" + " from: " + filesToCut.get(0).getParentFile().getAbsolutePath();
        }
        return filesToCut.size() + " files from: " + filesToCut.get(0).getParentFile().getAbsolutePath();
    }
}
