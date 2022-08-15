package com.raival.fileexplorer.tab.file.misc

import net.lingala.zip4j.ZipFile
import java.io.File

fun archive(filesToCompress: ArrayList<File>, zipFile: File?) {
    val zip = ZipFile(zipFile)
    for (file in filesToCompress) {
        if (file.isFile) {
            zip.addFile(file)
        } else {
            zip.addFolder(file)
        }
    }
}

fun extract(filesToExtract: ArrayList<File>, directory: File) {
    for (file in filesToExtract) {
        if (file.isFile) {
            val output = File(directory, file.name.substring(0, file.name.lastIndexOf(".")))
            if (output.mkdir()) {
                ZipFile(file).extractAll(output.absolutePath)
            } else {
                ZipFile(file).extractAll(directory.absolutePath)
            }
        }
    }
}