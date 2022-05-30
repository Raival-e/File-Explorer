package com.raival.quicktools.tasks;

import com.raival.quicktools.interfaces.QTask;
import com.raival.quicktools.interfaces.RegularTask;

import java.io.File;
import java.util.ArrayList;

public class APKSignerTask implements QTask, RegularTask {
    File getFileToSign;

    public APKSignerTask(File file) {
        this.getFileToSign = file;
    }

    public File getFileToSign() {
        return getFileToSign;
    }

    @Override
    public String getName() {
        return "APK Signer";
    }

    @Override
    public String getDetails() {
        return getFileToSign.getAbsolutePath();
    }

    @Override
    public ArrayList<File> getFilesList() {
        return new ArrayList<File>() {{
            add(getFileToSign);
        }};
    }
}
