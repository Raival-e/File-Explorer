package com.raival.quicktools.common;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.raival.quicktools.App;
import com.raival.quicktools.R;
import com.raival.quicktools.utils.FileUtil;

import java.io.File;
import java.util.ArrayList;

public class FileInfoDialog extends BottomSheetDialogFragment {
    ArrayList<InfoHolder> infoList = new ArrayList<>();
    boolean useDefaultFileInfo;
    File file;

    public FileInfoDialog(@NonNull File file) {
        this.file = file;
    }

    public FileInfoDialog setUseDefaultFileInfo(boolean is) {
        useDefaultFileInfo = is;
        return this;
    }

    public static class InfoHolder{
        public String name;
        public String info;
        public boolean clickable;

        public InfoHolder(String name, String info, boolean clickable) {
            this.name = name;
            this.info = info;
            this.clickable = clickable;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.file_info_dialog_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((TextView)view.findViewById(R.id.file_name)).setText(file.getName());
        FileUtil.setFileIcon((ImageView) view.findViewById(R.id.file_icon), file);

        if(!useDefaultFileInfo){
            ViewGroup container = view.findViewById(R.id.container);
            for(InfoHolder holder : infoList){
                addItemView(holder, container);
            }
        } else {
            if(file.isFile()){
                addDefaultFileInfo();
            } else {
                addDefaultFolderInfo();
            }
        }

    }

    private void addDefaultFolderInfo() {

    }

    private void addDefaultFileInfo() {

    }

    private TextView addItemView(InfoHolder holder, ViewGroup container) {
        View view = getLayoutInflater().inflate(R.layout.file_info_dialog_item, null, false);

        ((TextView)view.findViewById(R.id.name)).setText(holder.name);
        TextView details = view.findViewById(R.id.details);
        details.setText(holder.info);
        if(holder.clickable){
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
}
