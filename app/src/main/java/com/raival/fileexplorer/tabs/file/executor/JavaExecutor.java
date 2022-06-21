package com.raival.fileexplorer.tabs.file.executor;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import com.android.tools.r8.D8;
import com.raival.fileexplorer.App;
import com.raival.fileexplorer.utils.D8Util;
import com.raival.fileexplorer.utils.FileUtil;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;

import dalvik.system.DexClassLoader;

public class JavaExecutor {
    private File project;
    private File output;

    private final ArrayList<File> javaFiles = new ArrayList<>();
    private final ArrayList<File> dexFiles = new ArrayList<>();
    private final ArrayList<File> jarFiles = new ArrayList<>();

    private boolean isValidProject = false;

    private AppCompatActivity activity;

    public JavaExecutor(File folder, AppCompatActivity activity) {
        if (folder.isFile()) {
            isValidProject = false;
            return;
        }
        this.activity = activity;
        project = folder;
        parseInputFolder(project);
    }

    public void execute() throws Exception {
        if (!clearOutput()) {
            throw new Exception("Failed cleaning output folder");
        }
        runECJ();
        runD8();
    }

    public void invoke() throws Exception {
        final String optimizedDir = App.appContext.getCodeCacheDir().getAbsolutePath();
        DexClassLoader dexClassLoader = new DexClassLoader(
                getDexFiles(),
                optimizedDir,
                null,
                App.appContext.getClassLoader());
        Class<?> clazz = dexClassLoader.loadClass("com.main.Main");
        for(Method method : clazz.getMethods()){
            if(method.getName().equals("main")){
                if(method.getParameterTypes().length == 2){
                    method.invoke(null, activity, project);
                } else if(method.getParameterTypes().length == 3){
                    method.invoke(null, activity, activity, project);
                }
            }
        }
    }

    private String getDexFiles() {
        ArrayList<File> list = new ArrayList<>(dexFiles);
        for (File file : output.listFiles()) {
            if (file.getName().endsWith(".dex"))
                list.add(file);
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (File file : list) {
            stringBuilder.append(":");
            stringBuilder.append(file.getAbsolutePath());
        }
        return stringBuilder.substring(1);
    }

    private void runD8() throws Exception {
        ArrayList<String> opt = new ArrayList<>();

        opt.add("--intermediate");
        opt.add("--lib");
        opt.add(D8Util.getBootstrapJarFile().getAbsolutePath());
        opt.add("--output");
        opt.add(output.getAbsolutePath());
        ArrayList<String> classes = FileUtil.getAllFilesInDir(new File(output, "classes"), "class");
        if (classes != null && classes.size() > 0)
            opt.addAll(classes);

        D8.main(opt.toArray(new String[0]));
    }

    private void runECJ() throws Exception {
        ArrayList<String> opt = new ArrayList<>();
        final File classes = new File(output, "classes");
        classes.mkdir();

        opt.add("-11");
        opt.add("-nowarn");
        opt.add("-deprecation");
        opt.add("-d");
        opt.add(classes.getAbsolutePath());
        opt.add("-cp");

        final StringBuilder sb = new StringBuilder();
        for (File jar : jarFiles)
            sb.append(":").append(jar.getAbsolutePath());

        sb.append(":")
                .append(D8Util.getLambdaStubsJarFile().getAbsolutePath())
                .append(":")
                .append(D8Util.getBootstrapJarFile().getAbsolutePath());
        opt.add(sb.substring(1));

        opt.add("-proc:none");
        opt.add("-sourcepath");
        opt.add(" ");
        for (File file : javaFiles)
            opt.add(file.getAbsolutePath());

        PrintWriter printWriter = new PrintWriter(new OutputStream() {
            @Override
            public void write(int i) {
            }
        });
        final StringBuilder errors = new StringBuilder();
        PrintWriter printWriter1 = new PrintWriter(new OutputStream() {
            @Override
            public void write(int i) {
                errors.append((char) i);
            }
        });

        org.eclipse.jdt.internal.compiler.batch.Main main = new org.eclipse.jdt.internal.compiler.batch.Main(
                printWriter,
                printWriter1,
                false,
                null,
                null);
        main.compile(opt.toArray(new String[0]));

        if (main.globalErrorsCount > 0) {
            throw new Exception(errors.toString());
        }
    }

    private boolean clearOutput() {
        if (output == null) {
            output = new File(project, "output");
            return output.mkdir();
        }

        FileUtil.deleteFile(output);
        return output.mkdir();
    }

    private void parseInputFolder(File input) {
        for (File file : input.listFiles()) {
            if (file.getName().toLowerCase().endsWith(".dex")) {
                dexFiles.add(file);
            } else if (file.getName().toLowerCase().endsWith(".jar")) {
                jarFiles.add(file);
            } else if (file.getName().toLowerCase().endsWith(".java")) {
                javaFiles.add(file);
            } else if (file.isDirectory() && file.getName().equals("output")) {
                output = file;
            }
        }
    }
}
