package com.raival.fileexplorer.tab.file.model;

import android.graphics.drawable.Drawable;

import androidx.lifecycle.MutableLiveData;

import java.io.File;

public class FileItem {
    public File file;
    public boolean isSelected = false;

    public String details = "";
    public String name = "";
    public MutableLiveData<Drawable> img = new MutableLiveData<>();

    public FileItem(File file) {
        this.file = file;
    }
}
