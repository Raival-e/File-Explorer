package com.raival.fileexplorer.tab.file.task

import android.os.Handler
import android.os.Looper
import com.android.apksigner.ApkSignerTool
import com.raival.fileexplorer.tab.file.model.Task
import com.raival.fileexplorer.tab.file.util.APKSignerUtils
import java.io.File
import java.util.*

class APKSignerTask(private val fileToSign: File) : Task() {
    private var activeDirectory: File? = null
    override val name: String
        get() = "APK Signer"
    override val details: String
        get() = fileToSign.absolutePath
    override val isValid: Boolean
        get() = fileToSign.exists()

    override fun setActiveDirectory(directory: File) {
        activeDirectory = directory
    }

    override fun start(onUpdate: OnUpdateListener, onFinish: OnFinishListener) {
        Thread {
            Handler(Looper.getMainLooper()).post { onUpdate.onUpdate("Signing....") }
            try {
                signAPK(fileToSign)
                Handler(Looper.getMainLooper()).post { onFinish.onFinish("APK signed successfully") }
            } catch (e: Exception) {
                e.printStackTrace()
                Handler(Looper.getMainLooper()).post { onFinish.onFinish(e.toString()) }
            }
        }.start()
    }

    @Throws(Exception::class)
    private fun signAPK(unsignedAPK: File) {
        val signedAPK = File(
            activeDirectory, unsignedAPK.name
                .lowercase(Locale.getDefault())
                .substring(0, unsignedAPK.name.lowercase(Locale.getDefault()).lastIndexOf(".apk"))
                    + "_signed.apk"
        )
        val args = ArrayList<String>().apply {
            add("sign")
            add("--in")
            add(unsignedAPK.absolutePath)
            add("--out")
            add(signedAPK.absolutePath)
            add("--key")
            add(APKSignerUtils.pk8.absolutePath)
            add("--cert")
            add(APKSignerUtils.pem.absolutePath)
        }
        ApkSignerTool.main(args.toTypedArray())
    }
}