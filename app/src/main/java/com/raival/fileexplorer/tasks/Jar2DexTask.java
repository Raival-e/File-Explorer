package com.raival.fileexplorer.tasks;

import com.raival.fileexplorer.interfaces.QTask;
import com.raival.fileexplorer.interfaces.RegularTask;

import java.io.File;
import java.util.ArrayList;

public class Jar2DexTask implements QTask, RegularTask {
    private final File fileToConvert;

    public Jar2DexTask(File file) {
        this.fileToConvert = file;
    }

    public File getFileToConvert() {
        return fileToConvert;
    }

    @Override
    public String getName() {
        return "Jar2Dex";
    }

    @Override
    public String getDetails() {
        return fileToConvert.getAbsolutePath();
    }

    @Override
    public ArrayList<File> getFilesList() {
        return new ArrayList<File>() {{
            add(fileToConvert);
        }};
    }
}
