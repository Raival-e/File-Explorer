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

import java.io.File;
import java.util.ArrayList;

public class NormalTabFragment extends Fragment {
    NormalTab tab;
    RecyclerView recyclerView;

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
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        assignComparators();
        initRv();
    }

    private void assignComparators() {
        tab.setComparators(FileUtil.sortNameAsc(), FileUtil.sortByFolders());
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
        int files = 0;
        int folders = 0;

        for(FileItem item : tab.getFilesList()){
            if(item.getFile().isFile()) files++;
            else folders++;
        }

        if(requireActivity() instanceof MainActivity){
            StringBuilder sb = new StringBuilder();
            if(folders > 0){
               sb.append(folders);
               sb.append(" folder");
               if(folders > 1) sb.append("s");
               if(files > 0) sb.append(", ");
            }
            if(files > 0){
                sb.append(files);
                sb.append(" file");
                if(files > 1) sb.append("s");
            }
            ((MainActivity)requireActivity()).setPageSubtitle(sb.toString());
        }
    }

    private class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
        public RecyclerViewAdapter(){ }

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

            public ViewHolder(View v) {
                super(v);
                name = v.findViewById(R.id.file_name);
                details = v.findViewById(R.id.file_details);
                icon = v.findViewById(R.id.file_icon);
            }

            public void bind(){
                final int position = getAdapterPosition();

                name.setText(tab.getFilesList().get(position).getName());
                details.setText(tab.getFilesList().get(position).getDetails());

                if(tab.shouldHighlightFile(tab.getFilesList().get(position).getFile())){
                    itemView.findViewById(R.id.background).setForeground(new ColorDrawable(0x06ffffff));
                } else {
                    itemView.findViewById(R.id.background).setForeground(null);
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

                itemView.setOnClickListener(view -> {
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

    private void showFileOptions(int position) {
        ArrayList<File> selectedFiles = new ArrayList<>();
        selectedFiles.add(tab.getFilesList().get(position).getFile());

        BottomOptionsDialog bottomDialog = new BottomOptionsDialog(tab.getFilesList().get(position).getName());
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
