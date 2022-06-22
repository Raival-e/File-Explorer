package com.raival.fileexplorer.utils;

import static com.raival.fileexplorer.utils.D8Utils.unzipFromAssets;

import com.raival.fileexplorer.App;

import java.io.File;

public class APKSignerUtils {
    public static File getPk8() {
        File check = new File(App.appContext.getFilesDir() + "/build/testkey.pk8");
        if (check.exists()) {
            return check;
        }
        unzipFromAssets(App.appContext, "testkey.pk8.zip", check.getParentFile().getAbsolutePath());
        return check;
    }

    public static File getPem() {
        File check = new File(App.appContext.getFilesDir() + "/build/testkey.x509.pem");
        if (check.exists()) {
            return check;
        }
        unzipFromAssets(App.appContext, "testkey.x509.pem.zip", check.getParentFile().getAbsolutePath());
        return check;
    }
}
