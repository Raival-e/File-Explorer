package com.raival.quicktools.tabs.normal.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputLayout;
import com.raival.quicktools.App;
import com.raival.quicktools.MainActivity;
import com.raival.quicktools.R;

import com.raival.quicktools.activities.TextEditorActivity;
import com.raival.quicktools.common.BackgroundTask;
import com.raival.quicktools.common.FileInfoDialog;
import com.raival.quicktools.common.OptionsDialog;
import com.raival.quicktools.common.QDialogFragment;
import com.raival.quicktools.tabs.normal.NormalTab;

import com.raival.quicktools.tabs.normal.models.FileItem;
import com.raival.quicktools.tasks.CompressTask;
import com.raival.quicktools.tasks.CopyTask;
import com.raival.quicktools.tasks.CutTask;
import com.raival.quicktools.tasks.ExtractTask;
import com.raival.quicktools.utils.FileExtensions;
import com.raival.quicktools.utils.FileUtil;
import com.raival.quicktools.utils.TimeUtil;


import java.io.File;
import java.util.ArrayList;

public class NormalTabFragment extends Fragment {
    NormalTab tab;
    RecyclerView recyclerView;
    View placeHolder;
    int prevPathHighlight = 0x06ffffff;
    int selectedFileHighlight = 0x320066ff;

    public NormalTabFragment(NormalTab tab){
        super();
        this.tab = tab;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.normal_tab_fragment_layout, container, false);
        recyclerView = view.findViewById(R.id.rv);
        placeHolder = view.findViewById(R.id.place_holder);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initRv();
    }

    private void initRv() {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        recyclerView.setAdapter(new RecyclerViewAdapter());
        recyclerView.setHasFixedSize(true);
        updateFilesCount();
    }

    public Parcelable getRecyclerViewInstance() {
        if(recyclerView == null || recyclerView.getLayoutManager() == null) return null;
        return recyclerView.getLayoutManager().onSaveInstanceState();
    }

    public void setRecyclerViewInstance(Parcelable parcelable){
        if(recyclerView == null || recyclerView.getLayoutManager() == null) return;
        recyclerView.getLayoutManager().onRestoreInstanceState(parcelable);
    }

    public void updateFilesList(){
        tab.updateTabName();
        recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.scrollToPosition(0);
        updateFilesCount();
    }

    private void updateFilesCount() {
        if(requireActivity() instanceof MainActivity){
            ((MainActivity)requireActivity()).setPageSubtitle(FileUtil.getFormattedFileCount(tab.getCurrentPath()));
        }
        updatePathTreeView();
    }

    private void updatePathTreeView() {
        if(requireActivity() instanceof MainActivity){
            ((MainActivity)requireActivity()).updatePathTreeView();
        }
    }

    private class RvDataObserver extends RecyclerView.AdapterDataObserver {
        public RvDataObserver() {
            checkIfEmpty();
        }

        @Override
        public void onChanged() {
            super.onChanged();
            checkIfEmpty();
        }

        private void checkIfEmpty() {
            if(recyclerView.getAdapter() != null && recyclerView.getAdapter().getItemCount() == 0){
                placeHolder.setVisibility(View.VISIBLE);
            } else {
                placeHolder.setVisibility(View.GONE);
            }
        }
    }

    private class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
        public RecyclerViewAdapter(){
            registerAdapterDataObserver(new RvDataObserver());
        }

        @NonNull
        @Override
        public RecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater _inflater = (LayoutInflater) requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            @SuppressLint("InflateParams") View _v = _inflater.inflate(R.layout.normal_tab_fragment_file_item, null);
            RecyclerView.LayoutParams _lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            _v.setLayoutParams(_lp);
            return new RecyclerViewAdapter.ViewHolder(_v);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {
            holder.bind();
        }

        @Override
        public int getItemCount() {
            return tab.getFilesList().size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView name;
            TextView details;
            ImageView icon;
            View background;

            public ViewHolder(View v) {
                super(v);
                name = v.findViewById(R.id.file_name);
                details = v.findViewById(R.id.file_details);
                icon = v.findViewById(R.id.file_icon);
                background = v.findViewById(R.id.background);
            }

            public void bind(){
                final int position = getAdapterPosition();
                final FileItem fileItem = tab.getFilesList().get(position);

                name.setText(fileItem.getName());
                details.setText(fileItem.getDetails());

                if(fileItem.isSelected()) {
                    background.setForeground(new ColorDrawable(selectedFileHighlight));
                } else if(tab.shouldHighlightFile(fileItem.getFile())){
                    background.setForeground(new ColorDrawable(prevPathHighlight));
                } else {
                    background.setForeground(null);
                }

                if(FileUtil.getFileExtension(fileItem.getFile()).toLowerCase().equals(FileExtensions.apkType)){
                    loadApkIcon(fileItem.getFile(), icon);
                } else if(fileItem.getIcon() == null){
                    FileUtil.setFileIcon(icon, fileItem.getFile());
                    fileItem.setIcon(icon.getDrawable());
                } else {
                    Glide.with(App.appContext)
                            .load(tab.getFilesList().get(position).getIcon())
                            .into(icon);
                }

                icon.setAlpha(fileItem.getFile().isHidden()? 0.5f : 1f);

                itemView.findViewById(R.id.icon_container).setOnClickListener(view -> {
                    fileItem.changeSelection();
                    notifyItemChanged(position);
                });

                itemView.setOnClickListener(view -> {
                    if(tab.hasSelectedFiles()){
                        fileItem.changeSelection();
                        notifyItemChanged(position);
                        return;
                    }
                    if(fileItem.getFile().isFile()){
                        FileUtil.openFileWith(fileItem.getFile(), false);
                    } else {
                        tab.setCurrentPath(fileItem.getFile());
                        updateFilesList();
                    }
                });

                itemView.findViewById(R.id.icon_container).setOnLongClickListener(view -> {
                    showFileOptions(position);
                    return true;
                });

                itemView.setOnLongClickListener(view -> {
                    showFileOptions(position);
                    return true;
                });
            }

            private void loadApkIcon(File file, ImageView icon) {
                new Thread(()->{
                    PackageInfo info = App.appContext.getPackageManager().getPackageArchiveInfo(file.getAbsolutePath(),
                            PackageManager.GET_ACTIVITIES);
                    if(info != null){
                        ApplicationInfo applicationInfo = info.applicationInfo;
                        applicationInfo.sourceDir = file.getAbsolutePath();
                        applicationInfo.publicSourceDir = file.getAbsolutePath();
                        recyclerView.post(()->icon.setImageDrawable(applicationInfo.loadIcon(App.appContext.getPackageManager())));
                    }
                }).start();
            }
        }
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    private void showFileOptions(int position) {
        ArrayList<File> selectedFiles = tab.getSelectedFiles();
        if(selectedFiles.size() == 0){
            selectedFiles.add(tab.getFilesList().get(position).getFile());
        }

        String title = "";
        if(selectedFiles.size() == 1){
            title = tab.getFilesList().get(position).getName();
        } else {
            title = "" + selectedFiles.size() + " Files selected";
        }

        OptionsDialog bottomDialog = new OptionsDialog(title);
        bottomDialog.show(getParentFragmentManager(), "FileOptionsDialog");
        if(FileUtil.isSingleFolder(selectedFiles)){
            bottomDialog.addOption("Open in a new tab", R.drawable.ic_round_tab_24, view1 ->{
                if(requireActivity() instanceof MainActivity){
                    ((MainActivity)requireActivity()).addNewTab(selectedFiles.get(0));
                }
            }, true);
        }
        if (FileUtil.isOnlyFiles(selectedFiles)) {
            bottomDialog.addOption("Share", R.drawable.ic_round_share_24, view1 ->{
                shareFiles(selectedFiles);
                unSelectAndUpdateList();
            }, true);
        }

        bottomDialog.addOption("Copy", R.drawable.ic_baseline_file_copy_24, view1 ->{
            addCopyTask(selectedFiles);
            unSelectAndUpdateList();
            App.showMsg("New task has been added");
        }, true);

        bottomDialog.addOption("Cut", R.drawable.ic_round_content_cut_24, view1 ->{
            addCutTask(selectedFiles);
            unSelectAndUpdateList();
            App.showMsg("New task has been added");
        }, true);

        if(selectedFiles.size() == 1){
            bottomDialog.addOption("Rename", R.drawable.ic_round_edit_24, view1 ->{
                showRenameDialog(selectedFiles);
            }, true);
        }

        bottomDialog.addOption("Delete", R.drawable.ic_round_delete_forever_24, view1 ->{
            confirmDeletion(selectedFiles);
        }, true);

        if(FileUtil.isSingleFile(selectedFiles)){
            bottomDialog.addOption("Edit with code editor", R.drawable.ic_round_edit_note_24, view1 ->{
                openWithTextEditor(selectedFiles.get(0));
            }, true);
        }

        if(FileUtil.isArchiveFiles(selectedFiles)){
            bottomDialog.addOption("Extract", R.drawable.ic_baseline_open_in_new_24, view1 ->{
                addExtractTask(selectedFiles);
                unSelectAndUpdateList();
                App.showMsg("New task has been added");
            }, true);
        }

        bottomDialog.addOption("Compress", R.drawable.ic_round_compress_24, view1 ->{
            addCompressTask(selectedFiles);
            unSelectAndUpdateList();
            App.showMsg("New task has been added");
        }, true);

        if(selectedFiles.size() == 1){
            bottomDialog.addOption("Details", R.drawable.ic_baseline_info_24, view1 ->{
                showFileInfoDialog(selectedFiles.get(0));
            }, true);
        }

        bottomDialog.addOption("Find & Replace", R.drawable.ic_round_search_24, view1 ->{
            //do find&replace task
        }, true);

        bottomDialog.addOption("Deep search", R.drawable.ic_round_manage_search_24, view1 ->{
            //do search task
        }, true);
    }

    private void openWithTextEditor(File file) {
        Intent intent = new Intent();
        intent.setClass(requireActivity(), TextEditorActivity.class);
        intent.putExtra("file", file.getAbsolutePath());
        requireActivity().startActivity(intent);
    }

    private void showFileInfoDialog(File file) {
        new FileInfoDialog(file).setUseDefaultFileInfo(true).show(getParentFragmentManager(), "");
    }

    private void addExtractTask(ArrayList<File> selectedFiles) {
        if(requireActivity() instanceof MainActivity){
            ((MainActivity)requireActivity()).AddTask(new ExtractTask(selectedFiles));
        }
    }

    private void addCompressTask(ArrayList<File> selectedFiles) {
        if(requireActivity() instanceof MainActivity){
            ((MainActivity)requireActivity()).AddTask(new CompressTask(selectedFiles));
        }
    }

    private void shareFiles(ArrayList<File> selectedFiles) {
        FileUtil.shareFiles(selectedFiles, requireActivity());
    }

    private void showRenameDialog(ArrayList<File> selectedFiles) {
        TextInputLayout input = (TextInputLayout) getLayoutInflater().inflate(R.layout.input, null, false);
        input.setHint("File name");
        input.getEditText().setText(selectedFiles.get(0).getName());
        FileUtil.setFileInvalidator(input, selectedFiles.get(0), selectedFiles.get(0).getParentFile());

        new QDialogFragment()
                .setTitle("Rename")
                .addView(input)
                .setPositiveButton("Save", view -> {
                    if(input.getError() == null){
                        if(!FileUtil.rename(selectedFiles.get(0), input.getEditText().getText().toString())){
                            App.showMsg("Cannot rename this file");
                        } else {
                            App.showMsg("File has been renamed");
                            tab.refresh();
                        }
                    } else {
                        App.showMsg("Rename canceled");
                    }
                }, true)
                .showDialog(getParentFragmentManager(), "");
    }

    private void addCutTask(ArrayList<File> selectedFiles) {
        if(requireActivity() instanceof MainActivity){
            ((MainActivity)requireActivity()).AddTask(new CutTask(selectedFiles));
        }
    }

    private void unSelectAndUpdateList() {
        for(FileItem item : tab.getFilesList()){
            item.setSelected(false);
        }
        updateFilesList();
    }

    private void confirmDeletion(ArrayList<File> selectedFiles) {
        new QDialogFragment()
                .setTitle("Delete")
                .setMsg("Do you want to delete selected files? this action cannot be redone.")
                .setPositiveButton("Confirm", (view -> doDelete(selectedFiles)), true)
                .setNegativeButton("Cancel", null, true)
                .showDialog(getParentFragmentManager(), "");
    }

    private void doDelete(ArrayList<File> selectedFiles) {
        BackgroundTask backgroundTask = new BackgroundTask();
        backgroundTask.setTasks(()->{
            backgroundTask.showProgressDialog("Deleting files...", requireActivity());
        }, ()->{
            FileUtil.deleteFiles(selectedFiles);
        }, ()->{
            backgroundTask.dismiss();
            App.showMsg("Files has been deleted");
            tab.refresh();
        });
        backgroundTask.run();
    }

    private void addCopyTask(ArrayList<File> selectedFiles) {
        CopyTask copyTask = new CopyTask(selectedFiles);

        if(requireActivity() instanceof MainActivity){
            ((MainActivity)requireActivity()).AddTask(copyTask);
        }
    }
}
