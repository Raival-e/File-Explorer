package com.raival.fileexplorer.tab.file;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.raival.fileexplorer.App;
import com.raival.fileexplorer.R;
import com.raival.fileexplorer.activity.MainActivity;
import com.raival.fileexplorer.activity.TextEditorActivity;
import com.raival.fileexplorer.common.dialog.CustomDialog;
import com.raival.fileexplorer.tab.BaseDataHolder;
import com.raival.fileexplorer.tab.BaseTabFragment;
import com.raival.fileexplorer.tab.checklist.ChecklistTabFragment;
import com.raival.fileexplorer.tab.file.adapter.FileListAdapter;
import com.raival.fileexplorer.tab.file.adapter.PathRootAdapter;
import com.raival.fileexplorer.tab.file.dialog.SearchDialog;
import com.raival.fileexplorer.tab.file.dialog.TasksDialog;
import com.raival.fileexplorer.tab.file.executor.DexRunner;
import com.raival.fileexplorer.tab.file.model.FileItem;
import com.raival.fileexplorer.tab.file.option.FileOptionHandler;
import com.raival.fileexplorer.tab.file.util.FileExtensions;
import com.raival.fileexplorer.tab.file.util.FileUtils;
import com.raival.fileexplorer.util.PrefsUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class FileExplorerTabFragment extends BaseTabFragment {
    public final static int MAX_NAME_LENGTH = 32;
    private final ArrayList<FileItem> files = new ArrayList<>();
    private RecyclerView fileList;
    private RecyclerView pathRootRv;
    private View placeHolder;
    private FileOptionHandler fileOptionHandler;
    private File previousDirectory;
    private File currentDirectory;

    private boolean requireRefresh = false;

    private IconResolver iconResolver;
    private Thread iconResolverThread;

    public FileExplorerTabFragment() {
        super();
    }

    public FileExplorerTabFragment(File directory) {
        super();
        currentDirectory = directory;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.file_explorer_tab_fragment, container, false);
        fileList = view.findViewById(R.id.rv);
        pathRootRv = view.findViewById(R.id.path_root);
        placeHolder = view.findViewById(R.id.place_holder);

        view.findViewById(R.id.home).setOnClickListener(view1 -> setCurrentDirectory(getDefaultHomeDirectory()));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        prepareBottomBarView();
        initFileList();
        loadData();
        // restore RecyclerView state
        restoreRecyclerViewState();
    }

    private void loadData() {
        setCurrentDirectory(((FileExplorerTabDataHolder) getDataHolder()).activeDirectory);
    }

    public void prepareBottomBarView() {
        getBottomBarView().clear();
        getBottomBarView().addItem("Tasks", R.drawable.ic_baseline_assignment_24, (view) ->
                new TasksDialog(this).show(getParentFragmentManager(), ""));

        getBottomBarView().addItem("Search", R.drawable.ic_round_search_24, view -> {
            SearchDialog searchFragment = new SearchDialog(this, getCurrentDirectory());
            searchFragment.show(getParentFragmentManager(), "");
            setSelectAll(false);
        });
        getBottomBarView().addItem("Create", R.drawable.ic_baseline_add_24, view -> showAddNewFileDialog());
        getBottomBarView().addItem("Sort", R.drawable.ic_baseline_sort_24, this::showSortOptionsMenu);
        getBottomBarView().addItem("Select All", R.drawable.ic_baseline_select_all_24, view -> setSelectAll(true));
        getBottomBarView().addItem("refresh", R.drawable.ic_baseline_restart_alt_24, view -> refresh());
    }

    private void showSortOptionsMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(requireActivity(), view);

        popupMenu.getMenu().add("Sort by:").setEnabled(false);

        popupMenu.getMenu().add("Name (A-Z)").setCheckable(true).setChecked(PrefsUtils.getSortingMethod() == PrefsUtils.SORT_NAME_A2Z);
        popupMenu.getMenu().add("Name (Z-A)").setCheckable(true).setChecked(PrefsUtils.getSortingMethod() == PrefsUtils.SORT_NAME_Z2A);

        popupMenu.getMenu().add("Size (Bigger)").setCheckable(true).setChecked(PrefsUtils.getSortingMethod() == PrefsUtils.SORT_SIZE_BIGGER);
        popupMenu.getMenu().add("Size (Smaller)").setCheckable(true).setChecked(PrefsUtils.getSortingMethod() == PrefsUtils.SORT_SIZE_SMALLER);

        popupMenu.getMenu().add("Date (Newer)").setCheckable(true).setChecked(PrefsUtils.getSortingMethod() == PrefsUtils.SORT_DATE_NEWER);
        popupMenu.getMenu().add("Date (Older)").setCheckable(true).setChecked(PrefsUtils.getSortingMethod() == PrefsUtils.SORT_DATE_OLDER);

        popupMenu.getMenu().add("Other options:").setEnabled(false);

        popupMenu.getMenu().add("Folders first").setCheckable(true).setChecked(PrefsUtils.listFoldersFirst());

        popupMenu.setOnMenuItemClickListener(menuItem -> {
            menuItem.setChecked(!menuItem.isChecked());
            switch (menuItem.getTitle().toString()) {
                case "Name (A-Z)": {
                    PrefsUtils.setSortingMethod(PrefsUtils.SORT_NAME_A2Z);
                    break;
                }
                case "Name (Z-A)": {
                    PrefsUtils.setSortingMethod(PrefsUtils.SORT_NAME_Z2A);
                    break;
                }
                case "Size (Bigger)": {
                    PrefsUtils.setSortingMethod(PrefsUtils.SORT_SIZE_BIGGER);
                    break;
                }
                case "Size (Smaller)": {
                    PrefsUtils.setSortingMethod(PrefsUtils.SORT_SIZE_SMALLER);
                    break;
                }
                case "Date (Older)": {
                    PrefsUtils.setSortingMethod(PrefsUtils.SORT_DATE_OLDER);
                    break;
                }
                case "Date (Newer)": {
                    PrefsUtils.setSortingMethod(PrefsUtils.SORT_DATE_NEWER);
                    break;
                }
                case "Folders first": {
                    PrefsUtils.setListFoldersFirst(menuItem.isChecked());
                    break;
                }
            }
            refresh();
            return true;
        });
        popupMenu.show();
    }

    private void showAddNewFileDialog() {
        @SuppressLint("InflateParams") TextInputLayout input = (TextInputLayout) getLayoutInflater().inflate(R.layout.input, null, false);
        input.setHint("File name");
        Objects.requireNonNull(input.getEditText()).setSingleLine();
        FileUtils.setFileValidator(input, getCurrentDirectory());

        new CustomDialog()
                .setTitle("Create new file")
                .addView(input)
                .setPositiveButton("File", view ->
                        createFile(input.getEditText().getText().toString(), false), true)
                .setNegativeButton("Folder", view ->
                        createFile(input.getEditText().getText().toString(), true), true)
                .setNeutralButton("Cancel", null, true)
                .show(getParentFragmentManager(), "");
    }

    public void createFile(String name, boolean isFolder) {
        File file = new File(getCurrentDirectory(), name);
        if (isFolder) {
            if (!file.mkdir()) {
                App.showMsg("Unable to create folder: " + file.getAbsolutePath());
            } else {
                refresh();
                focusOn(file);
            }
        } else {
            try {
                if (!file.createNewFile()) {
                    App.showMsg("Unable to create file: " + file.getAbsolutePath());
                } else {
                    refresh();
                    focusOn(file);
                }
            } catch (IOException e) {
                App.showMsg(e.toString());
                App.log(e);
            }
        }
    }

    @Override
    public void closeTab() {
        // Close the tab (if not default tab)
        if (getTag() != null && !getTag().startsWith("0_")) {
            super.closeTab();
        }
    }

    @Override
    public boolean onBackPressed() {
        //  Unselect selected files (if any)
        if (getSelectedFiles().size() > 0) {
            setSelectAll(false);
            return true;
        }
        // Go back if possible
        final File parent = getCurrentDirectory().getParentFile();
        if (parent != null && parent.exists() && parent.canRead()) {
            setCurrentDirectory(getCurrentDirectory().getParentFile());
            // restore RecyclerView state
            restoreRecyclerViewState();
            // Clear any selected files from the DataHolder (it also gets cleared
            // when a FileItem is clicked)
            ((FileExplorerTabDataHolder) getDataHolder()).selectedFiles.clear();
            return true;
        }
        // Close the tab (if not default tab)
        if (getTag() != null && !getTag().startsWith("0_")) {
            closeTab();
            return true;
        }

        return false;
    }

    @Override
    public BaseDataHolder createNewDataHolder() {
        FileExplorerTabDataHolder _dataHolder = new FileExplorerTabDataHolder(getTag());
        _dataHolder.activeDirectory = currentDirectory == null ? getDefaultHomeDirectory() : currentDirectory;
        return _dataHolder;
    }

    @Override
    public void onStop() {
        super.onStop();
        requireRefresh = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        requireRefresh = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (requireRefresh) {
            requireRefresh = false;
            refresh();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setSelectAll(boolean select) {
        if (!select) ((FileExplorerTabDataHolder) getDataHolder()).selectedFiles.clear();

        for (FileItem item : files) {
            item.isSelected = select;
            if (select) {
                ((FileExplorerTabDataHolder) getDataHolder()).selectedFiles.add(item.file);
            }
        }
        // Don't call refresh(), because it will recreate the tab and reset the selection
        Objects.requireNonNull(fileList.getAdapter()).notifyDataSetChanged();
    }

    public ArrayList<FileItem> getSelectedFiles() {
        final ArrayList<FileItem> list = new ArrayList<>();
        for (FileItem item : files) {
            if (item.isSelected) list.add(item);
        }
        return list;
    }

    /**
     * Show/Hide placeholder
     */
    public void showPlaceholder(boolean isShow) {
        placeHolder.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    /**
     * Used to update the title of attached tabView
     */
    private void updateTabTitle() {
        getTabView().setName(getName());
    }

    /**
     * This method is called once from #onViewCreated(View, Bundle)
     */
    private void initFileList() {
        fileList.setAdapter(new FileListAdapter(this));
        fileList.setHasFixedSize(true);
        initPathRoot();
    }

    private void initPathRoot() {
        pathRootRv.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false));
        pathRootRv.setAdapter(new PathRootAdapter(this));
    }

    /**
     * RecyclerView state should be saved when the fragment is destroyed and recreated.
     * #getDataHolder() isn't used here because we don't want to create a new DataHolder if the fragment is about
     * to close (note that the DataHolder gets removed just right before the fragment is closed)
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        FileExplorerTabDataHolder fileExplorerTabDataHolder = (FileExplorerTabDataHolder) getMainViewModel().getDataHolder(getTag());
        if (fileExplorerTabDataHolder != null) {
            fileExplorerTabDataHolder.recyclerViewStates.put(getCurrentDirectory(), Objects.requireNonNull(fileList.getLayoutManager()).onSaveInstanceState());
        }
    }

    /**
     * This method automatically removes the restored state from DataHolder recyclerViewStates
     * This method is called when:
     * - Create the fragment
     * - #onBackPressed()
     * - when select a directory from pathRoot RecyclerView
     */
    public void restoreRecyclerViewState() {
        Parcelable savedState = ((FileExplorerTabDataHolder) getDataHolder()).recyclerViewStates.get(getCurrentDirectory());
        if (savedState != null) {
            Objects.requireNonNull(fileList.getLayoutManager()).onRestoreInstanceState(savedState);
            ((FileExplorerTabDataHolder) getDataHolder()).recyclerViewStates.remove(getCurrentDirectory());
        }
    }

    /**
     * Refreshes both fileList and pathRoot recyclerview (used by #setCurrentDirectory(File) ONLY)
     */
    @SuppressLint("NotifyDataSetChanged")
    private void refreshFileList() {
        Objects.requireNonNull(fileList.getAdapter()).notifyDataSetChanged();
        Objects.requireNonNull(pathRootRv.getAdapter()).notifyDataSetChanged();
        pathRootRv.scrollToPosition(pathRootRv.getAdapter().getItemCount() - 1);
        fileList.scrollToPosition(0);
        if (getToolbar() != null)
            getToolbar().setSubtitle(FileUtils.getFormattedFileCount(getCurrentDirectory()));
    }

    /**
     * Used to refresh the tab
     */
    public void refresh() {
        setCurrentDirectory(getCurrentDirectory());
        restoreRecyclerViewState();
    }

    private File getDefaultHomeDirectory() {
        return Environment.getExternalStorageDirectory();
    }

    private void prepareFiles() {
        // Make sure current file is ready
        if (getCurrentDirectory() == null) {
            loadData();
            return;
        }
        // Clear previous list
        files.clear();
        // Load all files in the current File
        File[] files = getCurrentDirectory().listFiles();
        if (files != null) {
            for (Comparator<File> comparator : FileUtils.getComparators()) {
                Arrays.sort(files, comparator);
            }
            for (File file : files) {
                FileItem fileItem = new FileItem(file);
                if (((FileExplorerTabDataHolder) getDataHolder()).selectedFiles.contains(fileItem.file)) {
                    fileItem.isSelected = true;
                }
                this.files.add(fileItem);
            }
        }
        iconResolver = new IconResolver().start();
    }

    public void focusOn(File file) {
        for (int i = 0; i < files.size(); i++) {
            if (file.equals(files.get(i).file)) {
                fileList.scrollToPosition(i);
                return;
            }
        }
    }

    /**
     * @return the name associated with this tab (currently used for tabView)
     */
    public String getName() {
        return FileUtils.getShortLabel(getCurrentDirectory(), MAX_NAME_LENGTH);
    }

    public void showFileOptions(FileItem fileItem) {
        if (fileOptionHandler == null) {
            fileOptionHandler = new FileOptionHandler(this);
        }
        fileOptionHandler.showOptions(fileItem);
    }

    public void openFile(FileItem fileItem) {
        if (!handleKnownFileExtensions(fileItem)) {
            FileUtils.openFileWith(fileItem.file, false);
        }
    }

    private boolean handleKnownFileExtensions(FileItem fileItem) {
        if (FileUtils.isTextFile(fileItem.file) || FileUtils.isCodeFile(fileItem.file)) {
            Intent intent = new Intent();
            intent.setClass(requireActivity(), TextEditorActivity.class);
            intent.putExtra("file", fileItem.file.getAbsolutePath());
            requireActivity().startActivity(intent);
            return true;
        }
        if (FileUtils.getFileExtension(fileItem.file).equals("checklist")) {
            ((MainActivity) requireActivity()).addNewTab(new ChecklistTabFragment(fileItem.file)
                    , "ChecklistTabFragment_" + ((MainActivity) requireActivity()).generateRandomTag());
            return true;
        }
        if (fileItem.file.getName().toLowerCase().endsWith(".exe.dex")) {
            new DexRunner(fileItem.file, (AppCompatActivity) requireActivity()).run();
            return true;
        }
        return false;
    }

    public void showDialog(String title, String msg) {
        new CustomDialog()
                .setTitle(title)
                .setMsg(msg)
                .setPositiveButton("Ok", null, true)
                .showDialog(getParentFragmentManager(), "");
    }

    public ArrayList<FileItem> getFiles() {
        return files;
    }

    //______________| Getter and Setter |_______________\\

    public File getCurrentDirectory() {
        return currentDirectory;
    }

    /**
     * This method handles the following (in order):
     * - Updating currentDirectory and previousDirectory fields
     * - Updating recyclerViewStates in DataHolder
     * - Sorting files based on the preferences
     * - Updating tabView title
     * - Refreshing adapters (fileList & pathRoot)
     * - Updating activeDirectory in DataHolder
     *
     * @param dir the directory to open
     */
    public void setCurrentDirectory(File dir) {
        previousDirectory = currentDirectory;
        currentDirectory = dir;
        // Save only when previousDirectory is set (so that it can restore the state before onDestroy())
        if (previousDirectory != null) ((FileExplorerTabDataHolder) getDataHolder())
                .recyclerViewStates.put(previousDirectory, Objects.requireNonNull(fileList.getLayoutManager()).onSaveInstanceState());
        prepareFiles();
        updateTabTitle();
        refreshFileList();
        ((FileExplorerTabDataHolder) getDataHolder()).activeDirectory = getCurrentDirectory();
    }

    public File getPreviousDirectory() {
        return previousDirectory;
    }

    private class IconResolver {
        public boolean isWorking = false;

        public IconResolver start() {
            isWorking = true;
            AtomicInteger i = new AtomicInteger();
            iconResolverThread = new Thread(() -> {
                for (FileItem fileItem : files) {
                    if (FileUtils.getFileExtension(fileItem.file).equalsIgnoreCase(FileExtensions.apkType)) {
                        Drawable drawable = FileUtils.getApkIcon(fileItem.file);
                        App.appHandler.post(() -> fileItem.img.setValue(drawable));
                    }
                    i.incrementAndGet();
                }
                isWorking = false;
            });
            iconResolverThread.start();
            return this;
        }
    }
}
