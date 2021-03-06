package com.raival.fileexplorer.tab.file;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.raival.fileexplorer.App;
import com.raival.fileexplorer.R;
import com.raival.fileexplorer.activity.MainActivity;
import com.raival.fileexplorer.common.dialog.CustomDialog;
import com.raival.fileexplorer.tab.BaseDataHolder;
import com.raival.fileexplorer.tab.BaseTabFragment;
import com.raival.fileexplorer.tab.file.adapter.FileListAdapter;
import com.raival.fileexplorer.tab.file.adapter.PathHistoryAdapter;
import com.raival.fileexplorer.tab.file.dialog.SearchDialog;
import com.raival.fileexplorer.tab.file.dialog.TasksDialog;
import com.raival.fileexplorer.tab.file.model.FileItem;
import com.raival.fileexplorer.tab.file.option.FileOptionHandler;
import com.raival.fileexplorer.tab.file.util.FileOpener;
import com.raival.fileexplorer.tab.file.util.FileUtils;
import com.raival.fileexplorer.util.Log;
import com.raival.fileexplorer.util.PrefsUtils;
import com.raival.fileexplorer.util.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

public class FileExplorerTabFragment extends BaseTabFragment {
    public final static int MAX_NAME_LENGTH = 32;
    private static final String TAG = "FileExplorerTabFragment";

    private final ArrayList<FileItem> files = new ArrayList<>();
    private ArrayList<File> pathHistory = new ArrayList<>();
    private RecyclerView fileList;

    private RecyclerView pathHistoryRv;
    private View placeHolder;
    private FileOptionHandler fileOptionHandler;
    private File previousDirectory;
    private File currentDirectory;

    private boolean requireRefresh = false;

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
        pathHistoryRv = view.findViewById(R.id.path_history);
        placeHolder = view.findViewById(R.id.place_holder);

        final View homeButton = view.findViewById(R.id.home);
        homeButton.setOnClickListener(view1 -> setCurrentDirectory(getDefaultHomeDirectory()));
        homeButton.setOnLongClickListener((v -> showSetPathDialog()));
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
        popupMenu.getMenu().add("Name (A-Z)").setCheckable(true).setChecked(PrefsUtils.FileExplorerTab.getSortingMethod() == PrefsUtils.SORT_NAME_A2Z);
        popupMenu.getMenu().add("Name (Z-A)").setCheckable(true).setChecked(PrefsUtils.FileExplorerTab.getSortingMethod() == PrefsUtils.SORT_NAME_Z2A);
        popupMenu.getMenu().add("Size (Bigger)").setCheckable(true).setChecked(PrefsUtils.FileExplorerTab.getSortingMethod() == PrefsUtils.SORT_SIZE_BIGGER);
        popupMenu.getMenu().add("Size (Smaller)").setCheckable(true).setChecked(PrefsUtils.FileExplorerTab.getSortingMethod() == PrefsUtils.SORT_SIZE_SMALLER);
        popupMenu.getMenu().add("Date (Newer)").setCheckable(true).setChecked(PrefsUtils.FileExplorerTab.getSortingMethod() == PrefsUtils.SORT_DATE_NEWER);
        popupMenu.getMenu().add("Date (Older)").setCheckable(true).setChecked(PrefsUtils.FileExplorerTab.getSortingMethod() == PrefsUtils.SORT_DATE_OLDER);
        popupMenu.getMenu().add("Other options:").setEnabled(false);
        popupMenu.getMenu().add("Folders first").setCheckable(true).setChecked(PrefsUtils.FileExplorerTab.listFoldersFirst());
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            menuItem.setChecked(!menuItem.isChecked());
            switch (menuItem.getTitle().toString()) {
                case "Name (A-Z)": {
                    PrefsUtils.FileExplorerTab.setSortingMethod(PrefsUtils.SORT_NAME_A2Z);
                    break;
                }
                case "Name (Z-A)": {
                    PrefsUtils.FileExplorerTab.setSortingMethod(PrefsUtils.SORT_NAME_Z2A);
                    break;
                }
                case "Size (Bigger)": {
                    PrefsUtils.FileExplorerTab.setSortingMethod(PrefsUtils.SORT_SIZE_BIGGER);
                    break;
                }
                case "Size (Smaller)": {
                    PrefsUtils.FileExplorerTab.setSortingMethod(PrefsUtils.SORT_SIZE_SMALLER);
                    break;
                }
                case "Date (Older)": {
                    PrefsUtils.FileExplorerTab.setSortingMethod(PrefsUtils.SORT_DATE_OLDER);
                    break;
                }
                case "Date (Newer)": {
                    PrefsUtils.FileExplorerTab.setSortingMethod(PrefsUtils.SORT_DATE_NEWER);
                    break;
                }
                case "Folders first": {
                    PrefsUtils.FileExplorerTab.setListFoldersFirst(menuItem.isChecked());
                    break;
                }
            }
            refresh();
            return true;
        });
        popupMenu.show();
    }

    @SuppressLint("SetTextI18n")
    private boolean showSetPathDialog() {
        @SuppressLint("InflateParams") TextInputLayout input = (TextInputLayout) getLayoutInflater().inflate(R.layout.input, null, false);
        input.setHint("File path");
        Objects.requireNonNull(input.getEditText()).setSingleLine();

        CustomDialog customDialog = new CustomDialog();

        MaterialTextView textView = new MaterialTextView(requireContext());
        textView.setPadding(0, (int) Utils.pxToDp(8), 0, 0);
        textView.setAlpha(0.7f);
        textView.setText("Quick Links:");

        ChipGroup layout = new ChipGroup(requireContext());

        Chip internal = new Chip(requireContext());
        internal.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        internal.setText("Internal Data Files");
        internal.setOnClickListener(v -> {
            setCurrentDirectory(requireActivity().getFilesDir());
            customDialog.dismiss();
        });

        Chip external = new Chip(requireContext());
        external.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        external.setText("External Data Files");
        external.setOnClickListener(v -> {
            setCurrentDirectory(requireActivity().getExternalFilesDir(null));
            customDialog.dismiss();
        });

        layout.addView(internal);
        layout.addView(external);


        customDialog.setTitle("Jump to path")
                .addView(input)
                .addView(textView)
                .addView(layout)
                .setPositiveButton("Go", view -> {
                    final File file = new File(input.getEditText().getText().toString());
                    if (file.exists()) {
                        if (file.canRead()) {
                            if (file.isFile()) {
                                new FileOpener((MainActivity) requireActivity()).openFile(file);
                            } else {
                                setCurrentDirectory(file);
                            }
                        } else {
                            App.showMsg(Log.UNABLE_TO + " read the provided file");
                        }
                    } else {
                        App.showMsg("The destination path doesn't exist!");
                    }
                }, true)
                .show(getParentFragmentManager(), "");
        return true;
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
                App.showMsg(Log.UNABLE_TO + " create folder: " + file.getAbsolutePath());
            } else {
                refresh();
                focusOn(file);
            }
        } else {
            try {
                if (!file.createNewFile()) {
                    App.showMsg(Log.UNABLE_TO + " " + FileUtils.CREATE_FILE + ": " + file.getAbsolutePath());
                } else {
                    refresh();
                    focusOn(file);
                }
            } catch (IOException e) {
                Log.e(TAG, e);
                App.showMsg(e.toString());
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
        initPathHistory();
    }

    private void initPathHistory() {
        pathHistoryRv.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false));
        pathHistoryRv.setAdapter(new PathHistoryAdapter(this));
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
     * - when select a directory from pathHistory RecyclerView
     */
    public void restoreRecyclerViewState() {
        Parcelable savedState = ((FileExplorerTabDataHolder) getDataHolder()).recyclerViewStates.get(getCurrentDirectory());
        if (savedState != null) {
            Objects.requireNonNull(fileList.getLayoutManager()).onRestoreInstanceState(savedState);
            ((FileExplorerTabDataHolder) getDataHolder()).recyclerViewStates.remove(getCurrentDirectory());
        }
    }

    /**
     * Refreshes both fileList and pathHistory recyclerview (used by #setCurrentDirectory(File) ONLY)
     */
    @SuppressLint("NotifyDataSetChanged")
    private void refreshFileList() {
        Objects.requireNonNull(fileList.getAdapter()).notifyDataSetChanged();
        Objects.requireNonNull(pathHistoryRv.getAdapter()).notifyDataSetChanged();
        pathHistoryRv.scrollToPosition(pathHistoryRv.getAdapter().getItemCount() - 1);
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
        new FileOpener((MainActivity) requireActivity()).openFile(fileItem.file);
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

    //______________| Getters and Setters |_______________\\

    public ArrayList<File> getPathHistory() {
        return pathHistory;
    }

    public File getCurrentDirectory() {
        return currentDirectory;
    }

    /**
     * This method handles the following (in order):
     * - Updating currentDirectory and previousDirectory fields
     * - Updating recyclerViewStates in DataHolder
     * - Sorting files based on the preferences
     * - Updating tabView title
     * - Update pathHistory list
     * - Refreshing adapters (fileList & pathHistory)
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
        updatePathHistoryList();
        refreshFileList();
        ((FileExplorerTabDataHolder) getDataHolder()).activeDirectory = getCurrentDirectory();
    }

    private void updatePathHistoryList() {
        ArrayList<File> list = new ArrayList<>();
        File file = getCurrentDirectory();
        while (file != null && file.canRead()) {
            list.add(file);
            file = file.getParentFile();
        }
        Collections.reverse(list);
        pathHistory = list;
    }

    public File getPreviousDirectory() {
        return previousDirectory;
    }
}
