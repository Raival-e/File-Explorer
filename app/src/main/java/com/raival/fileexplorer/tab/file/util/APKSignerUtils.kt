package com.raival.fileexplorer.tab.file.util

import com.raival.fileexplorer.App
import com.raival.fileexplorer.tab.file.util.BuildUtils.unzipFromAssets
import java.io.File
import java.util.*

object APKSignerUtils {
    val pk8: File
        get() {
            val check = File(App.appContext.filesDir.toString() + "/build/testkey.pk8")
            if (check.exists()) {
                return check
            }
            unzipFromAssets(
                App.appContext,
                "build/testkey.pk8.zip",
                Objects.requireNonNull(check.parentFile).absolutePath
            )
            return check
        }
    val pem: File
        get() {
            val check = File(App.appContext.filesDir.toString() + "/build/testkey.x509.pem")
            if (check.exists()) {
                return check
            }
            unzipFromAssets(
                App.appContext,
                "build/testkey.x509.pem.zip",
                Objects.requireNonNull(check.parentFile).absolutePath
            )
            return check
        }
}