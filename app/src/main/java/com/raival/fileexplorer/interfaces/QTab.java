package com.raival.fileexplorer.interfaces;

import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;
import com.raival.fileexplorer.tabs.normal.models.FileItem;

import java.io.File;
import java.util.ArrayList;

public interface QTab {
    void setTab(TabLayout.Tab tab);

    String getName();

    ArrayList<FileItem> getFilesList();

    Fragment getFragment();

    //BottomBar options
    boolean onBackPressed();

    void selectAll();

    void refresh();

    boolean canCreateFile();

    void createFile(String name, boolean isFolder);

    ArrayList<File> getTreeViewList();

    void onTreeViewPathSelected(int position);

    void handleTask(QTask task);

    void handleSearch();
}
