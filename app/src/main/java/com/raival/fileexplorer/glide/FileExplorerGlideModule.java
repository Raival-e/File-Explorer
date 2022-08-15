package com.raival.fileexplorer.glide;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import com.raival.fileexplorer.glide.apk.ApkIconModelLoaderFactory;
import com.raival.fileexplorer.glide.icon.IconModelLoaderFactory;
import com.raival.fileexplorer.glide.model.IconRes;

@GlideModule
public class FileExplorerGlideModule extends AppGlideModule {
    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, Registry registry) {
        registry.prepend(String.class, Drawable.class, new ApkIconModelLoaderFactory(context));
        registry.prepend(IconRes.class, Drawable.class, new IconModelLoaderFactory(context));
    }
}