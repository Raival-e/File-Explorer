package com.raival.fileexplorer.tab.apps.adapter

import android.annotation.SuppressLint
import android.os.Environment
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.raival.fileexplorer.App
import com.raival.fileexplorer.App.Companion.showMsg
import com.raival.fileexplorer.R
import com.raival.fileexplorer.common.BackgroundTask
import com.raival.fileexplorer.common.dialog.CustomDialog
import com.raival.fileexplorer.tab.apps.AppsTabFragment
import com.raival.fileexplorer.tab.apps.model.Apk
import com.raival.fileexplorer.tab.file.util.FileUtils
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

open class AppListAdapter(private val list: ArrayList<Apk>, private val fragment: AppsTabFragment) :
    RecyclerView.Adapter<AppListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        @SuppressLint("InflateParams") val view =
            fragment.layoutInflater.inflate(R.layout.apps_tab_app_item, null)
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
        return list.size
    }

    private fun showSaveDialog(file: Apk) {
        CustomDialog()
            .setIconDrawable(file.icon)
            .setTitle(file.name)
            .setMsg("Do you want to save this app to Download folder?")
            .setPositiveButton("Yes", { saveApkFile(file) }, true)
            .setNegativeButton("No", null, true)
            .show(fragment.parentFragmentManager, "")
    }

    private fun saveApkFile(file: Apk) {
        val backgroundTask = BackgroundTask()
        val error = AtomicBoolean(false)
        backgroundTask.setTasks({
            backgroundTask.showProgressDialog(
                "Copying...",
                fragment.requireActivity()
            )
        }, {
            try {
                FileUtils.copyFile(
                    file.source,
                    file.name + ".apk",
                    File(
                        Environment.getExternalStorageDirectory(),
                        Environment.DIRECTORY_DOWNLOADS
                    ),
                    true
                )
            } catch (e: Exception) {
                error.set(true)
                e.printStackTrace()
                App.appHandler.post { showMsg(e.toString()) }
            }
        }) {
            if (!error.get()) showMsg("APK file has been saved in " + "/Downloads/" + file.name)
            backgroundTask.dismiss()
        }
        backgroundTask.run()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var icon: ImageView
        var name: TextView
        var pkg: TextView
        var details: TextView

        init {
            icon = itemView.findViewById(R.id.app_icon)
            name = itemView.findViewById(R.id.app_name)
            pkg = itemView.findViewById(R.id.app_pkg)
            details = itemView.findViewById(R.id.app_details)
        }

        fun bind() {
            val position = adapterPosition
            val apk = list[position]
            name.text = apk.name
            pkg.text = apk.pkg
            details.text = apk.size
            icon.setImageDrawable(apk.icon)
            itemView.findViewById<View>(R.id.background)
                .setOnClickListener { showSaveDialog(apk) }
        }
    }
}