package com.raival.fileexplorer.activity.adapter;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.raival.fileexplorer.App;
import com.raival.fileexplorer.R;
import com.raival.fileexplorer.activity.MainActivity;
import com.raival.fileexplorer.util.PrefsUtils;
import com.raival.fileexplorer.util.Utils;

import java.io.File;
import java.util.ArrayList;

public class BookmarksAdapter extends RecyclerView.Adapter<BookmarksAdapter.ViewHolder> {
    private final MainActivity activity;
    private ArrayList<String> list;

    public BookmarksAdapter(MainActivity activity) {
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        @SuppressLint("InflateParams") View _v = activity.getLayoutInflater().inflate(R.layout.activity_main_drawer_bookmark_item, null);
        _v.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return new BookmarksAdapter.ViewHolder(_v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind();
    }

    @Override
    public int getItemCount() {
        return (list = PrefsUtils.getFileExplorerTabBookmarks()).size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView details;
        ImageView icon;
        View background;

        public ViewHolder(@NonNull View v) {
            super(v);
            name = v.findViewById(R.id.name);
            details = v.findViewById(R.id.details);
            icon = v.findViewById(R.id.icon);
            background = v.findViewById(R.id.background);
        }

        @SuppressLint("NotifyDataSetChanged")
        public void bind() {
            final int position = getAdapterPosition();
            final File file = new File(list.get(position));

            name.setText(file.getName());

            if (!file.exists()) {
                name.setTextColor(Color.RED);
                details.setTextColor(Color.RED);

                background.setOnClickListener((v -> {
                    App.showMsg("This folder doesn't exist anymore");
                    list.remove(file.getAbsolutePath());
                    PrefsUtils.setFileExplorerTabBookmarks(list);
                    list.clear();
                    notifyDataSetChanged();
                }));
            } else {
                name.setTextColor(Utils.getColorAttribute(R.attr.colorOnSurface, activity));
                details.setTextColor(Utils.getColorAttribute(R.attr.colorOnSurface, activity));

                background.setOnClickListener((v -> {
                    activity.onBookmarkSelected(file);
                }));
            }
            details.setText(file.getAbsolutePath());
            icon.setImageResource(R.drawable.ic_baseline_folder_24);

            background.setOnLongClickListener((v -> {
                list.remove(file.getAbsolutePath());
                PrefsUtils.setFileExplorerTabBookmarks(list);
                list.clear();
                notifyDataSetChanged();
                return true;
            }));
        }
    }
}
