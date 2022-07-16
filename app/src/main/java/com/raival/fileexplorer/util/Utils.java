package com.raival.fileexplorer.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.util.TypedValue;

import androidx.annotation.ColorInt;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.raival.fileexplorer.App;
import com.raival.fileexplorer.activity.SettingsActivity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Random;

public class Utils {
    public final static String REGULAR_DATE_FORMAT = "MMM dd , hh:mm a";
    public final static String TAB = "  ";
    private static final String ALLOWED_CHARACTERS = "0123456789qwertyuiopasdfghjklzxcvbnm_";

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
        if (PrefsUtils.Settings.getThemeMode().equals(SettingsActivity.THEME_MODE_DARK)) {
            return true;
        } else if (PrefsUtils.Settings.getThemeMode().equals(SettingsActivity.THEME_MODE_LIGHT)) {
            return false;
        } else {
            return (App.appContext.getResources().getConfiguration().uiMode
                    & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        }
    }

    public static String getRandomString(final int sizeOfRandomString) {
        final Random random = new Random();
        final StringBuilder sb = new StringBuilder(sizeOfRandomString);
        for (int i = 0; i < sizeOfRandomString; ++i) {
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        }
        return sb.toString();
    }

    public static ArrayList<String> getStringList(String json) {
        return new Gson().fromJson(json, new TypeToken<ArrayList<String>>() {
        }.getType());
    }

    public static String surroundWithBrackets(String string) {
        return "[" + string + "]";
    }
}
