package com.raival.quicktools.tabs.normal;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;

import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputLayout;
import com.raival.quicktools.App;
import com.raival.quicktools.R;
import com.raival.quicktools.common.BackgroundTask;
import com.raival.quicktools.common.QDialogFragment;
import com.raival.quicktools.interfaces.QTask;
import com.raival.quicktools.tabs.normal.fragment.NormalTabFragment;
import com.raival.quicktools.interfaces.QTab;
import com.raival.quicktools.tabs.normal.models.FileItem;
import com.raival.quicktools.tasks.CompressTask;
import com.raival.quicktools.tasks.CopyTask;
import com.raival.quicktools.tasks.CutTask;
import com.raival.quicktools.tasks.ExtractTask;
import com.raival.quicktools.utils.FileUtil;
import com.raival.quicktools.utils.PrefsUtil;
import com.raival.quicktools.utils.TimeUtil;
import com.raival.quicktools.utils.ZipUtil;

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
    public void onTreeViewPathSelected(int position) {
        if(getTreeViewList().size() > position+1){
            previousPath = getTreeViewList().get(position+1);
        }

        setCurrentPath(getTreeViewList().get(position));
        fragment.updateFilesList();

        restoreRecyclerViewState();
    }

    @Override
    public void handleTask(QTask task) {
        if(task instanceof CopyTask){
            handleCopyTask((CopyTask)task);
        } else if(task instanceof CutTask) {
            handleCutTask((CutTask) task);
        } else if(task instanceof CompressTask) {
            handleCompressTask((CompressTask) task);
        } else if(task instanceof ExtractTask){
            handleExtractTask((ExtractTask)task);
        } else {
            App.showMsg("Cannot execute this task here");
        }
    }

    private void handleExtractTask(ExtractTask task) {
        BackgroundTask backgroundTask = new BackgroundTask();
        backgroundTask.setTasks(()-> backgroundTask.showProgressDialog("Extracting files...", getFragment().requireActivity()), ()->{
            try {
                ZipUtil.extract(task.getFilesToExtract(), currentPath);
            } catch (Exception exception) {
                exception.printStackTrace();
                App.log(exception);
                new Handler(Looper.getMainLooper()).post(()-> App.showMsg("Cannot extract files"));
            }
        }, ()-> {
            refresh();
            backgroundTask.dismiss();
            App.showMsg("Files have been extracted successfully");
        });
        backgroundTask.run();
    }

    @SuppressLint("SetTextI18n")
    private void handleCompressTask(CompressTask task) {
        TextInputLayout input = (TextInputLayout) getFragment().requireActivity().getLayoutInflater().inflate(R.layout.input, null, false);
        input.setHint("Archive name");
        input.getEditText().setText(".zip");
        FileUtil.setFileInvalidator(input, currentPath);

        new QDialogFragment()
                .setTitle("Compress")
                .addView(input)
                .setPositiveButton("Save", view -> {
                    if(input.getError() == null){
                        executeCompressTask(task.getFilesToCompress(), new File(currentPath, input.getEditText().getText().toString()));
                    } else {
                        App.showMsg("Compress canceled");
                    }
                }, true)
                .showDialog(getFragment().getChildFragmentManager(), "");
    }

    private void executeCompressTask(ArrayList<File> filesToCompress, File zip) {
        BackgroundTask backgroundTask = new BackgroundTask();
        backgroundTask.setTasks(()-> backgroundTask.showProgressDialog("Compressing files...", getFragment().requireActivity()), ()->{
            try {
                ZipUtil.archive(filesToCompress, zip);
            } catch (Exception exception) {
                exception.printStackTrace();
                App.log(exception);
                new Handler(Looper.getMainLooper()).post(()-> App.showMsg("Cannot compress files"));
            }
        }, ()-> {
            refresh();
            backgroundTask.dismiss();
            App.showMsg("Files have been compressed successfully");
        });
        backgroundTask.run();
    }


    private void handleCutTask(CutTask task) {
        BackgroundTask backgroundTask = new BackgroundTask();
        backgroundTask.setTasks(()-> backgroundTask.showProgressDialog("Moving files...", getFragment().requireActivity()), ()->{
            try {
                FileUtil.MoveFiles(task.getFilesToCut(), getCurrentPath());
            } catch (IOException e) {
                e.printStackTrace();
                App.log(e);
                new Handler(Looper.getMainLooper()).post(()->App.showMsg("Cannot move files"));
            }

        } , ()->{
            refresh();
            backgroundTask.dismiss();
            App.showMsg("Files have been moved successfully");
        });
        backgroundTask.run();
    }

    private void handleCopyTask(CopyTask task) {
        BackgroundTask backgroundTask = new BackgroundTask();
        backgroundTask.setTasks(()-> backgroundTask.showProgressDialog("Coping files...", fragment.requireActivity()), ()->{
            try {
                FileUtil.copyFiles(task.getFilesToCopy(), getCurrentPath());
            } catch (IOException e) {
                e.printStackTrace();
                App.log(e);
                new Handler(Looper.getMainLooper()).post(()->App.showMsg("Cannot copy files"));
            }

        } , ()->{
            refresh();
            backgroundTask.dismiss();
            App.showMsg("Files has been copied successfully");
        });
        backgroundTask.run();
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

        restoreRecyclerViewState();
        return true;
    }

    private void restoreRecyclerViewState(){
        if(pathsStets.containsKey(currentPath.getAbsolutePath())){
            fragment.setRecyclerViewInstance(pathsStets.get(currentPath.getAbsolutePath()));
            pathsStets.remove(currentPath.getAbsolutePath());
        }
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
