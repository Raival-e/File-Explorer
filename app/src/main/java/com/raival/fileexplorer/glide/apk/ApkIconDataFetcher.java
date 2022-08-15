package com.raival.fileexplorer.glide.apk;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;
import com.raival.fileexplorer.R;
import com.raival.fileexplorer.tab.file.misc.FileUtils;

import java.io.File;

public class ApkIconDataFetcher implements DataFetcher<Drawable> {

    private final Context context;
    private final String model;

    public ApkIconDataFetcher(Context context, String model) {
        this.context = context;
        this.model = model;
    }

    @Override
    public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super Drawable> callback) {
        Drawable apkIcon = FileUtils.INSTANCE.getApkIcon(new File(model));
        if (apkIcon == null)
            apkIcon = ContextCompat.getDrawable(context, R.drawable.unknown_file_extension);
        callback.onDataReady(apkIcon);
    }

    @Override
    public void cleanup() {
        // Intentionally empty only because we're not opening an InputStream or another I/O resource!
    }

    @Override
    public void cancel() {
        // No cancellation procedure
    }

    @NonNull
    @Override
    public Class<Drawable> getDataClass() {
        return Drawable.class;
    }

    @NonNull
    @Override
    public DataSource getDataSource() {
        return DataSource.LOCAL;
    }
}