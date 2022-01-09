package com.raival.quicktools.tasks;

import com.raival.quicktools.interfaces.QTask;
import com.raival.quicktools.interfaces.RegularTask;

import java.io.File;
import java.util.ArrayList;

public class CopyTask  implements QTask, RegularTask {
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
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for(File file : filesToCopy){
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
        return getFilesToCopy();
    }
}
