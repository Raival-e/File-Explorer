package com.raival.quicktools.exe.java;

import android.content.Context;

import com.android.tools.r8.D8;
import com.raival.quicktools.App;
import com.raival.quicktools.utils.D8Util;
import com.raival.quicktools.utils.FileUtil;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

import dalvik.system.DexClassLoader;

public class JavaExecutor {
    File project;
    File output;

    ArrayList<File> javaFiles = new ArrayList<>();
    ArrayList<File> dexFiles = new ArrayList<>();
    ArrayList<File> jarFiles = new ArrayList<>();

    boolean isValidProject = false;

    public JavaExecutor(File folder) {
        if(folder.isFile()){
            isValidProject = false;
            return;
        }
        project = folder;
        parseInputFolder(project);
    }

    public void execute() throws Exception{
        if(!clearOutput()){
            throw new Exception("Failed cleaning output folder");
        }
        runECJ();
        runD8();
    }

    public void invoke() throws Exception{
        final String optimizedDir = App.appContext.getCodeCacheDir().getAbsolutePath();
        DexClassLoader dexClassLoader = new DexClassLoader(
                getDexFiles(),
                optimizedDir,
                null,
                App.appContext.getClassLoader());
        Class clazz = dexClassLoader.loadClass("com.main.Main");
        java.lang.reflect.Method method = clazz.getDeclaredMethod("main", Context.class);
        method.invoke(null, App.appContext);
    }

    private String getDexFiles() {
        ArrayList<File> list = new ArrayList<>(dexFiles);
        for(File file : output.listFiles()){
            if(file.getName().endsWith(".dex"))
                list.add(file);
        }
        StringBuilder stringBuilder = new StringBuilder();
        for(File file : list){
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
        if(classes != null && classes.size() > 0)
            opt.addAll(classes);
        if(jarFiles.size() > 0)
            for (File jar : jarFiles)
                opt.add(jar.getAbsolutePath());

        D8.main(opt.toArray(new String[0]));
    }

    private void runECJ() throws Exception{
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
        for(File jar : jarFiles)
      	sb.append(":" + jar.getAbsolutePath());
        sb.add(":" + D8Util.getLambdaStubsJarFile().getAbsolutePath()
                    + ":"
                    + D8Util.getBootstrapJarFile().getAbsolutePath());

        opt.add(sb.substring(1));

        opt.add("-proc:none");
        opt.add("-sourcepath");
        opt.add(" ");
        for(File file : javaFiles)
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
                errors.append((char)i);
            }
        });

        org.eclipse.jdt.internal.compiler.batch.Main main = new org.eclipse.jdt.internal.compiler.batch.Main(
                printWriter,
                printWriter1,
                false,
                null,
                null);
        main.compile(opt.toArray(new String[0]));

        if(main.globalErrorsCount > 0){
            throw new Exception(errors.toString());
        }
    }

    private boolean clearOutput() {
        if(output == null){
            output = new File(project, "output");
            return output.mkdir();
        }

        FileUtil.deleteFile(output);
        return output.mkdir();
    }

    private void parseInputFolder(File input) {
        for(File file : input.listFiles()){
            if(file.getName().toLowerCase().endsWith(".dex")){
                dexFiles.add(file);
            } else if(file.getName().toLowerCase().endsWith(".jar")){
                jarFiles.add(file);
            } else if(file.getName().toLowerCase().endsWith(".java")){
                javaFiles.add(file);
            } else if(file.isDirectory() && file.getName().equals("output")){
                output = file;
            }
        }
    }
}
