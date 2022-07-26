package com.raival.fileexplorer.tab.file.util;

import static com.raival.fileexplorer.tab.file.util.BuildUtils.unzipFromAssets;

import com.raival.fileexplorer.App;

import java.io.File;
import java.util.Objects;

public class APKSignerUtils {
    public static File getPk8() {
        File check = new File(App.appContext.getFilesDir() + "/build/testkey.pk8");
        if (check.exists()) {
            return check;
        }
        unzipFromAssets(App.appContext, "build/testkey.pk8.zip", Objects.requireNonNull(check.getParentFile()).getAbsolutePath());
        return check;
    }

    public static File getPem() {
        File check = new File(App.appContext.getFilesDir() + "/build/testkey.x509.pem");
        if (check.exists()) {
            return check;
        }
        unzipFromAssets(App.appContext, "build/testkey.x509.pem.zip", Objects.requireNonNull(check.getParentFile()).getAbsolutePath());
        return check;
    }
}
