package com.raival.fileexplorer.activity.model;

import androidx.lifecycle.ViewModel;

import com.raival.fileexplorer.tab.BaseDataHolder;
import com.raival.fileexplorer.tab.file.model.Task;

import java.util.ArrayList;
import java.util.List;

public class MainViewModel extends ViewModel {
    public final ArrayList<Task> tasks = new ArrayList<>();
    private final List<BaseDataHolder> dataHolders = new ArrayList<>();

    public void addDataHolder(BaseDataHolder dataHolder) {
        dataHolders.add(dataHolder);
    }

    public List<BaseDataHolder> getDataHolders() {
        return dataHolders;
    }

    public BaseDataHolder getDataHolder(String tag) {
        for (BaseDataHolder dataHolder : dataHolders) {
            if (dataHolder.getTag().equals(tag)) return dataHolder;
        }
        return null;
    }
}
