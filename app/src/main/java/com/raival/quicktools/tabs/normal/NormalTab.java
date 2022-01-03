package com.raival.quicktools.tabs.normal;

import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;

import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;
import com.raival.quicktools.App;
import com.raival.quicktools.tabs.normal.fragment.NormalTabFragment;
import com.raival.quicktools.interfaces.QTab;
import com.raival.quicktools.tabs.normal.models.FileItem;
import com.raival.quicktools.utils.TimeUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class NormalTab implements QTab {
    public final static String MAX_NAME_LENGTH = "maximum name length";

    TabLayout.Tab tab;

    File currentPath;
    File previousPath;

    NormalTabFragment fragment;
    ArrayList<FileItem> activeFilesList;
    Comparator<File>[] comparators;

    Map<String, Parcelable> pathsStets = new HashMap<>();

    public NormalTab(File path){
        currentPath = path;
    }

    @SafeVarargs
    public final void setComparators(Comparator<File>... comparators) {
        this.comparators = comparators;
    }

    public void setCurrentPath(File currentPath) {
        //save state before opening a new folder
        addPathState(this.currentPath);

        this.currentPath = currentPath;
        activeFilesList = null;
    }

    private void addPathState(File currentPath) {
        if(fragment.getRecyclerViewInstance() != null){
            pathsStets.put(currentPath.getAbsolutePath(), fragment.getRecyclerViewInstance());
        }
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

        previousPath = currentPath;
        setCurrentPath(currentPath.getParentFile());

        fragment.updateFilesList();

        if(pathsStets.containsKey(currentPath.getAbsolutePath())){
            fragment.setRecyclerViewInstance(pathsStets.get(currentPath.getAbsolutePath()));
            pathsStets.remove(currentPath.getAbsolutePath());
        }
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

    public boolean shouldHighlightFile(File file){
        if(previousPath == null) return false;
        return file.getAbsolutePath().equals(previousPath.getAbsolutePath());
    }
}
