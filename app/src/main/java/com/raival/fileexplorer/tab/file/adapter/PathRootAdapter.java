package com.raival.fileexplorer.tab.file.adapter;

import android.annotation.SuppressLint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.raival.fileexplorer.R;
import com.raival.fileexplorer.tab.file.FileExplorerTabFragment;
import com.raival.fileexplorer.util.FileUtils;
import com.raival.fileexplorer.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class PathRootAdapter extends RecyclerView.Adapter<PathRootAdapter.ViewHolder> {
    private final FileExplorerTabFragment parentFragment;

    public PathRootAdapter(FileExplorerTabFragment parentFragment) {
        this.parentFragment = parentFragment;
    }

    @NonNull
    @Override
    public PathRootAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        @SuppressLint("InflateParams") View v = parentFragment.requireActivity().getLayoutInflater().inflate(R.layout.file_explorer_tab_path_root_view, null);
        v.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PathRootAdapter.ViewHolder holder, int position) {
        holder.bind();
    }

    @Override
    public int getItemCount() {
        return getRootList(parentFragment.getCurrentDirectory()).size();
    }

    //TODO: add this method to FileExplorerTabFragment and call it when setCurrentDirectory is called and
    // store the result
    public ArrayList<File> getRootList(File dir) {
        ArrayList<File> list = new ArrayList<>();
        File file = dir;
        while (file != null && file.canRead()) {
            list.add(file);
            file = file.getParentFile();
        }
        Collections.reverse(list);
        return list;
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        TextView label;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.text);
        }

        public void bind() {
            final int position = getAdapterPosition();
            label.setText(FileUtils.isExternalStorageFolder(getRootList(parentFragment.getCurrentDirectory()).get(position))
                    ? FileUtils.INTERNAL_STORAGE
                    : getRootList(parentFragment.getCurrentDirectory()).get(position).getName());
            label.setTextColor((position == getItemCount() - 1)
                    ? Utils.getColorAttribute(R.attr.colorPrimary, parentFragment.requireActivity())
                    : Utils.getColorAttribute(R.attr.colorOutline, parentFragment.requireActivity()));
            itemView.setOnClickListener(view -> {
                parentFragment.setCurrentDirectory(getRootList(parentFragment.getCurrentDirectory()).get(position));
                // Restore RecyclerView state
                parentFragment.restoreRecyclerViewState();
            });
        }
    }
}