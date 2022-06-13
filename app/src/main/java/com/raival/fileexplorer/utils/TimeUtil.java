package com.raival.fileexplorer.utils;

import android.annotation.SuppressLint;

import java.io.File;
import java.text.SimpleDateFormat;

public class TimeUtil {
    public final static String REGULAR_DATE_FORMAT = "MMM dd , hh:mm a";

    @SuppressLint("SimpleDateFormat")
    public static String getLastModifiedDate(File file, String dateFormat) {
        return new SimpleDateFormat(REGULAR_DATE_FORMAT).format(file.lastModified());
    }
}
