package com.raival.fileexplorer.tabs.checklist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dvdb.materialchecklist.MaterialChecklist;
import com.raival.fileexplorer.App;
import com.raival.fileexplorer.R;
import com.raival.fileexplorer.tabs.BaseTabFragment;
import com.raival.fileexplorer.tabs.file.FileExplorerTabFragment;
import com.raival.fileexplorer.utils.FileUtils;

import java.io.File;
import java.io.IOException;

public class ChecklistTabFragment extends BaseTabFragment {
    private MaterialChecklist materialChecklist;
    private File file;

    public ChecklistTabFragment() {
        super();
    }

    public ChecklistTabFragment(File file) {
        super();
        this.file = file;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.checklist_tab_fragment, container, false);
        materialChecklist = view.findViewById(R.id.checklist);

        try {
            materialChecklist.setItems(FileUtils.readFile(((ChecklistTabDataHolder) getDataHolder()).file));
        } catch (Exception e) {
            e.printStackTrace();
            App.log(e);
            App.showMsg(e.toString());
            closeTab();
        }
        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        saveFile();
    }

    private void saveFile() {
        if (getDataHolder() == null) return;
        try {
            FileUtils.writeFile(((ChecklistTabDataHolder) getDataHolder()).file, materialChecklist.getItems(true, true));
        } catch (IOException e) {
            e.printStackTrace();
            App.log(e);
            App.showMsg("Unable to save the list, check logs for more details");
        }
    }

    public void closeTab() {
        // Close the tab
        saveFile();
        super.closeTab();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        prepareBottomBarView();
        updateTabTitle();
    }

    private void updateTabTitle() {
        getTabView().setName(FileUtils.getShortLabel(((ChecklistTabDataHolder) getDataHolder()).file, FileExplorerTabFragment.MAX_NAME_LENGTH));
    }

    private void prepareBottomBarView() {
        getBottomBarView().clear();

        getBottomBarView().addItem("Clear", R.drawable.ic_baseline_delete_sweep_24, view -> {
            materialChecklist.removeAllCheckedItems();
            saveFile();
        });
        getBottomBarView().addItem("Save", R.drawable.ic_baseline_save_24, view -> {
            saveFile();
            App.showMsg("Saved!");
        });
    }

    @Override
    public boolean onBackPressed() {
        closeTab();
        return true;
    }

    @Override
    public ChecklistTabDataHolder createNewDataHolder() {
        ChecklistTabDataHolder checklistTabDataHolder = new ChecklistTabDataHolder(getTag());
        checklistTabDataHolder.file = file;
        return checklistTabDataHolder;
    }
}
