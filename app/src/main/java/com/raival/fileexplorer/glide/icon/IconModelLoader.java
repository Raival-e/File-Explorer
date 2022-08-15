package com.raival.fileexplorer.glide.icon;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.signature.ObjectKey;
import com.raival.fileexplorer.glide.model.IconRes;

public class IconModelLoader implements ModelLoader<IconRes, Drawable> {

    private final Context context;

    public IconModelLoader(Context context) {
        this.context = context;
    }

    @Nullable
    @Override
    public LoadData<Drawable> buildLoadData(@NonNull IconRes s, int width, int height, @NonNull Options options) {
        return new LoadData<>(new ObjectKey(s), new IconDataFetcher(context, s));
    }

    @Override
    public boolean handles(@NonNull IconRes s) {
        return true;
    }
}
