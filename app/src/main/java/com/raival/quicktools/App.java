package com.raival.quicktools;

import android.app.Application;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import com.raival.quicktools.utils.FileUtil;
import com.raival.quicktools.utils.ToastUtil;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class App extends Application {
    public static Context appContext;
    public static volatile Handler appHandler;

    @Override
    public void onCreate() {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            log(throwable);
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(2);
        });
        super.onCreate();
        appContext = this;
        appHandler = new Handler(appContext.getMainLooper());
    }

    public static String getStackTrace(Throwable throwable) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        throwable.printStackTrace(printWriter);
        final String stacktraceAsString = result.toString();
        printWriter.close();
        return stacktraceAsString;
    }

    public static void showMsg(String message){
        ToastUtil.makeToast(appContext, message, Toast.LENGTH_SHORT).show();
    }

    public static void showWarning(String message){
        ToastUtil.makeWarningToast(appContext, message, Toast.LENGTH_SHORT).show();
    }

    public static void copyString(String string){
        ((ClipboardManager)appContext.getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("clipboard", string));
    }

    public static void log(Throwable exception) {
        log(getStackTrace(exception));
    }

    public static void log(String exception) {
        final File logFile = new File(appContext.getExternalFilesDir(null).getAbsolutePath() + "/debug/log.txt");
        try {
            FileUtil.writeFile(logFile, exception);
        } catch (Exception e){
            showMsg(e.toString());
        }
    }
}
