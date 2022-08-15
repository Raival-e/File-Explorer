package com.raival.fileexplorer.tab.file.misc

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.raival.fileexplorer.App
import com.raival.fileexplorer.R
import com.raival.fileexplorer.glide.model.IconRes
import java.io.File
import java.util.zip.ZipEntry

object IconHelper {
    fun setFileIcon(icon: ImageView, file: Any) {
        if ((file is File && !file.isFile) || (file is ZipEntry && file.isDirectory)) {
            icon.setImageResource(R.drawable.ic_baseline_folder_24)
            return
        }

        val ext: String = if (file is File) file.extension.lowercase()
        else ""

        if (ext == FileMimeTypes.pdfType) {
            Glide.with(App.appContext)
                .load(IconRes(R.drawable.pdf_file_extension))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(icon)
            return
        }
        if (FileMimeTypes.textType.contains(ext)) {
            Glide.with(App.appContext)
                .load(IconRes(R.drawable.txt_file_extension))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(icon)
            return
        }
        if (ext == FileMimeTypes.javaType) {
            Glide.with(App.appContext)
                .load(IconRes(R.drawable.java_file_extension))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(icon)
            return
        }
        if (ext == FileMimeTypes.kotlinType) {
            Glide.with(App.appContext)
                .load(IconRes(R.drawable.kt_file_extension))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(icon)
            return
        }
        if (ext == FileMimeTypes.xmlType) {
            Glide.with(App.appContext)
                .load(IconRes(R.drawable.xml_file_extension))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(icon)
            return
        }
        if (FileMimeTypes.codeType.contains(ext)) {
            Glide.with(App.appContext)
                .load(IconRes(R.drawable.code_file_extension))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(icon)
            return
        }
        if (ext == FileMimeTypes.apkType) {
            Glide.with(App.appContext)
                .load(if (file is File) file.absolutePath else R.drawable.apk_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .error(R.drawable.apk_placeholder)
                .into(icon)
            return
        }
        if (FileMimeTypes.archiveType.contains(ext) || ext == FileMimeTypes.rarType) {
            Glide.with(App.appContext)
                .load(IconRes(R.drawable.archive_file_extension))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(icon)
            return
        }
        if (FileMimeTypes.videoType.contains(ext)) {
            if (file is File) {
                Glide.with(App.appContext)
                    .load(file)
                    .signature(ObjectKey(file.lastModified()))
                    .error(R.drawable.video_file_extension)
                    .placeholder(R.drawable.video_file_extension)
                    .into(icon)
            } else {
                Glide.with(App.appContext)
                    .load(R.drawable.video_file_extension)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(icon)
            }
            return
        }
        if (FileMimeTypes.audioType.contains(ext)) {
            Glide.with(App.appContext)
                .load(IconRes(R.drawable.music_file_extension))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(icon)
            return
        }

        if (FileMimeTypes.fontType.contains(ext)) {
            Glide.with(App.appContext)
                .load(IconRes(R.drawable.font_file_extension))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(icon)
            return
        }

        if (ext == FileMimeTypes.sqlType) {
            Glide.with(App.appContext)
                .load(IconRes(R.drawable.sql_file_extension))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(icon)
            return
        }

        if (ext == FileMimeTypes.aiType) {
            Glide.with(App.appContext)
                .load(IconRes(R.drawable.vector_file_extension))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(icon)
            return
        }

        if (ext == FileMimeTypes.svgType) {
            Glide.with(App.appContext)
                .load(IconRes(R.drawable.svg_file_extension))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(icon)
            return
        }

        if (FileMimeTypes.imageType.contains(ext)) {
            if (file is File) {
                Glide.with(App.appContext)
                    .applyDefaultRequestOptions(RequestOptions().override(100).encodeQuality(80))
                    .load(file)
                    .signature(ObjectKey(file.lastModified()))
                    .error(R.drawable.image_file_extension)
                    .into(icon)
            } else {
                Glide.with(App.appContext)
                    .load(R.drawable.image_file_extension)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(icon)
            }
            return
        }

        if (ext == FileMimeTypes.docType || ext == FileMimeTypes.docxType) {
            Glide.with(App.appContext)
                .load(IconRes(R.drawable.doc_file_extension))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(icon)
            return
        }

        if (ext == FileMimeTypes.xlsType || ext == FileMimeTypes.xlsxType) {
            Glide.with(App.appContext)
                .load(IconRes(R.drawable.xls_file_extension))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(icon)
            return
        }

        if (ext == FileMimeTypes.pptType || ext == FileMimeTypes.pptxType) {
            Glide.with(App.appContext)
                .load(IconRes(R.drawable.powerpoint_file_extension))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(icon)
            return
        }

        if (ext == "extension") {
            icon.setImageResource(R.drawable.ic_baseline_extension_24)
            return
        }
        Glide.with(App.appContext)
            .load(IconRes(R.drawable.unknown_file_extension))
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(icon)
    }
}