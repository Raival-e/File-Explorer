package com.raival.fileexplorer.tab.file.executor;

import android.app.Activity;
import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import com.raival.fileexplorer.App;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Objects;

import dalvik.system.DexClassLoader;

public class ModuleRunner {
    private static final String TAG = "DexRunner";

    private final AppCompatActivity activity;
    private File directory;
    private final ArrayList<File> relatedDexFiles = new ArrayList<>();

    public ModuleRunner(File dexFile, AppCompatActivity activity) {
        this.activity = activity;
        this.directory = dexFile.getParentFile();

        assert directory != null;
        final File[] files = directory.listFiles();
        final String prefix = dexFile.getName().substring(0, dexFile.getName().indexOf(".extension"));
        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith(".extension")) {
                    if (file.getName().startsWith(prefix)) relatedDexFiles.add(file);
                }
            }
        }
    }

    public ModuleRunner setProjectDir(File file) {
        if (file.isDirectory()) directory = file;
        return this;
    }

    public void run() throws Exception {
        final String optimizedDir = App.appContext.getCodeCacheDir().getAbsolutePath();

        DexClassLoader dexClassLoader = new DexClassLoader(
                getDexFiles(),
                optimizedDir,
                null,
                App.appContext.getClassLoader());
        Class<?> clazz = dexClassLoader.loadClass("com.main.Main");

        // Look for a public static method called `main` and invoke it
        for (Method method : clazz.getMethods()) {
            if (method.getName().equals("main") && method.getModifiers() == Modifier.PUBLIC + Modifier.STATIC) {
                ArrayList<Object> params = new ArrayList<>();
                for (Object obj : method.getParameterTypes()) {
                    if (obj.equals(AppCompatActivity.class)) {
                        params.add(activity);
                    } else if (obj.equals(Activity.class)) {
                        params.add(activity);
                    } else if (obj.equals(Context.class)) {
                        params.add(activity);
                    } else if (obj.equals(File.class)) {
                        params.add(directory);
                    } else {
                        params.add(null);
                    }
                }
                method.invoke(null, params.toArray(new Object[0]));
                return;
            }
        }

        // if the specified method doesn't exist, create an instance of the Main class instead
        Constructor<?> method = clazz.getConstructors()[0];
        ArrayList<Object> params = new ArrayList<>();
        for (Object obj : method.getParameterTypes()) {
            if (obj.equals(AppCompatActivity.class)) {
                params.add(activity);
            } else if (obj.equals(Activity.class)) {
                params.add(activity);
            } else if (obj.equals(Context.class)) {
                params.add(activity);
            } else if (obj.equals(File.class)) {
                params.add(directory);
            } else {
                params.add(null);
            }
        }
        method.newInstance(params.toArray(new Object[0]));
    }

    private String getDexFiles() {
        final StringBuilder stringBuilder = new StringBuilder();
        for (File file : relatedDexFiles) {
            stringBuilder.append(":");
            stringBuilder.append(file.getAbsolutePath());
        }
        return stringBuilder.substring(1);
    }

    public ModuleRunner setLibsDir(File libs) {
        if (libs != null) {
            if (libs.isDirectory()) {
                for (File file : Objects.requireNonNull(libs.listFiles())) {
                    if (file.isFile()) {
                        if (file.getName().endsWith(".dex")) relatedDexFiles.add(file);
                    }
                }
            }
        }
        return this;
    }
}
