package com.raival.fileexplorer.tab.file.util

object FileExtensions {
    @JvmField
    val audioType = arrayOf("mp3", "4mp", "aup", "ogg", "3ga", "m4b", "wav", "acc", "m4a")

    @JvmField
    val videoType = arrayOf("mp4", "mov", "avi", "mkv", "wmv", "m4v", "3gp", "webm")

    @JvmField
    val archiveType = arrayOf("zip", "7z", "tar", "jar", "gz", "xz", "xapk", "obb", "apk", "rar")

    @JvmField
    val textType = arrayOf("txt", "text", "log", "dsc", "apt", "rtf", "rtx")

    @JvmField
    val codeType = arrayOf("java", "xml", "py", "css", "kt", "cs", "xml", "json", "svg")

    @JvmField
    val imageType = arrayOf("png", "jpeg", "jpg", "heic", "tiff", "gif", "webp", "svg", "bmp")
    const val apkType = "apk"
    const val rarType = "rar"
    const val pdfType = "pdf"
    const val javaType = "java"
    const val KotlinType = "kt"
}