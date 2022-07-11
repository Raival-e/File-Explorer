package com.raival.fileexplorer.tab.file.adapter;

import android.annotation.SuppressLint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.raival.fileexplorer.R;
import com.raival.fileexplorer.tab.file.FileExplorerTabFragment;
import com.raival.fileexplorer.tab.file.dialog.FileInfoDialog;
import com.raival.fileexplorer.tab.file.util.FileUtils;
import com.raival.fileexplorer.util.Utils;

import java.io.File;

public class PathHistoryAdapter extends RecyclerView.Adapter<PathHistoryAdapter.ViewHolder> {
    private final FileExplorerTabFragment parentFragment;

    public PathHistoryAdapter(FileExplorerTabFragment parentFragment) {
        this.parentFragment = parentFragment;
    }

    @NonNull
    @Override
    public PathHistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        @SuppressLint("InflateParams") View v = parentFragment.requireActivity().getLayoutInflater().inflate(R.layout.file_explorer_tab_path_history_view, null);
        v.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PathHistoryAdapter.ViewHolder holder, int position) {
        holder.bind();
    }

    @Override
    public int getItemCount() {
        return parentFragment.getPathHistory().size();
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        TextView label;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.text);
        }

        public void bind() {
            final int position = getAdapterPosition();
            final File file = parentFragment.getPathHistory().get(position);

            label.setText(FileUtils.isExternalStorageFolder(file)
                    ? FileUtils.INTERNAL_STORAGE
                    : file.getName());
            label.setTextColor((position == getItemCount() - 1)
                    ? Utils.getColorAttribute(R.attr.colorPrimary, parentFragment.requireActivity())
                    : Utils.getColorAttribute(R.attr.colorOutline, parentFragment.requireActivity()));
            itemView.setOnClickListener(view -> {
                parentFragment.setCurrentDirectory(file);
                // Restore RecyclerView state
                parentFragment.restoreRecyclerViewState();
            });
            itemView.setOnLongClickListener(v -> {
                new FileInfoDialog(file).setUseDefaultFileInfo(true).show(parentFragment.getParentFragmentManager(), "");
                return true;
            });
        }
    }
}