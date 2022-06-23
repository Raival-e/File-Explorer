package com.raival.fileexplorer.tabs.file.executor;

import android.app.Activity;
import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import com.raival.fileexplorer.App;
import com.raival.fileexplorer.utils.FileUtils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import dalvik.system.DexClassLoader;

public class DexRunner {
    private final AppCompatActivity activity;
    private final File directory;
    private final ArrayList<File> relatedDexFiles = new ArrayList<>();

    public DexRunner(File dexFile, File directory, AppCompatActivity activity){
        this.activity = activity;
        this.directory = directory;
        for(File file : directory.listFiles()){
            if(file.getName().endsWith(".exe.dex")){
                //TODO: only execute files with the same name as dexFile
                relatedDexFiles.add(file);
            }
        }
    }

    public void run(){
        final String optimizedDir = App.appContext.getCodeCacheDir().getAbsolutePath();

        DexClassLoader dexClassLoader = new DexClassLoader(
                getDexFiles(),
                optimizedDir,
                null,
                App.appContext.getClassLoader());
        Class<?> clazz = null;
        try {
            clazz = dexClassLoader.loadClass("com.main.Main");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            App.log(e);
            App.showMsg("Class com.main.Main is not found");
            return;
        }

        for (Method method : clazz.getMethods()) {
            if (method.getName().equals("main")) {
                ArrayList<Object> params = new ArrayList<>();
                for(Object obj : method.getParameterTypes()){
                    if(obj.equals(AppCompatActivity.class)){
                        params.add(activity);
                    } else if(obj.equals(Activity.class)){
                        params.add(activity);
                    } else if(obj.equals(Context.class)){
                        params.add(activity);
                    } else if(obj.equals(File.class)){
                        params.add(directory);
                    } else {
                        params.add(null);
                    }
                }
                try {
                    method.invoke(null, params.toArray(new Object[0]));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                    App.log(e);
                    App.log("Something went wrong, check logs for more details");
                }
            }
        }
    }

    private String getDexFiles(){
        final StringBuilder stringBuilder = new StringBuilder();
        for (File file : relatedDexFiles) {
            stringBuilder.append(":");
            stringBuilder.append(file.getAbsolutePath());
        }
        return stringBuilder.substring(1);
    }
}
