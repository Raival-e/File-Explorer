package com.raival.fileexplorer.tab.apps.adapter;

import android.annotation.SuppressLint;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.raival.fileexplorer.App;
import com.raival.fileexplorer.R;
import com.raival.fileexplorer.common.BackgroundTask;
import com.raival.fileexplorer.common.dialog.CustomDialog;
import com.raival.fileexplorer.tab.apps.AppsTabFragment;
import com.raival.fileexplorer.tab.apps.model.Apk;
import com.raival.fileexplorer.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.ViewHolder> {
    private final ArrayList<Apk> list;
    private final AppsTabFragment fragment;

    public AppListAdapter(ArrayList<Apk> list, AppsTabFragment fragment) {
        this.list = list;
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        @SuppressLint("InflateParams") View _v = fragment.getLayoutInflater().inflate(R.layout.apps_tab_app_item, null);
        _v.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return new ViewHolder(_v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name;
        TextView pkg;
        TextView details;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.app_icon);
            name = itemView.findViewById(R.id.app_name);
            pkg = itemView.findViewById(R.id.app_pkg);
            details = itemView.findViewById(R.id.app_details);
        }

        public void bind() {
            final int position = getAdapterPosition();
            final Apk apk = list.get(position);
            name.setText(apk.name);
            pkg.setText(apk.pkg);
            details.setText(apk.size);
            icon.setImageDrawable(apk.icon);

            itemView.findViewById(R.id.background).setOnClickListener((v -> showSaveDialog(apk)));
        }
    }

    private void showSaveDialog(Apk file) {
        new CustomDialog()
                .setIconDrawable(file.icon)
                .setTitle(file.name)
                .setMsg("Do you want to save this app to Download folder?")
                .setPositiveButton("Yes", view -> saveApkFile(file.source), true)
                .setNegativeButton("No", null, true)
                .show(fragment.getParentFragmentManager(), "");
    }

    private void saveApkFile(File file) {
        BackgroundTask backgroundTask = new BackgroundTask();

        backgroundTask.setTasks(() -> backgroundTask.showProgressDialog("Copying...", fragment.requireActivity()), () -> {
            try {
                FileUtils.copy(file, new File(Environment.getExternalStorageDirectory(), Environment.DIRECTORY_DOWNLOADS));
            } catch (IOException e) {
                e.printStackTrace();
                App.appHandler.post(() -> App.showMsg(e.toString()));
            }
        }, backgroundTask::dismiss);
        backgroundTask.run();
    }
}
