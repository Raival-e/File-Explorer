package com.raival.fileexplorer.tabs.file.tasks;

import android.os.Handler;
import android.os.Looper;

import com.android.tools.r8.CompilationMode;
import com.android.tools.r8.D8;
import com.android.tools.r8.D8Command;
import com.android.tools.r8.OutputMode;
import com.raival.fileexplorer.tabs.file.d8.DexDiagnosticHandler;
import com.raival.fileexplorer.tabs.file.model.Task;
import com.raival.fileexplorer.utils.D8Util;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Jar2DexTask extends Task {
    private final File fileToConvert;
    private File activeDirectory;

    public Jar2DexTask(File file) {
        this.fileToConvert = file;
    }

    public File getFileToConvert() {
        return fileToConvert;
    }

    @Override
    public String getName() {
        return "Jar2Dex";
    }

    @Override
    public String getDetails() {
        return fileToConvert.getAbsolutePath();
    }

    @Override
    public boolean isValid() {
        return fileToConvert.exists();
    }

    @Override
    public void setActiveDirectory(File directory) {
        activeDirectory = directory;
    }

    @Override
    public void start(OnUpdateListener onUpdateListener, OnFinishListener onFinishListener) {
        new Thread(() -> {
            new Handler(Looper.getMainLooper()).post(() -> onUpdateListener.onUpdate("Converting...."));
            try {
                runD8(fileToConvert, activeDirectory);
                new Handler(Looper.getMainLooper()).post(() -> onFinishListener.onFinish("File has been converted successfully"));
            } catch (Exception e) {
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() -> onFinishListener.onFinish(e.toString()));
            }
        }).start();
    }

    private void runD8(File file, File currentPath) throws Exception {
        List<Path> path = new ArrayList<>();
        path.add(D8Util.getLambdaStubsJarFile().toPath());
        path.add(D8Util.getBootstrapJarFile().toPath());

        D8Command command = D8Command.builder(new DexDiagnosticHandler())
                .addLibraryFiles(path)
                .addProgramFiles(file.toPath())
                .setMode(CompilationMode.RELEASE)
                .setOutput(currentPath.toPath(), OutputMode.DexIndexed)
                .build();
        D8.run(command);
    }
}
