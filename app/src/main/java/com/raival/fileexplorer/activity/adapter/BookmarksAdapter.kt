package com.raival.fileexplorer.activity.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.raival.fileexplorer.App.Companion.showMsg
import com.raival.fileexplorer.R
import com.raival.fileexplorer.activity.MainActivity
import com.raival.fileexplorer.tab.file.misc.IconHelper
import com.raival.fileexplorer.util.PrefsUtils
import com.raival.fileexplorer.util.Utils
import java.io.File

class BookmarksAdapter(private val activity: MainActivity) :
    RecyclerView.Adapter<BookmarksAdapter.ViewHolder>() {
    private var list = arrayListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        @SuppressLint("InflateParams") val v =
            activity.layoutInflater.inflate(R.layout.activity_main_drawer_bookmark_item, null)
        v.layoutParams = RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int {
        return PrefsUtils.TextEditor.fileExplorerTabBookmarks.also { list = it }.size
    }

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var name: TextView
        var details: TextView
        var icon: ImageView
        var background: View

        init {
            name = v.findViewById(R.id.name)
            details = v.findViewById(R.id.details)
            icon = v.findViewById(R.id.icon)
            background = v.findViewById(R.id.background)
        }

        @SuppressLint("NotifyDataSetChanged")
        fun bind() {
            val position = adapterPosition
            val file = File(list[position])
            if (file.isFile && file.name.endsWith(".extension")) {
                name.text = file.name.substring(0, file.name.length - 10)
            } else {
                name.text = file.name
            }
            if (!file.exists()) {
                name.setTextColor(Color.RED)
                details.setTextColor(Color.RED)
                background.setOnClickListener {
                    showMsg("This file doesn't exist anymore")
                    list.remove(file.absolutePath)
                    PrefsUtils.General.setFileExplorerTabBookmarks(list)
                    list.clear()
                    notifyDataSetChanged()
                }
            } else {
                name.setTextColor(Utils.getColorAttribute(R.attr.colorOnSurface, activity))
                details.setTextColor(Utils.getColorAttribute(R.attr.colorOnSurface, activity))
                background.setOnClickListener { activity.onBookmarkSelected(file) }
            }
            details.text = file.absolutePath
            IconHelper.setFileIcon(icon, file)
            background.setOnLongClickListener {
                list.remove(file.absolutePath)
                PrefsUtils.General.setFileExplorerTabBookmarks(list)
                list.clear()
                notifyDataSetChanged()
                true
            }
        }
    }
}