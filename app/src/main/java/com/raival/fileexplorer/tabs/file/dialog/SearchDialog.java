package com.raival.fileexplorer.tabs.file.dialog;

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
import com.raival.fileexplorer.App;
import com.raival.fileexplorer.R;
import com.raival.fileexplorer.tabs.file.FileExplorerTabFragment;
import com.raival.fileexplorer.tabs.file.model.FileItem;
import com.raival.fileexplorer.utils.FileExtensions;
import com.raival.fileexplorer.utils.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class SearchDialog extends BottomSheetDialogFragment {
    private final FileExplorerTabFragment tab;
    private final ArrayList<File> filesToSearchIn;
    private RecyclerView recyclerView;

    private TextInputLayout input;
    private CheckBox deepSearch;
    private CheckBox regEx;
    private CheckBox suffix;
    private CheckBox prefix;
    private Button searchButton;
    private ProgressBar progress;
    private TextView fileCount;

    private Thread searchThread;
    private boolean active = false;
    private String query;

    public SearchDialog(FileExplorerTabFragment tab, File directory) {
        filesToSearchIn = new ArrayList<>();
        filesToSearchIn.add(directory);
        this.tab = tab;
    }

    public SearchDialog(FileExplorerTabFragment tab, ArrayList<File> files) {
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
        return inflater.inflate(R.layout.search_fragment, container, false);
    }

    @Override
    public int getTheme() {
        return R.style.ThemeOverlay_Material3_BottomSheetDialog;
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
            if (active) {
                searchButton.setText("Search");
                progress.setVisibility(View.GONE);
                recyclerView.getAdapter().notifyDataSetChanged();
                active = false;
                setCancelable(true);
            } else {
                setCancelable(false);
                searchButton.setText("Stop");
                tab.getDataHolder().searchList.clear();
                recyclerView.getAdapter().notifyDataSetChanged();
                progress.setVisibility(View.VISIBLE);
                loseFocus(input);
                active = true;
                query = input.getEditText().getText().toString();
                searchThread = new Thread(() -> {
                    for (File file : filesToSearchIn) {
                        searchIn(file, deepSearch.isChecked(), regEx.isChecked(), prefix.isChecked(), suffix.isChecked());
                    }
                    recyclerView.post(() -> {
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

        if (tab.getDataHolder().searchList.size() > 0) {
            fileCount.setVisibility(View.VISIBLE);
            updateFileCount();
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateFileCount() {
        fileCount.setText(tab.getDataHolder().searchList.size() + " results found");
    }

    private void searchIn(File file, boolean isDeepSearch, boolean useRegex, boolean startWith, boolean endWith) {
        if (file.isFile()) {
            if (isDeepSearch) {
                if (useRegex) {
                    try {
                        if (Pattern.compile(query).matcher(FileUtil.readFile(file)).find())
                            addFileItem(file);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                } else {
                    try {
                        if (FileUtil.readFile(file).contains(query))
                            addFileItem(file);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
            } else {
                if (startWith) {
                    if (file.getName().startsWith(query))
                        addFileItem(file);
                } else if (endWith) {
                    if (file.getName().endsWith(query))
                        addFileItem(file);
                } else {
                    if (file.getName().contains(query))
                        addFileItem(file);
                }
            }
        } else {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    searchIn(child, isDeepSearch, useRegex, startWith, endWith);
                }
            }
        }
    }

    private void addFileItem(File file) {
        tab.getDataHolder().searchList.add(new FileItem(file));
        recyclerView.post(() -> {
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
            @SuppressLint("InflateParams") View _v = _inflater.inflate(R.layout.file_explorer_tab_fragment_file_item, null);
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
            return tab.getDataHolder().searchList.size();
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

            public void bind() {
                final int position = getAdapterPosition();
                final FileItem fileItem = tab.getDataHolder().searchList.get(position);

                name.setText(fileItem.file.getName());
                details.setText(FileUtil.getFileDetails(fileItem.file));

                FileUtil.setFileIcon(icon, fileItem.file);

                icon.setAlpha(fileItem.file.isHidden() ? 0.5f : 1f);

                background.setOnClickListener((v) -> {
                    tab.setCurrentDirectory(fileItem.file.getParentFile());
                    tab.refresh();
                    tab.focusOn(fileItem.file);
                    dismiss();
                });
            }
        }
    }
}