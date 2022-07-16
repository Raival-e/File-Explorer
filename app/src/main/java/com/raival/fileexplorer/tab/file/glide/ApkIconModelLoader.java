package com.raival.fileexplorer.tab.file.glide;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.signature.ObjectKey;
import com.raival.fileexplorer.tab.file.util.FileExtensions;

import java.util.Locale;

public class ApkIconModelLoader implements ModelLoader<String, Drawable> {

    private final Context context;

    public ApkIconModelLoader(Context context) {
        this.context = context;
    }

    @Nullable
    @Override
    public LoadData<Drawable> buildLoadData(@NonNull String s, int width, int height, @NonNull Options options) {
        return new LoadData<>(new ObjectKey(s), new ApkIconDataFetcher(context, s));
    }

    @Override
    public boolean handles(String s) {
        return s.toLowerCase(Locale.ROOT).endsWith(FileExtensions.apkType);
    }
}
