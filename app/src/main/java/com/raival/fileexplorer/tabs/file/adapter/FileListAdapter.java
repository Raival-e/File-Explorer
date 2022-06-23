package com.raival.fileexplorer.tabs.file.adapter;

import android.annotation.SuppressLint;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.raival.fileexplorer.R;
import com.raival.fileexplorer.tabs.file.FileExplorerTabFragment;
import com.raival.fileexplorer.tabs.file.holder.FileExplorerTabDataHolder;
import com.raival.fileexplorer.tabs.file.model.FileItem;
import com.raival.fileexplorer.tabs.file.observer.FileListObserver;
import com.raival.fileexplorer.utils.FileUtils;

public class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.ViewHolder> {
    private final FileExplorerTabFragment parentFragment;

    public FileListAdapter(FileExplorerTabFragment parentFragment) {
        this.parentFragment = parentFragment;
        registerAdapterDataObserver(new FileListObserver(parentFragment, this));
    }

    @NonNull
    @Override
    public FileListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        @SuppressLint("InflateParams") View _v = parentFragment.getLayoutInflater().inflate(R.layout.file_explorer_tab_fragment_file_item, null);
        _v.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return new FileListAdapter.ViewHolder(_v);
    }

    @Override
    public void onBindViewHolder(@NonNull FileListAdapter.ViewHolder holder, int position) {
        holder.bind();
    }

    @Override
    public int getItemCount() {
        return parentFragment.getFiles().size();
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

        /**
         * Update the ui of each item
         */
        public void bind() {
            final int position = getAdapterPosition();
            final FileItem fileItem = parentFragment.getFiles().get(position);

            FileUtils.setFileIcon(icon, fileItem.file);
            name.setText(fileItem.file.getName());
            details.setText(FileUtils.getFileDetails(fileItem.file));

            // Hidden files will be 50% transparent
            icon.setAlpha(fileItem.file.isHidden() ? 0.5f : 1f);

            // Set a proper background color
            if (fileItem.isSelected) {
                background.setForeground(new ColorDrawable(parentFragment.getContext().getColor(R.color.selectedFileHighlight)));
            } else if (parentFragment.getPreviousDirectory() != null
                    && fileItem.file.getAbsolutePath().equals(parentFragment.getPreviousDirectory().getAbsolutePath())) {
                background.setForeground(new ColorDrawable(parentFragment.getContext().getColor(R.color.previousFileHighlight)));
            } else {
                background.setForeground(null);
            }

            // Select/unselect item by pressing the icon
            itemView.findViewById(R.id.icon_container).setOnClickListener(view -> {
                fileItem.isSelected = !fileItem.isSelected;
                if(fileItem.isSelected){
                    ((FileExplorerTabDataHolder)parentFragment.getDataHolder()).selectedFiles.add(fileItem.file);
                } else {
                    ((FileExplorerTabDataHolder)parentFragment.getDataHolder()).selectedFiles.remove(fileItem.file);
                }
                notifyItemChanged(position);
            });

            // Handle click event
            background.setOnClickListener(view -> {
                if (fileItem.file.isFile()) {
                    parentFragment.openFile(fileItem);
                } else {
                    parentFragment.setCurrentDirectory(fileItem.file);
                    // Clear any selected files from the DataHolder (it also gets cleared
                    // in onBackPressed (when go back)
                    ((FileExplorerTabDataHolder)parentFragment.getDataHolder()).selectedFiles.clear();
                }
            });

            View.OnLongClickListener longClickListener = view -> {
                parentFragment.showFileOptions(fileItem);
                return true;
            };

            // Apply the listener for both the background and the icon
            itemView.findViewById(R.id.icon_container).setOnLongClickListener(longClickListener);
            itemView.setOnLongClickListener(longClickListener);

        }
    }
}