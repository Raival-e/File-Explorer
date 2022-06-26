package com.raival.fileexplorer.tab.checklist;

import com.raival.fileexplorer.tab.BaseDataHolder;

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
