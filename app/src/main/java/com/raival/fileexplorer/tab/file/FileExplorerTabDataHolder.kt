package com.raival.fileexplorer.tab.file

import android.os.Parcelable
import com.raival.fileexplorer.tab.BaseDataHolder
import com.raival.fileexplorer.tab.file.model.FileItem
import java.io.File

class FileExplorerTabDataHolder(override val tag: String) : BaseDataHolder() {
    @JvmField
    var activeDirectory: File? = null

    @JvmField
    var recyclerViewStates: HashMap<File, Parcelable> = HashMap()
    var searchList = ArrayList<FileItem>()

    @JvmField
    var selectedFiles = ArrayList<File>()
}