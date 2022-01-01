package com.raival.quicktools;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import com.raival.quicktools.utils.FileUtil;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class App extends Application {
    public static Context appContext;
    public static volatile Handler appHandler;

    @Override
    public void onCreate() {
        Thread.setDefaultUncaughtExceptionHandler((thread, ex) -> {
            log(ex);
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(2);
        });
        super.onCreate();
        appContext = this;
        appHandler = new Handler(appContext.getMainLooper());
    }

    public static String getStackTrace(Throwable th) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        th.printStackTrace(printWriter);
        final String stacktraceAsString = result.toString();
        printWriter.close();
        return stacktraceAsString;
    }

    public static void showMsg(String s){
        Toast.makeText(appContext, s, Toast.LENGTH_SHORT).show();
    }

    public static void log(Throwable exception) {
        log(getStackTrace(exception));
    }

    public static void log(String exception) {
        final File logFile = new File(appContext.getExternalFilesDir(null).getAbsolutePath() + "/debug/log.txt");
        try {
            FileUtil.writeFile(logFile, exception);
        } catch (Exception ignored){

        }

    }
}
