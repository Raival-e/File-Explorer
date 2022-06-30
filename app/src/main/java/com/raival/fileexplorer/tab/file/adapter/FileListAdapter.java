package com.raival.fileexplorer.tab.file.adapter;

import android.annotation.SuppressLint;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.raival.fileexplorer.R;
import com.raival.fileexplorer.tab.file.FileExplorerTabDataHolder;
import com.raival.fileexplorer.tab.file.FileExplorerTabFragment;
import com.raival.fileexplorer.tab.file.model.FileItem;
import com.raival.fileexplorer.tab.file.observer.FileListObserver;
import com.raival.fileexplorer.tab.file.util.FileExtensions;
import com.raival.fileexplorer.tab.file.util.FileUtils;

import java.io.File;

public class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.ViewHolder> {
    private final FileExplorerTabFragment parentFragment;
    private final ColorDrawable selectedFileDrawable;
    private final ColorDrawable highlightedFileDrawable;

    public FileListAdapter(FileExplorerTabFragment parentFragment) {
        this.parentFragment = parentFragment;
        registerAdapterDataObserver(new FileListObserver(parentFragment, this));
        selectedFileDrawable = new ColorDrawable(parentFragment.getContext().getColor(R.color.selectedFileHighlight));
        highlightedFileDrawable = new ColorDrawable(parentFragment.getContext().getColor(R.color.previousFileHighlight));
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
        View divider;

        File prevFile;

        public ViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.file_name);
            details = v.findViewById(R.id.file_details);
            icon = v.findViewById(R.id.file_icon);
            background = v.findViewById(R.id.background);
            divider = v.findViewById(R.id.divider);
        }

        /**
         * Update the ui of each item
         */
        public void bind() {
            final int position = getAdapterPosition();
            final FileItem fileItem = parentFragment.getFiles().get(position);

            if (!FileUtils.getFileExtension(fileItem.file).equalsIgnoreCase(FileExtensions.apkType)) {
                if (prevFile == null) {
                    FileUtils.setFileIcon(icon, fileItem.file);
                } else if (!fileItem.file.isDirectory()) {
                    FileUtils.setFileIcon(icon, fileItem.file);
                } else if (!prevFile.isDirectory()) {
                    FileUtils.setFileIcon(icon, fileItem.file);
                }
            } else {
                fileItem.img.observe(parentFragment, (drawable -> icon.setImageDrawable(drawable)));
            }

            if (TextUtils.isEmpty(fileItem.name)) {
                name.setText(fileItem.file.getName());
            } else {
                name.setText(fileItem.name);
            }

            if (TextUtils.isEmpty(fileItem.details)) {
                details.setText(FileUtils.getFileDetails(fileItem.file));
            } else {
                details.setText(fileItem.details);
            }

            if (position == getItemCount() - 1) {
                divider.setVisibility(View.GONE);
            } else {
                divider.setVisibility(View.VISIBLE);
            }

            // Hidden files will be 50% transparent
            if (fileItem.file.isHidden()) {
                if (icon.getAlpha() == 1) icon.setAlpha(0.5f);
            } else {
                if (icon.getAlpha() < 1) icon.setAlpha(1f);
            }

            // Set a proper background color
            if (fileItem.isSelected) {
                if (background.getForeground() != selectedFileDrawable)
                    background.setForeground(selectedFileDrawable);
            } else if (parentFragment.getPreviousDirectory() != null
                    && fileItem.file.getAbsolutePath().equals(parentFragment.getPreviousDirectory().getAbsolutePath())) {
                if (background.getForeground() != highlightedFileDrawable)
                    background.setForeground(highlightedFileDrawable);
            } else {
                if (background.getForeground() != null) background.setForeground(null);
            }

            // Select/unselect item by pressing the icon
            itemView.findViewById(R.id.icon_container).setOnClickListener(view -> {
                fileItem.isSelected = !fileItem.isSelected;
                if (fileItem.isSelected) {
                    ((FileExplorerTabDataHolder) parentFragment.getDataHolder()).selectedFiles.add(fileItem.file);
                } else {
                    ((FileExplorerTabDataHolder) parentFragment.getDataHolder()).selectedFiles.remove(fileItem.file);
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
                    ((FileExplorerTabDataHolder) parentFragment.getDataHolder()).selectedFiles.clear();
                }
            });

            View.OnLongClickListener longClickListener = view -> {
                parentFragment.showFileOptions(fileItem);
                return true;
            };

            // Apply the listener for both the background and the icon
            itemView.findViewById(R.id.icon_container).setOnLongClickListener(longClickListener);
            background.setOnLongClickListener(longClickListener);

            prevFile = fileItem.file;
        }
    }
}