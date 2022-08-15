package com.raival.fileexplorer.tab.file.misc

object FileMimeTypes {
    const val apkType = "apk"
    const val rarType = "rar"
    const val pdfType = "pdf"
    const val javaType = "java"
    const val kotlinType = "kt"
    const val xmlType = "xml"

    const val aiType = "ai"
    const val docType = "doc"
    const val docxType = "docx"
    const val xlsType = "xls"
    const val xlsxType = "xlsx"

    const val pptType = "ppt"
    const val pptxType = "pptx"
    const val sqlType = "sql"
    const val svgType = "svg"

    const val default = "*/*"

    @JvmField
    val fontType = arrayOf("ttf", "otf")

    @JvmField
    val audioType = arrayOf("mp3", "4mp", "aup", "ogg", "3ga", "m4b", "wav", "acc", "m4a")

    @JvmField
    val videoType = arrayOf("mp4", "mov", "avi", "mkv", "wmv", "m4v", "3gp", "webm")

    @JvmField
    val archiveType = arrayOf("zip", "7z", "tar", "jar", "gz", "xz", "xapk", "obb", apkType)

    @JvmField
    val textType = arrayOf("txt", "text", "log", "dsc", "apt", "rtf", "rtx")

    @JvmField
    val codeType = arrayOf(javaType, xmlType, "py", "css", kotlinType, "cs", "xml", "json")

    @JvmField
    val imageType = arrayOf("png", "jpeg", "jpg", "heic", "tiff", "gif", "webp", svgType, "bmp")

    val mimeTypes = HashMap<String, String>().apply {
        put("asm", "text/x-asm");
        put("def", "text/plain");
        put("in", "text/plain");
        put("rc", "text/plain");
        put("list", "text/plain");
        put("log", "text/plain");
        put("pl", "text/plain");
        put("prop", "text/plain");
        put("properties", "text/plain");
        put("rc", "text/plain");

        put("epub", "application/epub+zip");
        put("ibooks", "application/x-ibooks+zip");

        put("ifb", "text/calendar");
        put("eml", "message/rfc822");
        put("msg", "application/vnd.ms-outlook");

        put("ace", "application/x-ace-compressed");
        put("bz", "application/x-bzip");
        put("bz2", "application/x-bzip2");
        put("cab", "application/vnd.ms-cab-compressed");
        put("gz", "application/x-gzip");
        put("lrf", "application/octet-stream");
        put("jar", "application/java-archive");
        put("xz", "application/x-xz");
        put("Z", "application/x-compress");

        put("bat", "application/x-msdownload");
        put("ksh", "text/plain");
        put("sh", "application/x-sh");

        put("db", "application/octet-stream");
        put("db3", "application/octet-stream");

        put("otf", "application/x-font-otf");
        put("ttf", "application/x-font-ttf");
        put("psf", "application/x-font-linux-psf");

        put("cgm", "image/cgm");
        put("btif", "image/prs.btif");
        put("dwg", "image/vnd.dwg");
        put("dxf", "image/vnd.dxf");
        put("fbs", "image/vnd.fastbidsheet");
        put("fpx", "image/vnd.fpx");
        put("fst", "image/vnd.fst");
        put("mdi", "image/vnd.ms-mdi");
        put("npx", "image/vnd.net-fpx");
        put("xif", "image/vnd.xiff");
        put("pct", "image/x-pict");
        put("pic", "image/x-pict");

        put("adp", "audio/adpcm");
        put("au", "audio/basic");
        put("snd", "audio/basic");
        put("m2a", "audio/mpeg");
        put("m3a", "audio/mpeg");
        put("oga", "audio/ogg");
        put("spx", "audio/ogg");
        put("aac", "audio/x-aac");
        put("mka", "audio/x-matroska");

        put("jpgv", "video/jpeg");
        put("jpgm", "video/jpm");
        put("jpm", "video/jpm");
        put("mj2", "video/mj2");
        put("mjp2", "video/mj2");
        put("mpa", "video/mpeg");
        put("ogv", "video/ogg");
        put("flv", "video/x-flv");
        put("mkv", "video/x-matroska");
    }
}