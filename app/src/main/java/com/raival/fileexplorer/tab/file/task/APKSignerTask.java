package com.raival.fileexplorer.tab.file.task;

import android.os.Handler;
import android.os.Looper;

import com.android.apksigner.ApkSignerTool;
import com.raival.fileexplorer.tab.file.model.Task;
import com.raival.fileexplorer.tab.file.util.APKSignerUtils;

import java.io.File;
import java.util.ArrayList;

public class APKSignerTask extends Task {
    private final File fileToSign;
    private File activeDirectory;

    public APKSignerTask(File file) {
        this.fileToSign = file;
    }

    @Override
    public String getName() {
        return "APK Signer";
    }

    @Override
    public String getDetails() {
        return fileToSign.getAbsolutePath();
    }

    @Override
    public boolean isValid() {
        return fileToSign.exists();
    }

    @Override
    public void setActiveDirectory(File directory) {
        activeDirectory = directory;
    }

    @Override
    public void start(OnUpdateListener onUpdateListener, OnFinishListener onFinishListener) {
        new Thread(() -> {
            new Handler(Looper.getMainLooper()).post(() -> onUpdateListener.onUpdate("Signing...."));
            try {
                signAPK(fileToSign);
                new Handler(Looper.getMainLooper()).post(() -> onFinishListener.onFinish("APK signed successfully"));
            } catch (Exception e) {
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() -> onFinishListener.onFinish(e.toString()));
            }
        }).start();
    }

    private void signAPK(File unsignedAPK) throws Exception {
        File signedAPK = new File(activeDirectory,
                unsignedAPK.getName()
                        .toLowerCase()
                        .substring(0, unsignedAPK.getName().toLowerCase().lastIndexOf(".apk"))
                        + "_signed.apk");

        ArrayList<String> args = new ArrayList<>();
        args.add("sign");
        args.add("--in");
        args.add(unsignedAPK.getAbsolutePath());
        args.add("--out");
        args.add(signedAPK.getAbsolutePath());
        args.add("--key");
        args.add(APKSignerUtils.getPk8().getAbsolutePath());
        args.add("--cert");
        args.add(APKSignerUtils.getPem().getAbsolutePath());
        ApkSignerTool.main(args.toArray(new String[0]));
    }
}
