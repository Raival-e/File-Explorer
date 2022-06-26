package com.raival.fileexplorer.tab.file;

import android.os.Parcelable;

import com.raival.fileexplorer.tab.BaseDataHolder;
import com.raival.fileexplorer.tab.file.model.FileItem;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FileExplorerTabDataHolder extends BaseDataHolder {
    private final String tag;
    public File activeDirectory;
    public Map<File, Parcelable> recyclerViewStates = new HashMap<>();
    public ArrayList<FileItem> searchList = new ArrayList<>();
    public ArrayList<File> selectedFiles = new ArrayList<>();

    public FileExplorerTabDataHolder(String tag) {
        this.tag = tag;
    }

    @Override
    public String getTag() {
        return tag;
    }
}
