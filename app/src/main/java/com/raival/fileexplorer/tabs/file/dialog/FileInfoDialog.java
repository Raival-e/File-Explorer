package com.raival.fileexplorer.tabs.file.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.raival.fileexplorer.App;
import com.raival.fileexplorer.R;
import com.raival.fileexplorer.utils.FileUtil;
import com.raival.fileexplorer.utils.TimeUtil;

import java.io.File;
import java.util.ArrayList;

public class FileInfoDialog extends BottomSheetDialogFragment {
    private final ArrayList<InfoHolder> infoList = new ArrayList<>();
    private final File file;
    private boolean useDefaultFileInfo;
    private ViewGroup container;

    public FileInfoDialog(@NonNull File file) {
        this.file = file;
    }

    public FileInfoDialog setUseDefaultFileInfo(boolean is) {
        useDefaultFileInfo = is;
        return this;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.file_explorer_tab_info_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((TextView) view.findViewById(R.id.file_name)).setText(file.getName());
        FileUtil.setFileIcon(view.findViewById(R.id.file_icon), file);

        container = view.findViewById(R.id.container);

        if (!useDefaultFileInfo) {
            for (InfoHolder holder : infoList) {
                addItemView(holder, container);
            }
        } else {
            if (file.isFile()) {
                addDefaultFileInfo();
            } else {
                addDefaultFolderInfo();
            }
        }

    }

    private void addDefaultFolderInfo() {
        addItemView(new InfoHolder("Path:", file.getAbsolutePath(), true), container);
        addItemView(new InfoHolder("Modified:", TimeUtil.getLastModifiedDate(file, TimeUtil.REGULAR_DATE_FORMAT), true), container);
        addItemView(new InfoHolder("Content:", FileUtil.getFormattedFileCount(file), true), container);
        addItemView(new InfoHolder("Type:", file.isFile() ? "File" : "Folder", true), container);
        addItemView(new InfoHolder("Read:", file.canRead() ? "Yes" : "No", true), container);
        addItemView(new InfoHolder("Write:", file.canWrite() ? "Yes" : "No", true), container);

        TextView size = addItemView(new InfoHolder("Size:", "Counting...", true), container);
        new Thread(() -> {
            String s = FileUtil.getFormattedFileSize(file);
            size.post(() -> size.setText(s));
        }).start();
    }

    private void addDefaultFileInfo() {
        addItemView(new InfoHolder("Path:", file.getAbsolutePath(), true), container);
        addItemView(new InfoHolder("Extension:", FileUtil.getFileExtension(file), true), container);
        addItemView(new InfoHolder("Modified:", TimeUtil.getLastModifiedDate(file, TimeUtil.REGULAR_DATE_FORMAT), true), container);
        addItemView(new InfoHolder("Type:", file.isFile() ? "File" : "Folder", true), container);
        addItemView(new InfoHolder("Read:", file.canRead() ? "Yes" : "No", true), container);
        addItemView(new InfoHolder("Write:", file.canWrite() ? "Yes" : "No", true), container);
        addItemView(new InfoHolder("Size:", FileUtil.getFormattedFileSize(file), true), container);
    }

    private TextView addItemView(InfoHolder holder, ViewGroup container) {
        View view = getLayoutInflater().inflate(R.layout.file_explorer_tab_info_dialog_item, null, false);

        ((TextView) view.findViewById(R.id.name)).setText(holder.name);
        TextView details = view.findViewById(R.id.details);
        details.setText(holder.info);
        if (holder.clickable) {
            details.setClickable(true);
            details.setOnClickListener(view1 -> {
                App.copyString(holder.info);
                App.showMsg(holder.name + " has been copied");
            });
        }
        container.addView(view);
        return details;
    }

    public FileInfoDialog addItem(String name, String info, boolean clickable) {
        infoList.add(new InfoHolder(name, info, clickable));
        return this;
    }

    @Override
    public int getTheme() {
        return R.style.ThemeOverlay_Material3_BottomSheetDialog;
    }

    public static class InfoHolder {
        public String name;
        public String info;
        public boolean clickable;

        public InfoHolder(String name, String info, boolean clickable) {
            this.name = name;
            this.info = info;
            this.clickable = clickable;
        }
    }
}
