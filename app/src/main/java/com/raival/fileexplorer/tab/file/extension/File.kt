package com.raival.fileexplorer.tab.file.extension

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
import com.raival.fileexplorer.tab.file.util.FileExtensions
import com.raival.fileexplorer.tab.file.util.FileUtils
import java.io.File
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.log10
import kotlin.math.pow

fun File.getFileDetails(): String {
    val sb = StringBuilder()
    sb.append(getLastModifiedDate())
    sb.append("  |  ")
    if (this.isFile) {
        sb.append(getFormattedFileSize())
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

fun File.isImageFile(): Boolean {
    for (extension in FileExtensions.imageType) {
        if (extension == getFileExtension()) return true
    }
    return false
}

fun File.isAudioFile(): Boolean {
    for (ext in FileExtensions.audioType) {
        if (getFileExtension() == ext) return true
    }
    return false
}

fun File.isVideoFile(): Boolean {
    for (extension in FileExtensions.videoType) {
        if (extension == getFileExtension()) return true
    }
    return false
}

fun File.isArchiveFile(): Boolean {
    for (extension in FileExtensions.archiveType) {
        if (extension == getFileExtension()) return true
    }
    return false
}

fun File.isCodeFile(): Boolean {
    for (extension in FileExtensions.codeType) {
        if (extension == getFileExtension()) return true
    }
    return false
}

fun File.isTextFile(): Boolean {
    for (extension in FileExtensions.textType) {
        if (extension == getFileExtension()) return true
    }
    return false
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

fun File.getFormattedSize(): String {
    return getFormattedSize("%.02f")
}

fun File.getFormattedSize(format: String): String {
    val length = length()
    if (length > 1073741824) return String.format(
        Locale.ENGLISH,
        format,
        length.toFloat() / 1073741824
    ) + "GB"
    if (length > 1048576) return String.format(
        Locale.ENGLISH,
        format,
        length.toFloat() / 1048576
    ) + "MB"
    return if (length > 1024) String.format(
        Locale.ENGLISH,
        format,
        length.toFloat() / 1024
    ) + "KB" else length.toString() + "B"
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

fun File.getFileExtension(): String {
    if (this.isDirectory) return ""
    val name = this.name
    val last = name.lastIndexOf(".")
    return if (!name.contains(".") || last == -1) {
        ""
    } else name.substring(last + 1)
}

fun File.getMimeTypeFromFile(): String {
    return MimeTypeMap.getSingleton().getMimeTypeFromExtension(getFileExtension())!!
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

fun File.getFormattedFileSize(): String {
    val size = if (isFile) length() else getFolderSize()
    if (size <= 0) return "0 B"
    val units = arrayOf("B", "kB", "MB", "GB", "TB")
    val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
    return DecimalFormat("#,##0.#").format(
        size / 1024.0.pow(digitGroups.toDouble())
    ) + " " + units[digitGroups]
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

fun File.getNameWithoutExtension(): String {
    return name.substring(0, name.length - getFileExtension().length - 1)
}

@SuppressLint("SimpleDateFormat")
fun File.getLastModifiedDate(REGULAR_DATE_FORMAT: String = "MMM dd , hh:mm a"): String {
    return SimpleDateFormat(REGULAR_DATE_FORMAT).format(lastModified())
}