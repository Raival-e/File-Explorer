package com.raival.quicktools.interfaces;

import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;
import com.raival.quicktools.tabs.normal.models.FileItem;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;

public interface QTab {
    void setTab(TabLayout.Tab tab);
    String getName();//used only when the tab get attached to the viewpager
    ArrayList<FileItem> getFilesList();
    Fragment getFragment();
    boolean onBackPressed();
}
