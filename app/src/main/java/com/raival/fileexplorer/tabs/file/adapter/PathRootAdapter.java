package com.raival.fileexplorer.tabs.file.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.raival.fileexplorer.R;
import com.raival.fileexplorer.tabs.file.FileExplorerTabFragment;
import com.raival.fileexplorer.utils.AndroidUtil;
import com.raival.fileexplorer.utils.FileUtil;

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
        return new ViewHolder(parentFragment.requireActivity().getLayoutInflater().inflate(R.layout.file_explorer_tab_path_root_view, null));
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
            label.setText(FileUtil.isExternalStorageFolder(getRootList(parentFragment.getCurrentDirectory()).get(position))
                    ? FileUtil.INTERNAL_STORAGE
                    : getRootList(parentFragment.getCurrentDirectory()).get(position).getName());
            label.setTextColor((position == getItemCount() - 1)
                    ? AndroidUtil.getColorAttribute(R.attr.colorPrimary, parentFragment.requireActivity())
                    : AndroidUtil.getColorAttribute(R.attr.colorOutline, parentFragment.requireActivity()));
            itemView.setOnClickListener(view -> {
                parentFragment.setCurrentDirectory(getRootList(parentFragment.getCurrentDirectory()).get(position));
                // Restore RecyclerView state
                parentFragment.restoreRecyclerViewState();
            });
        }
    }
}