package com.raival.fileexplorer.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.TypedValue;

import androidx.annotation.ColorInt;

import com.raival.fileexplorer.App;

import java.io.File;
import java.text.SimpleDateFormat;

public class Utils {
    @ColorInt  public static int getColorAttribute(int id, Context context){
        TypedValue out = new TypedValue();
        context.getTheme().resolveAttribute(id, out, true);
        return out.data;
    }

    public static float pxToDp(float val) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, val, App.appContext.getResources().getDisplayMetrics());
    }

    public final static String REGULAR_DATE_FORMAT = "MMM dd , hh:mm a";

    @SuppressLint("SimpleDateFormat")
    public static String getLastModifiedDate(File file, String dateFormat) {
        return new SimpleDateFormat(REGULAR_DATE_FORMAT).format(file.lastModified());
    }
}
