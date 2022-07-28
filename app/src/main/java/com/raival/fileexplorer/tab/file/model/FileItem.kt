package com.raival.fileexplorer.tab.file.model

import java.io.File

class FileItem(var f: File) {
    @JvmField
    var isSelected = false

    @JvmField
    var file: File = f
    var details = ""
    var name = ""
}