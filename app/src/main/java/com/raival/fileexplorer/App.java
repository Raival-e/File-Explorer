package com.raival.fileexplorer;

import android.app.Application;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Handler;
import android.widget.Toast;

import com.pixplicity.easyprefs.library.Prefs;
import com.raival.fileexplorer.util.Log;

public class App extends Application {
    public static Context appContext;
    public static volatile Handler appHandler;

    public static void showMsg(String message) {
        Toast.makeText(appContext, message, Toast.LENGTH_SHORT).show();
    }

    public static void copyString(String string) {
        ((ClipboardManager) appContext.getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("clipboard", string));
    }

    @Override
    public void onCreate() {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Log.e("AppCrash", "", throwable);
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(2);
        });
        super.onCreate();
        appContext = this;
        appHandler = new Handler(appContext.getMainLooper());

        new Prefs.Builder()
                .setContext(getApplicationContext())
                .setPrefsName("Prefs")
                .setMode(ContextWrapper.MODE_PRIVATE)
                .build();

        Log.start(appContext);
    }
}
