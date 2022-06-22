package com.raival.fileexplorer.tabs;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.raival.fileexplorer.activities.MainActivity;
import com.raival.fileexplorer.activities.model.MainViewModel;
import com.raival.fileexplorer.common.view.BottomBarView;
import com.raival.fileexplorer.common.view.TabView;

/**
 * Each TabFragment must handle the creation of its DataHolder and the related TabView using
 * the provided APIs or custom ones.
 */
public abstract class BaseTabFragment extends Fragment {
    private MainViewModel mainViewModel;
    private BottomBarView bottomBarView;
    private MaterialToolbar toolbar;
    private TabView.Tab tabView;
    private BaseDataHolder dataHolder;

    public abstract boolean onBackPressed();
    public abstract BaseDataHolder createNewDataHolder();

    public BaseTabFragment() {
        super();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public BaseDataHolder getDataHolder(){
        if (dataHolder == null && (dataHolder = getMainViewModel().getDataHolder(getTag())) == null) {
            dataHolder = createNewDataHolder();
            getMainViewModel().addDataHolder(dataHolder);
        }
        return dataHolder;
    }

    public MainViewModel getMainViewModel(){
        if (mainViewModel == null) {
            mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        }
        return mainViewModel;
    }
    public BottomBarView getBottomBarView(){
        if (bottomBarView == null)
            bottomBarView = ((MainActivity) requireActivity()).getBottomBarView();
        return bottomBarView;
    }
    public MaterialToolbar getToolbar(){
        if (toolbar == null) toolbar = ((MainActivity) requireActivity()).getToolbar();
        return toolbar;
    }
    public TabView.Tab getTabView(){
        if(tabView == null && (tabView = ((MainActivity) requireActivity()).getTabView().getTabByTag(getTag())) == null){
            tabView = ((MainActivity) requireActivity()).getTabView().addNewTab(getTag());
        }
        return tabView;
    }

    public void closeTab(){
        getMainViewModel().getDataHolders().removeIf(dataHolder1 -> dataHolder1.getTag().equals(getTag()));
        ((MainActivity) requireActivity()).closeTab(getTag());
    }
}
