package com.raival.quicktools.tabs.normal.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
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

import com.raival.quicktools.App;
import com.raival.quicktools.R;
import com.raival.quicktools.fragments.CommonBottomDialog;
import com.raival.quicktools.tabs.normal.NormalTab;
import com.raival.quicktools.utils.FileUtil;

import java.io.File;

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
    }

    public void updateFilesList(){
        tab.updateTabName();
        recyclerView.getAdapter().notifyDataSetChanged();
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
                FileUtil.setFileIcon(icon, tab.getFilesList().get(position).getFile());

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
                    CommonBottomDialog bottomDialog = new CommonBottomDialog(name.getText().toString());
                    bottomDialog.show(getParentFragmentManager(), "");

                    bottomDialog.addOption("copy", R.drawable.ic_baseline_assignment_24, view1 ->{
                        App.showMsg("test");
                    } , true);

                    return true;
                });
            }
        }
    }
}
