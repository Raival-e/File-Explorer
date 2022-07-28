package com.raival.fileexplorer.tab.file.adapter

import android.annotation.SuppressLint
import android.graphics.drawable.ColorDrawable
import android.text.TextUtils
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.raival.fileexplorer.R
import com.raival.fileexplorer.tab.file.FileExplorerTabDataHolder
import com.raival.fileexplorer.tab.file.FileExplorerTabFragment
import com.raival.fileexplorer.tab.file.extension.getFileDetails
import com.raival.fileexplorer.tab.file.observer.FileListObserver
import com.raival.fileexplorer.tab.file.util.FileUtils
import java.io.File

class FileListAdapter(private val parentFragment: FileExplorerTabFragment) :
    RecyclerView.Adapter<FileListAdapter.ViewHolder>() {

    private val selectedFileDrawable: ColorDrawable
    private val highlightedFileDrawable: ColorDrawable

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        @SuppressLint("InflateParams") val view =
            parentFragment.layoutInflater.inflate(R.layout.file_explorer_tab_file_item, null)
        view.layoutParams = RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int {
        return parentFragment.files.size
    }

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var name: TextView
        var details: TextView
        var icon: ImageView
        var background: View
        private var divider: View
        private lateinit var prevFile: File

        /**
         * Update the ui of each item
         */
        fun bind() {
            val position = adapterPosition
            val fileItem = parentFragment.files[position]
            if (!this::prevFile.isInitialized || !fileItem.file.isDirectory || !prevFile.isDirectory) {
                FileUtils.setFileIcon(icon, fileItem.file)
            }
            if (TextUtils.isEmpty(fileItem.name)) {
                name.text = fileItem.file.name
            } else {
                name.text = fileItem.name
            }
            if (TextUtils.isEmpty(fileItem.details)) {
                details.text = fileItem.file.getFileDetails()
            } else {
                details.text = fileItem.details
            }
            if (position == itemCount - 1) {
                divider.visibility = View.GONE
            } else {
                divider.visibility = View.VISIBLE
            }

            // Hidden files will be 50% transparent
            if (fileItem.file.isHidden) {
                if (icon.alpha == 1f) icon.alpha = 0.5f
            } else {
                if (icon.alpha < 1) icon.alpha = 1f
            }

            // Set a proper background color
            if (fileItem.isSelected) {
                if (background.foreground !== selectedFileDrawable) background.foreground =
                    selectedFileDrawable
            } else if (parentFragment.previousDirectory != null
                && fileItem.file.absolutePath == parentFragment.previousDirectory?.absolutePath
            ) {
                if (background.foreground !== highlightedFileDrawable) background.foreground =
                    highlightedFileDrawable
            } else {
                if (background.foreground != null) background.foreground = null
            }

            // Select/unselect item by pressing the icon
            itemView.findViewById<View>(R.id.icon_container).setOnClickListener {
                fileItem.isSelected = !fileItem.isSelected
                if (fileItem.isSelected) {
                    (parentFragment.dataHolder as FileExplorerTabDataHolder?)!!.selectedFiles.add(
                        fileItem.file
                    )
                } else {
                    (parentFragment.dataHolder as FileExplorerTabDataHolder?)!!.selectedFiles.remove(
                        fileItem.file
                    )
                }
                notifyItemChanged(position)
            }

            // Handle click event
            background.setOnClickListener {
                if (fileItem.file.isFile) {
                    parentFragment.openFile(fileItem)
                } else {
                    parentFragment.currentDirectory = fileItem.file
                    // Clear any selected files from the DataHolder (it also gets cleared
                    // in onBackPressed (when go back)
                    (parentFragment.dataHolder as FileExplorerTabDataHolder?)!!.selectedFiles.clear()
                }
            }
            val longClickListener = OnLongClickListener {
                parentFragment.showFileOptions(fileItem)
                true
            }

            // Apply the listener for both the background and the icon
            itemView.findViewById<View>(R.id.icon_container)
                .setOnLongClickListener(longClickListener)
            background.setOnLongClickListener(longClickListener)
            prevFile = fileItem.file
        }

        init {
            name = v.findViewById(R.id.file_name)
            details = v.findViewById(R.id.file_details)
            icon = v.findViewById(R.id.file_icon)
            background = v.findViewById(R.id.background)
            divider = v.findViewById(R.id.divider)
        }
    }

    init {
        registerAdapterDataObserver(FileListObserver(parentFragment, this))
        selectedFileDrawable =
            ColorDrawable(parentFragment.requireContext().getColor(R.color.selectedFileHighlight))
        highlightedFileDrawable =
            ColorDrawable(parentFragment.requireContext().getColor(R.color.previousFileHighlight))
    }
}