package com.raival.quicktools.tabs.normal;

import android.net.Uri;
import android.os.Environment;

import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;
import com.raival.quicktools.tabs.normal.fragment.NormalTabFragment;
import com.raival.quicktools.interfaces.QTab;
import com.raival.quicktools.tabs.normal.models.FileItem;
import com.raival.quicktools.utils.TimeUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

public class NormalTab implements QTab {
    public final static String MAX_NAME_LENGTH = "maximum name length";

    TabLayout.Tab tab;
    File currentPath;
    NormalTabFragment fragment;
    ArrayList<FileItem> activeFilesList;
    Comparator<File>[] comparators;

    public NormalTab(File path){
        currentPath = path;
    }

    @SafeVarargs
    public final void setComparators(Comparator<File>... comparators) {
        this.comparators = comparators;
    }

    public void setCurrentPath(File currentPath) {
        this.currentPath = currentPath;
        activeFilesList = null;
    }

    @Override
    public void setTab(TabLayout.Tab tab) {
        this.tab = tab;
    }

    @Override
    public String getName() {
        String name = Uri.parse(currentPath.getAbsolutePath()).getLastPathSegment();
        if(name.equals("0")){
            name = "Internal Storage";
        }
        if(name.length() > MAX_NAME_LENGTH.length()){
            name = name.substring(0, MAX_NAME_LENGTH.length() - 3) + "...";
        }
        return name;
    }

    @Override
    public ArrayList<FileItem> getFilesList() {
        if(activeFilesList == null){
            activeFilesList = getSortedFilesList(comparators);
        }
        return activeFilesList;
    }

    @SafeVarargs
    public final ArrayList<FileItem> getSortedFilesList(Comparator<File>... comparators){
        ArrayList<FileItem> list = new ArrayList<>();
        File[] files = currentPath.listFiles();
        if(files != null){
            for (Comparator<File> comparator : comparators){
                Arrays.sort(files, comparator);
            }
            for(File file : files){
                list.add(new FileItem(file, getFileDetails(file)));
            }
        }
        return list;
    }

    @Override
    public Fragment getFragment() {
        if(fragment == null){
            fragment = new NormalTabFragment(this);
        }
        return fragment;
    }

    @Override
    public boolean onBackPressed() {
        return canGoBack();
    }

    private boolean canGoBack() {
        if(currentPath.getAbsolutePath().equals(Environment.getExternalStorageDirectory().getAbsolutePath())){
            return false;
        }
        setCurrentPath(currentPath.getParentFile());
        fragment.updateFilesList();
        return true;
    }

    private String getFileDetails(File file) {
        return TimeUtil.getLastModifiedDate(file, TimeUtil.REGULAR_DATE_FORMAT);
    }

    public void updateTabName() {
        if(tab != null){
            tab.setText(getName());
        }
    }
}
