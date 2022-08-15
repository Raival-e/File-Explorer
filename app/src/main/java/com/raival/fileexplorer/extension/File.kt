package com.raival.fileexplorer.extension

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.os.StatFs
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import com.raival.fileexplorer.App
import com.raival.fileexplorer.App.Companion.showMsg
import com.raival.fileexplorer.tab.file.misc.FileMimeTypes
import com.raival.fileexplorer.tab.file.misc.FileUtils
import java.io.File
import java.text.SimpleDateFormat

fun File.getFileDetails(): String {
    val sb = StringBuilder()
    sb.append(getLastModifiedDate())
    sb.append("  |  ")
    if (this.isFile) {
        sb.append(length().toFormattedSize())
    } else {
        sb.append(getFormattedFileCount())
    }
    return sb.toString()
}

fun File.getShortLabel(maxLength: Int): String {
    var name = Uri.parse(this.absolutePath).lastPathSegment
    if (isExternalStorageFolder()) {
        name = FileUtils.INTERNAL_STORAGE
    }
    if (name!!.length > maxLength) {
        name = name.substring(0, maxLength - 3) + "..."
    }
    return name
}

fun File.isExternalStorageFolder(): Boolean {
    return this.absolutePath == Environment.getExternalStorageDirectory().absolutePath
}

fun File.getAvailableMemoryBytes(): Long {
    val statFs = StatFs(this.absolutePath)
    return statFs.blockSizeLong * statFs.availableBlocksLong
}

fun File.getTotalMemoryBytes(): Long {
    val statFs = StatFs(this.absolutePath)
    return statFs.blockSizeLong * statFs.blockCountLong
}

fun File.getUsedMemoryBytes(): Long {
    return getTotalMemoryBytes() - getAvailableMemoryBytes()
}

fun File.getFormattedFileCount(): String {
    val noItemsString = "Empty folder"
    if (this.isFile) {
        return noItemsString
    }
    var files = 0
    var folders = 0
    val fileList = this.listFiles() ?: return noItemsString
    for (item in fileList) {
        if (item.isFile) files++ else folders++
    }
    val sb = java.lang.StringBuilder()
    if (folders > 0) {
        sb.append(folders)
        sb.append(" folder")
        if (folders > 1) sb.append("s")
        if (files > 0) sb.append(", ")
    }
    if (files > 0) {
        sb.append(files)
        sb.append(" file")
        if (files > 1) sb.append("s")
    }
    return if (folders == 0 && files == 0) noItemsString else sb.toString()
}

fun File.getMimeTypeFromFile(): String {
    return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        ?: getMimeTypeFromExtension()
}

fun File.getMimeTypeFromExtension(): String {
    val type = FileMimeTypes.mimeTypes[extension]
    return type ?: FileMimeTypes.default
}

fun File.openFileWith(anonymous: Boolean) {
    val i = Intent(Intent.ACTION_VIEW)
    val uri = FileProvider.getUriForFile(
        App.appContext, App.appContext.packageName + ".provider",
        this
    )
    i.setDataAndType(uri, if (anonymous) "*/*" else getMimeTypeFromFile())
    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    try {
        App.appContext.startActivity(i)
    } catch (e: ActivityNotFoundException) {
        if (!anonymous) {
            openFileWith(true)
        } else {
            showMsg("Couldn't find any app that can open this type of files")
        }
    } catch (e: Exception) {
        //Log.i(TAG, e);
        showMsg("Failed to open this file")
    }
}

fun File.getFolderSize(): Long {
    var size: Long = 0
    val list = listFiles()
    if (list != null) {
        for (child in list) {
            size = if (child.isFile) {
                size + child.length()
            } else {
                size + child.getFolderSize()
            }
        }
    }
    return size
}

fun File.getAllFilesInDir(extension: String): ArrayList<String> {
    if (!exists() || isFile) {
        return ArrayList()
    }
    val list = arrayListOf<String>()
    val content = listFiles()
    if (content != null) {
        for (file in content) {
            if (file.isFile && file.name.endsWith(".$extension")) {
                list.add(file.absolutePath)
            } else {
                list.addAll(file.getAllFilesInDir(extension))
            }
        }
    }
    return list
}

@SuppressLint("SimpleDateFormat")
fun File.getLastModifiedDate(REGULAR_DATE_FORMAT: String = "MMM dd , hh:mm a"): String {
    return SimpleDateFormat(REGULAR_DATE_FORMAT).format(lastModified())
}