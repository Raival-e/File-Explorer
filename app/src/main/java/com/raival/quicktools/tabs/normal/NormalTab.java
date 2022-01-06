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
import com.raival.quicktools.utils.FileUtil;
import com.raival.quicktools.utils.PrefsUtil;
import com.raival.quicktools.utils.TimeUtil;

import java.io.File;
import java.io.IOException;
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
    ArrayList<Comparator<File>> comparators = new ArrayList<>();

    Map<String, Parcelable> pathsStets = new HashMap<>();

    public NormalTab(File path){
        currentPath = path;
    }

    private void assignComparators() {
        comparators.clear();
        switch (PrefsUtil.getSortingMethod()){
            case PrefsUtil.SORT_NAME_A2Z:{
                addComparators(FileUtil.sortNameAsc());
                break;
            }
            case PrefsUtil.SORT_NAME_Z2A:{
                addComparators(FileUtil.sortNameDesc());
                break;
            }
            case PrefsUtil.SORT_SIZE_SMALLER:{
                addComparators(FileUtil.sortSizeAsc());
                break;
            }
            case PrefsUtil.SORT_SIZE_BIGGER:{
                addComparators(FileUtil.sortSizeDesc());
                break;
            }
            case PrefsUtil.SORT_DATE_NEWER:{
                addComparators(FileUtil.sortDateDesc());
                break;
            }
            case PrefsUtil.SORT_DATE_OLDER:{
                addComparators(FileUtil.sortDateAsc());
                break;
            }
        }
        if(PrefsUtil.listFoldersFirst()){
            addComparators(FileUtil.sortFoldersFirst());
        } else {
            addComparators(FileUtil.sortFilesFirst());
        }
    }

    public File getCurrentPath(){
        return currentPath;
    }

    public final void addComparators(Comparator<File> comparator) {
        this.comparators.add(comparator);
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
        if(FileUtil.isExternalStorageFolder(currentPath)){
            name = "Internal Storage";
        }
        if(name.length() > MAX_NAME_LENGTH.length()){
            name = name.substring(0, MAX_NAME_LENGTH.length() - 3) + "...";
        }
        return name;
    }

    @Override
    public ArrayList<FileItem> getFilesList() {
        assignComparators();
        if(activeFilesList == null){
            activeFilesList = getSortedFilesList(comparators);
        }
        return activeFilesList;
    }

    public final ArrayList<FileItem> getSortedFilesList(ArrayList<Comparator<File>>  comparators){
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

    @Override
    public void selectAll() {
        for(FileItem item : activeFilesList){
            item.setSelected(true);
        }
        fragment.getRecyclerView().getAdapter().notifyDataSetChanged();
    }

    @Override
    public boolean canCreateFile() {
        return true;
    }

    @Override
    public void refresh() {
        setCurrentPath(currentPath);
        fragment.updateFilesList();
    }

    @Override
    public void createFile(String name, boolean isFolder) {
        File file = new File(currentPath, name);
        if(isFolder){
            if(!file.mkdir()){
                App.showMsg("Unable to create folder " + file.getAbsolutePath());
            } else {
                refresh();
                scrollTo(file);
            }
        } else {
            try {
                if(!file.createNewFile()){
                    App.showMsg("Unable to create file " + file.getAbsolutePath());
                } else {
                    refresh();
                    scrollTo(file);
                }
            } catch (IOException e){
                App.showMsg(e.toString());
                App.log(e);
            }
        }
    }

    @Override
    public ArrayList<File> getTreeViewList() {
        ArrayList<File> list = new ArrayList<>();
        File file = currentPath;
        while (!file.getAbsolutePath().equals(Environment.getExternalStorageDirectory().getParentFile().getAbsolutePath())){
            list.add(file);
            file = file.getParentFile();
        }
        Collections.reverse(list);
        return list;
    }

    @Override
    public void onTreeViewPathSelected(File path) {
        setCurrentPath(path);
        fragment.updateFilesList();
    }

    private void scrollTo(File file) {
        for(int i = 0; i < activeFilesList.size(); i++){
            if(activeFilesList.get(i).getFile().getAbsolutePath().equals(file.getAbsolutePath())){
                if(fragment.getRecyclerView().getAdapter().getItemCount() > i){
                    fragment.getRecyclerView().scrollToPosition(i);
                    return;
                }
            }
        }
    }

    public boolean hasSelectedFiles(){
        for(FileItem item : activeFilesList){
            if(item.isSelected())
                return true;
        }
        return false;
    }

    private boolean canGoBack() {
        boolean hasFileSelected = false;
        for(FileItem item : activeFilesList){
            if(!hasFileSelected && item.isSelected())
                hasFileSelected = true;
            item.setSelected(false);
        }
        if(hasFileSelected) {
            fragment.getRecyclerView().getAdapter().notifyDataSetChanged();
            return true;
        }

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
        final StringBuilder sb = new StringBuilder();
        sb.append(TimeUtil.getLastModifiedDate(file, TimeUtil.REGULAR_DATE_FORMAT));
        sb.append("  |  ");
        if(file.isFile()){
            sb.append(FileUtil.getFormattedFileSize(file));
        } else {
            sb.append(FileUtil.getFormattedFileCount(file));
        }
        return sb.toString();
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

    public ArrayList<File> getSelectedFiles() {
        ArrayList<File> list = new ArrayList<>();
        for(FileItem fileItem : activeFilesList){
            if(fileItem.isSelected())
                list.add(fileItem.getFile());
        }
        return list;
    }
}
