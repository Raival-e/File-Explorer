package com.raival.fileexplorer.util;

import android.annotation.SuppressLint;
import android.content.Context;

import com.raival.fileexplorer.activity.SettingsActivity;
import com.raival.fileexplorer.tab.file.util.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;

public class Log {
    public final static String TAG = "CustomLog";
    // This is just a common phrase used in many classes
    public final static String UNABLE_TO = "Unable to";
    public final static String SOMETHING_WENT_WRONG = "Something went wrong while";

    public static File logFile;

    public static void start(Context context) {
        logFile = new File(context.getExternalFilesDir(null).getAbsolutePath() + "/debug/log.txt");
    }

    public static void e(String tag, String msg) {
        e(tag, msg, null);
    }

    public static void e(String tag, Exception e) {
        e(tag, "", e);
    }

    public static void e(String tag, String msg, Throwable throwable) {
        write(tag, "Error", msg, throwable);
    }

    public static void w(String tag, String msg) {
        w(tag, msg, null);
    }

    public static void w(String tag, Exception e) {
        w(tag, "", e);
    }

    public static void w(String tag, String msg, Throwable throwable) {
        write(tag, "Warning", msg, throwable);
    }

    public static void d(String tag, String msg) {
        d(tag, msg, null);
    }

    public static void d(String tag, Exception e) {
        d(tag, "", e);
    }

    public static void d(String tag, String msg, Throwable throwable) {
        write(tag, "Warning", msg, throwable);
    }

    public static void i(String tag, String msg) {
        i(tag, msg, null);
    }

    public static void i(String tag, Exception e) {
        i(tag, "", e);
    }

    public static void i(String tag, String msg, Throwable throwable) {
        write(tag, "Info", msg, throwable);
    }

    @SuppressLint("SimpleDateFormat")
    private static String getCurrentTime() {
        return new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS").format(System.currentTimeMillis());
    }

    public static String getStackTrace(Throwable throwable) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        throwable.printStackTrace(printWriter);
        final String stacktraceAsString = result.toString();
        printWriter.close();
        return stacktraceAsString;
    }

    private static void write(String tag, String priority, String msg, Throwable throwable) {
        if (!checkPrefs(tag)) return;

        if (logFile == null) return;

        if (!logFile.getParentFile().exists() && !logFile.getParentFile().mkdirs()) {
            android.util.Log.e(TAG, Log.UNABLE_TO + " " + FileUtils.CREATE_FILE + ": " + logFile.getParentFile());
            return;
        }

        try {
            if (!logFile.exists() && !logFile.createNewFile()) {
                android.util.Log.e(TAG, Log.UNABLE_TO + " " + FileUtils.CREATE_FILE + ": " + logFile);
                return;
            }

            final StringBuilder logToWrite = new StringBuilder();
            if (logFile.length() > 0) logToWrite.append(System.lineSeparator());
            logToWrite.append(Utils.surroundWithBrackets(getCurrentTime()))
                    .append(Utils.surroundWithBrackets(priority))
                    .append(Utils.surroundWithBrackets(tag))
                    .append(":").append(Utils.TAB);
            if (!msg.isEmpty()) {
                logToWrite.append(msg);
            }
            if (throwable != null) {
                logToWrite.append(System.lineSeparator()).append(getStackTrace(throwable));
            }

            FileWriter fileWriter = new FileWriter(logFile, true);
            fileWriter.write(logToWrite.toString());
            fileWriter.flush();
            fileWriter.close();
        } catch (Exception e) {
            android.util.Log.e(TAG, Log.UNABLE_TO + " write to log file" + System.lineSeparator() + e);
            android.util.Log.e(tag, msg, throwable);
        }
    }

    private static boolean checkPrefs(String tag) {
        if (PrefsUtils.Settings.getLogMode().equals(SettingsActivity.LOG_MODE_ALL)) return true;
        return PrefsUtils.Settings.getLogMode().equals(SettingsActivity.LOG_MODE_ERRORS_ONLY) && tag.equals("Error");
    }
}
