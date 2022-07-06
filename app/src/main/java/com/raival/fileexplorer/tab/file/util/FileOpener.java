package com.raival.fileexplorer.tab.file.util;

import android.content.Intent;

import com.raival.fileexplorer.App;
import com.raival.fileexplorer.activity.MainActivity;
import com.raival.fileexplorer.activity.TextEditorActivity;
import com.raival.fileexplorer.tab.file.executor.ModuleRunner;
import com.raival.fileexplorer.util.Log;

import java.io.File;

public class FileOpener {
    private static final String TAG = FileOpener.class.getSimpleName();

    private final MainActivity mainActivity;

    public FileOpener(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void openFile(File file) {
        if (!handleKnownFileExtensions(file)) {
            FileUtils.openFileWith(file, false);
        }
    }

    private boolean handleKnownFileExtensions(File file) {
        if (FileUtils.isTextFile(file) || FileUtils.isCodeFile(file)) {
            Intent intent = new Intent();
            intent.setClass(mainActivity, TextEditorActivity.class);
            intent.putExtra("file", file.getAbsolutePath());
            mainActivity.startActivity(intent);
            return true;
        }
        if (file.getName().toLowerCase().endsWith(".extension")) {
            try {
                new ModuleRunner(file, mainActivity).run();
            } catch (Exception e) {
                Log.e(TAG, e);
                App.showMsg("Something went wrong, check logs for more details");
            }
            return true;
        }
        return false;
    }
}
