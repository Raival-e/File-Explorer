package com.raival.fileexplorer.tab.file.glide;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;

public class ApkIconModelLoaderFactory implements ModelLoaderFactory<String, Drawable> {

    private final Context context;

    public ApkIconModelLoaderFactory(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ModelLoader<String, Drawable> build(@NonNull MultiModelLoaderFactory multiFactory) {
        return new ApkIconModelLoader(context);
    }

    @Override
    public void teardown() {
    }
}