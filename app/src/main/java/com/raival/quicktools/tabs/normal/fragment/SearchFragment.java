package com.raival.quicktools.tabs.normal.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputLayout;
import com.raival.quicktools.App;
import com.raival.quicktools.R;
import com.raival.quicktools.tabs.normal.NormalTab;
import com.raival.quicktools.tabs.normal.models.FileItem;
import com.raival.quicktools.utils.FileExtensions;
import com.raival.quicktools.utils.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchFragment extends BottomSheetDialogFragment {
    NormalTab tab;
    ArrayList<File> filesToSearchIn;
    RecyclerView recyclerView;

    TextInputLayout input;
    CheckBox deepSearch;
    CheckBox regEx;
    CheckBox suffix;
    CheckBox prefix;
    Button searchButton;
    ProgressBar progress;
    TextView fileCount;

    Thread searchThread;
    boolean active = false;
    private String query;

    public SearchFragment(NormalTab tab, ArrayList<File> files) {
        this.tab = tab;
        filesToSearchIn = files;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        return inflater.inflate(R.layout.search_fragment_layout, container, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.rv);
        input = view.findViewById(R.id.input);
        deepSearch = view.findViewById(R.id.search_option_deep_search);
        regEx = view.findViewById(R.id.search_option_regex);
        suffix = view.findViewById(R.id.search_option_suffix);
        prefix = view.findViewById(R.id.search_option_prefix);
        searchButton = view.findViewById(R.id.search_button);
        progress = view.findViewById(R.id.progress);
        fileCount = view.findViewById(R.id.file_count);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(new RecyclerViewAdapter());

        progress.setVisibility(View.GONE);

        searchButton.setOnClickListener(view1 -> {
            if(active){
                searchButton.setText("Search");
                progress.setVisibility(View.GONE);
                recyclerView.getAdapter().notifyDataSetChanged();
                active = false;
                setCancelable(true);
            } else {
                setCancelable(false);
                searchButton.setText("Stop");
                tab.getSearchList().clear();
                recyclerView.getAdapter().notifyDataSetChanged();
                progress.setVisibility(View.VISIBLE);
                loseFocus(input);
                active = true;
                query = input.getEditText().getText().toString();
                searchThread = new Thread(()->{
                    for(File file : filesToSearchIn){
                        searchIn(file, deepSearch.isChecked(), regEx.isChecked(), prefix.isChecked(), suffix.isChecked());
                    }
                    recyclerView.post(()->{
                        searchButton.setText("Search");
                        progress.setVisibility(View.GONE);
                        recyclerView.getAdapter().notifyDataSetChanged();
                        active = false;
                        setCancelable(true);
                    });
                });
                searchThread.start();
            }
        });

        if(tab.getSearchList().size() > 0){
            fileCount.setVisibility(View.VISIBLE);
            updateFileCount();
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateFileCount() {
        fileCount.setText(tab.getSearchList().size() + " results found");
    }

    private void searchIn(File file, boolean isDeepSearch, boolean useRegex, boolean startWith, boolean endWith) {
        if(file.isFile()){
            if(isDeepSearch){
                if(useRegex){
                    try {
                        if(Pattern.compile(query).matcher(FileUtil.readFile(file)).find())
                            addFileItem(file);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                } else {
                    try {
                        if(FileUtil.readFile(file).contains(query))
                            addFileItem(file);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
            } else {
               if(startWith){
                   if(file.getName().startsWith(query))
                       addFileItem(file);
               }else if(endWith){
                   if(file.getName().endsWith(query))
                       addFileItem(file);
               } else {
                   if(file.getName().contains(query))
                       addFileItem(file);
               }
            }
        } else {
            File[] children = file.listFiles();
            if(children != null){
                for(File child : children){
                    searchIn(child, isDeepSearch, useRegex, startWith, endWith);
                }
            }
        }
    }

    private void addFileItem(File file) {
        tab.getSearchList().add(new FileItem(file, file.getAbsolutePath()));
        recyclerView.post(()-> {
            updateFileCount();
            recyclerView.getAdapter().notifyDataSetChanged();
        });
    }

    private void loseFocus(View view) {
        view.setEnabled(false);
        view.setEnabled(true);
    }

    private class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

        @NonNull
        @Override
        public RecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater _inflater = (LayoutInflater) requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            @SuppressLint("InflateParams") View _v = _inflater.inflate(R.layout.normal_tab_fragment_file_item, null);
            RecyclerView.LayoutParams _lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            _v.setLayoutParams(_lp);
            return new ViewHolder(_v);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {
            holder.bind();
        }

        @Override
        public int getItemCount() {
            return tab.getSearchList().size();
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
                final FileItem fileItem = tab.getSearchList().get(position);

                name.setText(fileItem.getName());
                details.setText(fileItem.getDetails());

                if(FileUtil.getFileExtension(fileItem.getFile()).equalsIgnoreCase(FileExtensions.apkType)){
                    loadApkIcon(fileItem, icon);
                } else if(fileItem.getIcon() == null){
                    FileUtil.setFileIcon(icon, fileItem.getFile());
                    fileItem.setIcon(icon.getDrawable());
                } else {
                    Glide.with(App.appContext)
                            .load(fileItem.getIcon())
                            .into(icon);
                }

                icon.setAlpha(fileItem.getFile().isHidden()? 0.5f : 1f);

                background.setOnClickListener((v)->{
                    tab.setCurrentPath(fileItem.getFile().getParentFile());
                    tab.refresh();
                    dismiss();
                });
            }

            private void loadApkIcon(FileItem fileItem, ImageView icon) {
                new Thread(()->{
                    PackageInfo info = App.appContext.getPackageManager().getPackageArchiveInfo(fileItem.getFile().getAbsolutePath(),
                            PackageManager.GET_ACTIVITIES);
                    if(info != null){
                        ApplicationInfo applicationInfo = info.applicationInfo;
                        applicationInfo.sourceDir = fileItem.getFile().getAbsolutePath();
                        applicationInfo.publicSourceDir = fileItem.getFile().getAbsolutePath();
                        recyclerView.post(()->{
                            icon.setImageDrawable(applicationInfo.loadIcon(App.appContext.getPackageManager()));
                            fileItem.setIcon(icon.getDrawable());
                        });
                    }
                }).start();
            }

        }
    }
}
