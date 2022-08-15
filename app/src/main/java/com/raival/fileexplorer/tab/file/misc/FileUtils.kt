package com.raival.fileexplorer.tab.file.misc

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageInfo
import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.webkit.MimeTypeMap
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.material.textfield.TextInputLayout
import com.raival.fileexplorer.App
import com.raival.fileexplorer.App.Companion.showMsg
import com.raival.fileexplorer.R
import com.raival.fileexplorer.util.Log
import com.raival.fileexplorer.util.PrefsUtils
import com.raival.fileexplorer.util.PrefsUtils.FileExplorerTab.listFoldersFirst
import com.raival.fileexplorer.util.PrefsUtils.FileExplorerTab.sortingMethod
import java.io.*
import java.util.*


object FileUtils {
    const val INTERNAL_STORAGE = "Internal Storage"
    const val CREATE_FILE = "create file"

    private fun sortFoldersFirst(): Comparator<File> {
        return Comparator { file1: File, file2: File ->
            if (file1.isDirectory && !file2.isDirectory) {
                return@Comparator -1
            } else if (!file1.isDirectory && file2.isDirectory) {
                return@Comparator 1
            } else {
                return@Comparator 0
            }
        }
    }

    private fun sortFilesFirst(): Comparator<File> {
        return Comparator { file2: File, file1: File ->
            if (file1.isDirectory && !file2.isDirectory) {
                return@Comparator -1
            } else if (!file1.isDirectory && file2.isDirectory) {
                return@Comparator 1
            } else {
                return@Comparator 0
            }
        }
    }

    val comparators: ArrayList<Comparator<File>>
        get() {
            val list = ArrayList<Comparator<File>>()
            when (sortingMethod) {
                PrefsUtils.SORT_NAME_A2Z -> {
                    list.add(sortNameAsc())
                }
                PrefsUtils.SORT_NAME_Z2A -> {
                    list.add(sortNameDesc())
                }
                PrefsUtils.SORT_SIZE_SMALLER -> {
                    list.add(sortSizeAsc())
                }
                PrefsUtils.SORT_SIZE_BIGGER -> {
                    list.add(sortSizeDesc())
                }
                PrefsUtils.SORT_DATE_NEWER -> {
                    list.add(sortDateDesc())
                }
                PrefsUtils.SORT_DATE_OLDER -> {
                    list.add(sortDateAsc())
                }
            }
            if (listFoldersFirst()) {
                list.add(sortFoldersFirst())
            } else {
                list.add(sortFilesFirst())
            }
            return list
        }

    private fun sortDateAsc(): Comparator<File> {
        return Comparator.comparingLong { obj: File -> obj.lastModified() }
    }

    private fun sortDateDesc(): Comparator<File> {
        return Comparator { file1: File, file2: File ->
            file2.lastModified().compareTo(file1.lastModified())
        }
    }

    private fun sortNameAsc(): Comparator<File> {
        return Comparator.comparing { file: File -> file.name.lowercase(Locale.getDefault()) }
    }

    private fun sortNameDesc(): Comparator<File> {
        return Comparator { file1: File, file2: File ->
            file2.name.lowercase(Locale.getDefault()).compareTo(
                file1.name.lowercase(
                    Locale.getDefault()
                )
            )
        }
    }

    private fun sortSizeAsc(): Comparator<File> {
        return Comparator.comparingLong { obj: File -> obj.length() }
    }

    private fun sortSizeDesc(): Comparator<File> {
        return Comparator { file1: File, file2: File ->
            file2.length().compareTo(file1.length())
        }
    }

    fun isSingleFolder(selectedFiles: ArrayList<File>): Boolean {
        return selectedFiles.size == 1 && !selectedFiles[0].isFile
    }

    fun isSingleFile(selectedFiles: ArrayList<File>): Boolean {
        return selectedFiles.size == 1 && selectedFiles[0].isFile
    }

    fun isOnlyFiles(selectedFiles: ArrayList<File>): Boolean {
        for (file in selectedFiles) {
            if (!file.isFile) return false
        }
        return true
    }

    fun isArchiveFiles(selectedFiles: ArrayList<File>): Boolean {
        for (file in selectedFiles) {
            if (!FileMimeTypes.archiveType.contains(file.extension.lowercase())) return false
        }
        return true
    }

    fun copy(fileToCopy: File, destinationFolder: File, overwrite: Boolean) {
        if (!fileToCopy.exists()) throw Exception("File " + fileToCopy.absolutePath + " doesn't exist")
        if (fileToCopy.isFile) {
            copyFile(fileToCopy, destinationFolder, overwrite)
        } else copyFolder(fileToCopy, destinationFolder, overwrite)
    }

    fun copyFile(fileToCopy: File, destinationFolder: File, overwrite: Boolean) {
        copyFile(fileToCopy, fileToCopy.name, destinationFolder, overwrite)
    }

    /**
     * Copy file to a new folder
     *
     * @param fileToCopy:        the file that needs to be copied
     * @param fileName:          The name of the copied file in the destination folder
     * @param destinationFolder: The folder to copy the file into
     * @param overwrite:         Whether or not to overwrite the already existed file in the destination folder
     * @throws Exception: Any errors that occur during the copying process
     */
    fun copyFile(
        fileToCopy: File,
        fileName: String,
        destinationFolder: File,
        overwrite: Boolean
    ) {
        if (!destinationFolder.exists() && !destinationFolder.mkdirs()) {
            throw Exception(Log.UNABLE_TO + " create folder: " + destinationFolder)
        }
        val newFile = File(destinationFolder, fileName)
        if (newFile.exists() && !overwrite) return
        if (!newFile.exists() && !newFile.createNewFile()) {
            throw Exception(Log.UNABLE_TO + " " + CREATE_FILE + ": " + newFile)
        }
        val fileInputStream = FileInputStream(fileToCopy)
        val fileOutputStream = FileOutputStream(newFile, false)
        val buff = ByteArray(1024)
        var length: Int
        while (fileInputStream.read(buff).also { length = it } > 0) {
            fileOutputStream.write(buff, 0, length)
        }
        fileInputStream.close()
        fileOutputStream.close()
    }

    fun copyFolder(folderToCopy: File, destinationFolder: File, overwrite: Boolean) {
        copyFolder(folderToCopy, folderToCopy.name, destinationFolder, overwrite)
    }

    /**
     * Copy folder to another new folder
     *
     * @param folderToCopy:      the folder that needs to be copied
     * @param folderName:        The name of the copied folder in the destination folder
     * @param destinationFolder: The folder to copy into
     * @param overwrite:         Whether or not to overwrite the already existed files in the destination folder
     * @throws Exception: Any errors that occur during the copying process
     */
    fun copyFolder(
        folderToCopy: File,
        folderName: String,
        destinationFolder: File,
        overwrite: Boolean
    ) {
        val newFolder = File(destinationFolder, folderName)
        if (!newFolder.exists() && !newFolder.mkdirs()) {
            throw Exception(Log.UNABLE_TO + " create folder: " + newFolder)
        }
        if (newFolder.isFile) {
            throw Exception(
                """${Log.UNABLE_TO} create folder: $newFolder.
A file with the same name exists."""
            )
        }
        val folderContent = folderToCopy.listFiles()
        if (folderContent != null) {
            for (file in folderContent) {
                if (file.isFile) {
                    copyFile(file, file.name, newFolder, overwrite)
                } else {
                    copyFolder(file, file.name, newFolder, overwrite)
                }
            }
        }
    }

    fun deleteFile(file: File) {
        if (!file.exists()) {
            throw Exception("File $file doesn't exist")
        }
        if (!file.isFile) {
            val fileArr = file.listFiles()
            if (fileArr != null) {
                for (subFile in fileArr) {
                    if (subFile.isDirectory) {
                        deleteFile(subFile)
                    }
                    if (subFile.isFile) {
                        if (!subFile.delete()) throw Exception(Log.UNABLE_TO + " delete file: " + subFile)
                    }
                }
            }
        }
        if (!file.delete()) throw Exception(Log.UNABLE_TO + " delete file: " + file)
    }

    fun deleteFiles(selectedFiles: ArrayList<File>) {
        for (file in selectedFiles) {
            deleteFile(file)
        }
    }

    fun move(file: File, destination: File?) {
        if (file.isFile) {
            if (!file.renameTo(File(destination, file.name))) {
                throw IOException("Failed to move file: " + file.absolutePath)
            }
        } else {
            val parent = File(destination, file.name)
            if (parent.mkdir()) {
                val files = file.listFiles()
                if (files != null) {
                    for (child in files) {
                        move(child, parent)
                    }
                }
                if (!file.delete()) {
                    throw IOException("Failed to delete file: " + file.absolutePath)
                }
            } else {
                throw IOException("Failed to create folder: $parent")
            }
        }
    }

    fun setFileValidator(input: TextInputLayout, directory: File) {
        setFileValidator(input, null, directory)
    }

    fun setFileValidator(input: TextInputLayout, file: File?, directory: File) {
        setFileValidator(input, file, directory.list()?.toMutableList() as ArrayList<String>)
    }

    fun setFileValidator(input: TextInputLayout, file: File?, nameList: ArrayList<String>) {
        if (file != null) input.error = "This name is the same as before"
        else input.error = "Invalid file name"

        input.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                if (isValidFileName(editable.toString())) {
                    if (!nameList.contains(editable.toString())) {
                        input.error = null
                    } else if (file != null && editable.toString() == file.name) {
                        input.error = "This name is the same as before"
                    } else {
                        input.error = "This name is in use"
                    }
                } else {
                    input.error = "Invalid file name"
                }
            }
        })
    }

    fun isValidFileName(name: String): Boolean {
        return if (name.isEmpty()) false else !hasInvalidChar(
            name
        )
    }

    private fun hasInvalidChar(name: String): Boolean {
        for (ch in name.toCharArray()) {
            when (ch) {
                '"', '*', '/', ':', '>', '<', '?', '\\', '|', '\n', '\t', 0x7f.toChar() -> {
                    return true
                }
                else -> {}
            }
            if (ch.code <= 0x1f) return true
        }
        return false
    }

    fun rename(file: File, newName: String): Boolean {
        return file.renameTo(File(file.parentFile, newName))
    }

    fun shareFiles(filesToShare: ArrayList<File>, activity: Activity) {
        if (filesToShare.size == 1) {
            val file = filesToShare[0]
            if (file.isDirectory) {
                showMsg("Folders cannot be shared")
                return
            }
            val uri = FileProvider.getUriForFile(
                App.appContext,
                App.appContext.packageName + ".provider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            intent.type =
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension)
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            activity.startActivity(Intent.createChooser(intent, "Share file"))
            return
        }
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        intent.type = "*/*"
        val uriList = ArrayList<Uri>()
        for (file in filesToShare) {
            if (file.isFile) {
                val uri = FileProvider.getUriForFile(
                    App.appContext,
                    App.appContext.packageName + ".provider",
                    file
                )
                uriList.add(uri)
            }
        }
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList)
        activity.startActivity(Intent.createChooser(intent, "Share files"))
    }

    fun getFormattedSize(length: Long, format: String): String {
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

    fun getFormattedSize(length: Long): String {
        return getFormattedSize(length, "%.02f")
    }

    fun copyFromInputStream(inputStream: InputStream): String {
        val outputStream = ByteArrayOutputStream()
        val buf = ByteArray(1024)
        var i: Int
        try {
            while (inputStream.read(buf).also { i = it } != -1) {
                outputStream.write(buf, 0, i)
            }
            outputStream.close()
            inputStream.close()
        } catch (ignored: IOException) {
        }
        return outputStream.toString()
    }

    fun getApkIcon(file: File): Drawable? {
        val pi: PackageInfo? =
            App.appContext.packageManager.getPackageArchiveInfo(file.absolutePath, 0)
        return if (pi != null) {
            pi.applicationInfo.sourceDir = file.absolutePath
            pi.applicationInfo.publicSourceDir = file.absolutePath
            pi.applicationInfo.loadIcon(App.appContext.packageManager)
        } else {
            ContextCompat.getDrawable(App.appContext, R.drawable.unknown_file_extension)
        }
    }

    fun getApkName(file: File): String? {
        val pi = App.appContext.packageManager.getPackageArchiveInfo(file.absolutePath, 0)
        return if (pi != null) {
            App.appContext.packageManager.getApplicationLabel(pi.applicationInfo).toString()
        } else {
            pi?.applicationInfo?.name
        }
    }
}