package com.raival.fileexplorer.utils;

import android.content.Context;
import android.util.TypedValue;

import androidx.annotation.ColorInt;

public class AndroidUtil {
    @ColorInt  public static int getColorAttribute(int id, Context context){
        TypedValue out = new TypedValue();
        context.getTheme().resolveAttribute(id, out, true);
        return out.data;
    }
}
