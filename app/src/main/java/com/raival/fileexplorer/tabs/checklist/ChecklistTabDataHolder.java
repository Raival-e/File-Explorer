package com.raival.fileexplorer.tabs.checklist;

import com.raival.fileexplorer.tabs.BaseDataHolder;

import java.io.File;

public class ChecklistTabDataHolder extends BaseDataHolder {
    public final String tag;
    public File file;

    public ChecklistTabDataHolder(String tag) {
        this.tag = tag;
    }

    @Override
    public String getTag() {
        return tag;
    }
}
