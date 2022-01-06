package com.raival.quicktools.tabs.normal.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
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
import com.raival.quicktools.App;
import com.raival.quicktools.MainActivity;
import com.raival.quicktools.R;
import com.raival.quicktools.common.BottomOptionsDialog;
import com.raival.quicktools.tabs.normal.NormalTab;
import com.raival.quicktools.tabs.normal.models.FileItem;
import com.raival.quicktools.utils.FileUtil;
import com.raival.quicktools.utils.PrefsUtil;

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

                name.setText(tab.getFilesList().get(position).getName());
                details.setText(tab.getFilesList().get(position).getDetails());

                if(tab.getFilesList().get(position).isSelected()) {
                    background.setForeground(new ColorDrawable(selectedFileHighlight));
                } else if(tab.shouldHighlightFile(tab.getFilesList().get(position).getFile())){
                    background.setForeground(new ColorDrawable(prevPathHighlight));
                } else {
                    background.setForeground(null);
                }


                if(tab.getFilesList().get(position).getIcon() == null){
                    FileUtil.setFileIcon(icon, tab.getFilesList().get(position).getFile());
                    tab.getFilesList().get(position).setIcon(icon.getDrawable());
                } else {
                    Glide.with(App.appContext)
                            .load(tab.getFilesList().get(position).getIcon())
                            .into(icon);
                }

                icon.setAlpha(tab.getFilesList().get(position).getFile().isHidden()? 0.5f : 1f);

                itemView.findViewById(R.id.icon_container).setOnClickListener(view -> {
                    tab.getFilesList().get(position).changeSelection();
                    notifyItemChanged(position);
                });

                itemView.setOnClickListener(view -> {
                    if(tab.hasSelectedFiles()){
                        tab.getFilesList().get(position).changeSelection();
                        notifyItemChanged(position);
                        return;
                    }
                    if(tab.getFilesList().get(position).getFile().isFile()){
                        FileUtil.openFileWith(tab.getFilesList().get(position).getFile(), false);
                    } else {
                        tab.setCurrentPath(tab.getFilesList().get(position).getFile());
                        updateFilesList();
                    }
                });

                itemView.setOnLongClickListener(view -> {
                    showFileOptions(position);
                    return true;
                });
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

        BottomOptionsDialog bottomDialog = new BottomOptionsDialog(title);
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
                //do share task
            }, true);
        }

        bottomDialog.addOption("Copy", R.drawable.ic_baseline_file_copy_24, view1 ->{
            //do copy task
        }, true);

        bottomDialog.addOption("Cut", R.drawable.ic_round_content_cut_24, view1 ->{
            //do cut task
        }, true);

        if(selectedFiles.size() == 1){
            bottomDialog.addOption("Rename", R.drawable.ic_round_edit_24, view1 ->{
                //do rename task
            }, true);
        }

        bottomDialog.addOption("Delete", R.drawable.ic_round_delete_forever_24, view1 ->{
            //do delete task
        }, true);

        if(FileUtil.isSingleFile(selectedFiles)){
            bottomDialog.addOption("Edit with code editor", R.drawable.ic_round_edit_note_24, view1 ->{
                //do edit task
            }, true);
        }

        if(FileUtil.isSingleArchive(selectedFiles)){
            bottomDialog.addOption("Extract", R.drawable.ic_baseline_open_in_new_24, view1 ->{
                //do extract task
            }, true);
        }

        bottomDialog.addOption("Compress", R.drawable.ic_round_compress_24, view1 ->{
            //do compress task
        }, true);

        bottomDialog.addOption("Details", R.drawable.ic_baseline_info_24, view1 ->{
            //do compress task
        }, true);

        bottomDialog.addOption("Find & Replace", R.drawable.ic_round_search_24, view1 ->{
            //do find&replace task
        }, true);

        bottomDialog.addOption("Deep search", R.drawable.ic_round_manage_search_24, view1 ->{
            //do search task
        }, true);


    }
}
