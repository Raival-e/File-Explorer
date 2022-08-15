package com.raival.fileexplorer.tab.file.dialog

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.raival.fileexplorer.App.Companion.copyString
import com.raival.fileexplorer.App.Companion.showMsg
import com.raival.fileexplorer.R
import com.raival.fileexplorer.extension.getFolderSize
import com.raival.fileexplorer.extension.getFormattedFileCount
import com.raival.fileexplorer.extension.getLastModifiedDate
import com.raival.fileexplorer.extension.toFormattedSize
import com.raival.fileexplorer.tab.file.misc.IconHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class FileInfoDialog(private val file: File) : BottomSheetDialogFragment() {
    private val infoList = ArrayList<InfoHolder>()
    private var useDefaultFileInfo = false
    private lateinit var container: ViewGroup

    fun setUseDefaultFileInfo(`is`: Boolean): FileInfoDialog {
        useDefaultFileInfo = `is`
        return this
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.file_explorer_tab_info_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (view.findViewById<View>(R.id.file_name) as TextView).text = file.name
        IconHelper.setFileIcon(view.findViewById(R.id.file_icon), file)
        container = view.findViewById(R.id.container)
        if (!useDefaultFileInfo) {
            for (holder in infoList) {
                addItemView(holder, container)
            }
        } else {
            if (file.isFile) {
                addDefaultFileInfo()
            } else {
                addDefaultFolderInfo()
            }
        }
    }

    private fun addDefaultFolderInfo() {
        addItemView(InfoHolder("Path:", file.absolutePath, true), container)
        addItemView(
            InfoHolder(
                "Modified:", file.getLastModifiedDate(), true
            ), container
        )
        addItemView(
            InfoHolder(
                "Content:", file.getFormattedFileCount(), true
            ), container
        )
        addItemView(InfoHolder("Type:", if (file.isFile) "File" else "Folder", true), container)
        addItemView(InfoHolder("Read:", if (file.canRead()) "Yes" else "No", true), container)
        addItemView(InfoHolder("Write:", if (file.canWrite()) "Yes" else "No", true), container)
        val size = addItemView(InfoHolder("Size:", "Counting...", true), container)
        CoroutineScope(Dispatchers.IO).launch {
            val s = file.getFolderSize().toFormattedSize()
            withContext(Dispatchers.Main) { size.text = s }
        }.start()
    }

    private fun addDefaultFileInfo() {
        addItemView(InfoHolder("Path:", file.absolutePath, true), container)
        addItemView(
            InfoHolder(
                "Extension:", file.extension, true
            ), container
        )
        addItemView(
            InfoHolder(
                "Modified:", file.getLastModifiedDate(), true
            ), container
        )
        addItemView(InfoHolder("Type:", if (file.isFile) "File" else "Folder", true), container)
        addItemView(InfoHolder("Read:", if (file.canRead()) "Yes" else "No", true), container)
        addItemView(InfoHolder("Write:", if (file.canWrite()) "Yes" else "No", true), container)
        addItemView(
            InfoHolder(
                "Size:", file.length().toFormattedSize(), true
            ), container
        )
    }

    private fun addItemView(holder: InfoHolder, container: ViewGroup?): TextView {
        @SuppressLint("InflateParams") val view =
            layoutInflater.inflate(R.layout.file_explorer_tab_info_dialog_item, null, false)
        (view.findViewById<View>(R.id.name) as TextView).text = holder.name
        val details = view.findViewById<TextView>(R.id.details)
        details.text = holder.info
        if (holder.clickable) {
            details.isClickable = true
            details.setOnClickListener {
                copyString(holder.info)
                showMsg(holder.name + " has been copied")
            }
        }
        container!!.addView(view)
        return details
    }

    fun addItem(name: String, info: String?, clickable: Boolean): FileInfoDialog {
        infoList.add(InfoHolder(name, info, clickable))
        return this
    }

    override fun getTheme(): Int {
        return R.style.ThemeOverlay_Material3_BottomSheetDialog
    }

    class InfoHolder(var name: String, var info: String?, var clickable: Boolean)
}