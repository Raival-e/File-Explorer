package com.raival.fileexplorer

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Handler
import android.os.Process
import android.widget.Toast
import com.pixplicity.easyprefs.library.Prefs
import com.raival.fileexplorer.util.Log
import kotlin.system.exitProcess

class App : Application() {
    override fun onCreate() {
        Thread.setDefaultUncaughtExceptionHandler { _: Thread?, throwable: Throwable? ->
            Log.e("AppCrash", "", throwable)
            Process.killProcess(Process.myPid())
            exitProcess(2)
        }

        super.onCreate()
        appContext = this
        appHandler = Handler(mainLooper)

        Prefs.Builder()
            .setContext(applicationContext)
            .setPrefsName("Prefs")
            .setMode(MODE_PRIVATE)
            .build()
        Log.start(appContext)
    }

    companion object {
        lateinit var appContext: Context

        @Volatile
        lateinit var appHandler: Handler

        @JvmStatic
        fun showMsg(message: String?) {
            Toast.makeText(appContext, message, Toast.LENGTH_SHORT).show()
        }

        @JvmStatic
        fun copyString(string: String?) {
            (appContext.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(
                ClipData.newPlainText("clipboard", string)
            )
        }
    }
}