package com.raival.quicktools.tasks;

import com.raival.quicktools.common.BackgroundTask;
import com.raival.quicktools.interfaces.QTask;

import java.io.File;
import java.util.ArrayList;

public class CopyTask  implements QTask {
    ArrayList<File> filesToCopy;

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
        if(filesToCopy.size() == 1){
            return "\"" + filesToCopy.get(0).getName() + "\"" + " from: " + filesToCopy.get(0).getParentFile().getAbsolutePath();
        }
        return filesToCopy.size() + " files from: " + filesToCopy.get(0).getParentFile().getAbsolutePath();
    }
}
