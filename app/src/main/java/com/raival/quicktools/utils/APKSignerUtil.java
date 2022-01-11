package com.raival.quicktools.utils;

import static com.raival.quicktools.utils.D8Util.unzipFromAssets;

import com.raival.quicktools.App;

import java.io.File;

public class APKSignerUtil {
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
