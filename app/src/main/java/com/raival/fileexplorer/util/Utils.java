package com.raival.fileexplorer.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.util.TypedValue;

import androidx.annotation.ColorInt;

import com.raival.fileexplorer.App;

import java.io.File;
import java.text.SimpleDateFormat;

public class Utils {
    public final static String REGULAR_DATE_FORMAT = "MMM dd , hh:mm a";

    @ColorInt
    public static int getColorAttribute(int id, Context context) {
        TypedValue out = new TypedValue();
        context.getTheme().resolveAttribute(id, out, true);
        return out.data;
    }

    public static float pxToDp(float val) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, val, App.appContext.getResources().getDisplayMetrics());
    }

    @SuppressLint("SimpleDateFormat")
    public static String getLastModifiedDate(File file) {
        return new SimpleDateFormat(REGULAR_DATE_FORMAT).format(file.lastModified());
    }

    public static boolean isDarkMode() {
        return (App.appContext.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }
}
