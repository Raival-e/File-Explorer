package com.raival.fileexplorer.tabs.file.model;

import java.io.File;

public class FileItem {
    public File file;
    public boolean isSelected = false;

    public FileItem(File file){
        this.file = file;
    }
}
