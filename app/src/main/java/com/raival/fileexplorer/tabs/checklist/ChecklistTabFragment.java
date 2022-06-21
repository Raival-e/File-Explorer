package com.raival.fileexplorer.tabs.checklist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.dvdb.materialchecklist.MaterialChecklist;
import com.raival.fileexplorer.App;
import com.raival.fileexplorer.R;
import com.raival.fileexplorer.activities.MainActivity;
import com.raival.fileexplorer.activities.model.MainViewModel;
import com.raival.fileexplorer.common.view.BottomBarView;
import com.raival.fileexplorer.common.view.TabView;
import com.raival.fileexplorer.tabs.BaseDataHolder;
import com.raival.fileexplorer.tabs.BaseTabFragment;
import com.raival.fileexplorer.tabs.file.FileExplorerTabFragment;
import com.raival.fileexplorer.utils.FileUtil;

import java.io.File;
import java.io.IOException;

public class ChecklistTabFragment extends BaseTabFragment {
    private MainViewModel mainViewModel;
    private MaterialChecklist materialChecklist;
    private BottomBarView bottomBarView;
    private TabView.Tab tabView;
    private File file;

    public ChecklistTabFragment() {
        super();
    }

    public ChecklistTabFragment(File file) {
        super();
        this.file = file;
    }

    private void createDataHolderIfNecessary() {
        if (getDataHolder() == null) {
            ChecklistTabDataHolder checklistTabDataHolder = new ChecklistTabDataHolder(getTag());
            checklistTabDataHolder.file = file;
            getMainViewModel().addDataHolder(checklistTabDataHolder);
        }
    }

    private ChecklistTabDataHolder getDataHolder() {
        for (BaseDataHolder baseDataHolder : getMainViewModel().getDataHolders()) {
            if (baseDataHolder.getTag().equals(getTag()))
                return (ChecklistTabDataHolder) baseDataHolder;
        }
        return null;
    }

    private MainViewModel getMainViewModel() {
        if (mainViewModel == null) {
            mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        }
        return mainViewModel;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.checklist_tab_fragment, container, false);
        materialChecklist = view.findViewById(R.id.checklist);

        createDataHolderIfNecessary();

        try {
            materialChecklist.setItems(FileUtil.readFile(getDataHolder().file));
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
            FileUtil.writeFile(getDataHolder().file, materialChecklist.getItems(true, true));
        } catch (IOException e) {
            e.printStackTrace();
            App.log(e);
            App.showMsg("Unable to save the list, check logs for more details");
        }
    }

    private void closeTab() {
        // Close the tab
        saveFile();
        getMainViewModel().getDataHolders().removeIf(dataHolder1 -> dataHolder1.getTag().equals(getTag()));
        ((MainActivity) requireActivity()).closeTab(getTag());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (bottomBarView == null)
            bottomBarView = ((MainActivity) requireActivity()).getBottomBarView();
        prepareBottomBarView();
        updateTabTitle();
    }

    private void updateTabTitle() {
        if (tabView == null) {
            if (!findAssociatedTabView()) {
                createNewTabView();
            }
        }
        tabView.setName(FileUtil.getShortLabel(getDataHolder().file, FileExplorerTabFragment.MAX_NAME_LENGTH));
    }

    private void createNewTabView() {
        tabView = ((MainActivity) requireActivity()).getTabView().addNewTab(getTag());
    }

    private boolean findAssociatedTabView() {
        tabView = ((MainActivity) requireActivity()).getTabView().getTabByTag(getTag());
        return (tabView != null);
    }

    private void prepareBottomBarView() {
        bottomBarView.clear();

        bottomBarView.addItem("Clear", R.drawable.ic_baseline_delete_sweep_24, view -> {
            materialChecklist.removeAllCheckedItems();
            saveFile();
        });
        bottomBarView.addItem("Save", R.drawable.ic_baseline_save_24, view -> {
            saveFile();
            App.showMsg("Saved!");
        });
    }

    @Override
    public boolean onBackPressed() {
        closeTab();
        return true;
    }
}
