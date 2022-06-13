package com.raival.fileexplorer.tabs.normal.models;

import android.graphics.drawable.Drawable;

import java.io.File;

public class FileItem {
    private File file;
    private String details;
    private Drawable icon;
    private boolean isSelected;

    public FileItem(File file, String details) {
        this.file = file;
        this.details = details;
    }

    public FileItem(File file, String details, Drawable icon) {
        this.file = file;
        this.details = details;
        this.icon = icon;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getName() {
        return file.getName();
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public void changeSelection() {
        setSelected(!isSelected);
    }
}
