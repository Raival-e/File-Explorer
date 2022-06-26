package com.raival.fileexplorer.activity.model;

import androidx.lifecycle.ViewModel;

import com.raival.fileexplorer.tab.BaseDataHolder;
import com.raival.fileexplorer.tab.file.FileExplorerTabDataHolder;
import com.raival.fileexplorer.tab.file.model.Task;

import java.util.ArrayList;
import java.util.List;

public class MainViewModel extends ViewModel {
    public final ArrayList<Task> tasks = new ArrayList<Task>();
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

    public FileExplorerTabDataHolder getFileExplorerDataHolder(String tag) {
        for (BaseDataHolder dataHolder : dataHolders) {
            if (dataHolder instanceof FileExplorerTabDataHolder) {
                if (dataHolder.getTag().equals(tag)) return (FileExplorerTabDataHolder) dataHolder;
            }
        }
        return null;
    }

    public List<FileExplorerTabDataHolder> getFileExplorerDataHolders() {
        List<FileExplorerTabDataHolder> list = new ArrayList<>();
        dataHolders.stream()
                .filter(baseDataHolder -> baseDataHolder instanceof FileExplorerTabDataHolder)
                .forEach(baseDataHolder -> list.add((FileExplorerTabDataHolder) dataHolders));
        return list;
    }
}
