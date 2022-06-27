package com.raival.fileexplorer.tab.file.model;

import java.io.File;

public class FileItem {
    public File file;
    public boolean isSelected = false;

    public String details = "";
    public String name = "";

    public FileItem(File file) {
        this.file = file;
    }
}
