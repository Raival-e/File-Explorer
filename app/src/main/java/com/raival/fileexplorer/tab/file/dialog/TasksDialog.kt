package com.raival.fileexplorer.tab.file.dialog

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.raival.fileexplorer.App.Companion.showMsg
import com.raival.fileexplorer.R
import com.raival.fileexplorer.activity.model.MainViewModel
import com.raival.fileexplorer.tab.file.FileExplorerTabFragment
import com.raival.fileexplorer.tab.file.model.Task
import com.raival.fileexplorer.tab.file.model.Task.OnFinishListener
import com.raival.fileexplorer.tab.file.model.Task.OnUpdateListener

class TasksDialog(private val fileExplorerTabFragment: FileExplorerTabFragment) :
    BottomSheetDialogFragment() {

    private lateinit var container: ViewGroup
    private lateinit var mainViewModel: MainViewModel
    private lateinit var alertDialog: AlertDialog
    private lateinit var placeHolder: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.file_explorer_tab_task_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        container = view.findViewById(R.id.container)
        placeHolder = view.findViewById(R.id.place_holder)
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        val tasks = mainViewModel.tasks
        if (tasks.isNotEmpty()) {
            placeHolder.visibility = View.GONE
        }
        for (task in tasks) {
            addTask(task, task.isValid)
        }
    }

    override fun getTheme(): Int {
        return R.style.ThemeOverlay_Material3_BottomSheetDialog
    }

    private fun addTask(task: Task, valid: Boolean) {
        val v =
            layoutInflater.inflate(R.layout.file_explorer_tab_task_dialog_item, container, false)
        (v.findViewById<View>(R.id.label) as TextView).text = task.name
        (v.findViewById<View>(R.id.task_details) as TextView).text = task.details
        v.findViewById<View>(R.id.background).setOnClickListener {
            if (!valid) {
                showMsg("This task is invalid, some files are missing, long click to execute it anyway")
                return@setOnClickListener
            }
            run(task)
        }
        v.findViewById<View>(R.id.remove).setOnClickListener {
            mainViewModel.tasks.remove(task)
            container.removeView(v)
            if (container.childCount == 0) {
                placeHolder.visibility = View.VISIBLE
            }
        }
        v.setOnLongClickListener label@{
            if (!valid) {
                run(task)
                return@label true
            }
            false
        }
        container.addView(v)
    }

    @SuppressLint("SetTextI18n")
    private fun run(task: Task) {
        dismiss()
        task.setActiveDirectory(fileExplorerTabFragment.currentDirectory!!)
        val view = progressView
        val progressText = view.findViewById<TextView>(R.id.msg)
        progressText.text = "Processing..."
        alertDialog = MaterialAlertDialogBuilder(requireActivity())
            .setCancelable(false)
            .setView(view)
            .show()

        task.start(
            object : OnUpdateListener {
                override fun onUpdate(progress: String) {
                    progressText.text = progress
                }
            },
            object : OnFinishListener {
                override fun onFinish(result: String) {
                    mainViewModel.tasks.remove(task)
                    showMsg(result)
                    fileExplorerTabFragment.refresh()
                    alertDialog.dismiss()
                }
            }
        )
    }

    @get:SuppressLint("InflateParams")
    private val progressView: View
        get() = requireActivity().layoutInflater.inflate(R.layout.progress_view, null)
}