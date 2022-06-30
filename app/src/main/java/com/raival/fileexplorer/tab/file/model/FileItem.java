package com.raival.fileexplorer.tab.file.model;

import android.graphics.drawable.Drawable;

import androidx.lifecycle.MutableLiveData;

import com.raival.fileexplorer.tab.file.util.FileExtensions;
import com.raival.fileexplorer.tab.file.util.FileUtils;

import java.io.File;

public class FileItem {
    public File file;
    public boolean isSelected = false;

    public String details = "";
    public String name = "";
    public MutableLiveData<Drawable> img;

    public FileItem(File file) {
        this.file = file;
        if (FileUtils.getFileExtension(file).equalsIgnoreCase(FileExtensions.apkType)) {
            img = new MutableLiveData<>();
        }
    }
}
