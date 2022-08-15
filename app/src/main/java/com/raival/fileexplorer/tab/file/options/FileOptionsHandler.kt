package com.raival.fileexplorer.tab.file.options

import android.annotation.SuppressLint
import android.content.Intent
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.raival.fileexplorer.App.Companion.showMsg
import com.raival.fileexplorer.R
import com.raival.fileexplorer.activity.MainActivity
import com.raival.fileexplorer.activity.TextEditorActivity
import com.raival.fileexplorer.activity.model.MainViewModel
import com.raival.fileexplorer.common.BackgroundTask
import com.raival.fileexplorer.common.dialog.CustomDialog
import com.raival.fileexplorer.common.dialog.OptionsDialog
import com.raival.fileexplorer.extension.openFileWith
import com.raival.fileexplorer.tab.BaseTabFragment
import com.raival.fileexplorer.tab.file.FileExplorerTabFragment
import com.raival.fileexplorer.tab.file.dialog.FileInfoDialog
import com.raival.fileexplorer.tab.file.dialog.SearchDialog
import com.raival.fileexplorer.tab.file.executor.Executor
import com.raival.fileexplorer.tab.file.misc.FileUtils
import com.raival.fileexplorer.tab.file.model.FileItem
import com.raival.fileexplorer.tab.file.model.Task.OnFinishListener
import com.raival.fileexplorer.tab.file.model.Task.OnUpdateListener
import com.raival.fileexplorer.tab.file.task.*
import com.raival.fileexplorer.util.Log
import com.raival.fileexplorer.util.PrefsUtils
import java.io.File
import java.util.*
import java.util.concurrent.atomic.AtomicReference

class FileOptionsHandler(private val parentFragment: FileExplorerTabFragment) {
    private var mainViewModel: MainViewModel? = null
        get() {
            if (field == null) {
                field = ViewModelProvider(parentFragment.requireActivity()).get(
                    MainViewModel::class.java
                )
            }
            return field
        }

    fun showOptions(fileItem: FileItem) {
        val selectedFiles = ArrayList<File>()
        for (item in parentFragment.selectedFiles) {
            selectedFiles.add(item.file)
        }
        if (!fileItem.isSelected) selectedFiles.add(fileItem.file)
        val title: String = if (selectedFiles.size == 1) {
            fileItem.file.name
        } else {
            "" + selectedFiles.size + " Files selected"
        }
        val bottomDialog = OptionsDialog(title)
        bottomDialog.show(parentFragment.parentFragmentManager, "FileOptionsDialog")

        //______________| Options |_______________\\
        if (FileUtils.isSingleFile(selectedFiles)) {
            if (selectedFiles[0].name.lowercase(Locale.getDefault()).endsWith(".java")
                || selectedFiles[0].name.lowercase(Locale.getDefault()).endsWith(".kt")
            ) {
                bottomDialog.addOption("Execute", R.drawable.ic_round_code_24, {
                    exeJava(
                        selectedFiles[0].parentFile!!
                    )
                }, true)
            }
        }
        if (selectedFiles.size == 1) {
            val list = PrefsUtils.TextEditor.fileExplorerTabBookmarks
            if (!list.contains(selectedFiles[0].toString())) {
                bottomDialog.addOption(
                    "Add to bookmarks",
                    R.drawable.ic_baseline_bookmark_add_24,
                    {
                        list.add(selectedFiles[0].absolutePath)
                        PrefsUtils.General.setFileExplorerTabBookmarks(list)
                        (parentFragment.requireActivity() as MainActivity).refreshBookmarks()
                        showMsg("Added to bookmarks successfully")
                    },
                    true
                )
            } else {
                bottomDialog.addOption(
                    "Remove from bookmarks",
                    R.drawable.ic_baseline_bookmark_remove_24,
                    {
                        list.remove(selectedFiles[0].absolutePath)
                        PrefsUtils.General.setFileExplorerTabBookmarks(list)
                        (parentFragment.requireActivity() as MainActivity).refreshBookmarks()
                        showMsg("Removed from bookmarks successfully")
                    },
                    true
                )
            }
        }
        if (FileUtils.isSingleFolder(selectedFiles)) {
            bottomDialog.addOption(
                "Open in a new tab",
                R.drawable.ic_round_tab_24,
                {
                    if (parentFragment.requireActivity() is MainActivity) {
                        val fragment = FileExplorerTabFragment(selectedFiles[0])
                        (parentFragment.requireActivity() as MainActivity).addNewTab(
                            fragment,
                            BaseTabFragment.FILE_EXPLORER_TAB_FRAGMENT_PREFIX
                                    + (parentFragment.requireActivity() as MainActivity).generateRandomTag()
                        )
                    }
                },
                true
            )
        }
        if (FileUtils.isOnlyFiles(selectedFiles)) {
            bottomDialog.addOption("Share", R.drawable.ic_round_share_24, {
                FileUtils.shareFiles(selectedFiles, parentFragment.requireActivity())
                parentFragment.setSelectAll(false)
            }, true)
        }
        if (FileUtils.isSingleFile(selectedFiles)) {
            bottomDialog.addOption(
                "Open with",
                R.drawable.ic_baseline_open_in_new_24,
                {
                    selectedFiles[0].openFileWith(true)
                    parentFragment.setSelectAll(false)
                },
                true
            )
        }
        bottomDialog.addOption("Copy", R.drawable.ic_baseline_file_copy_24, {
            mainViewModel!!.tasks.add(CopyTask(selectedFiles))
            parentFragment.setSelectAll(false)
            notifyNewTask()
        }, true)
        if (FileUtils.isSingleFile(selectedFiles)) {
            bottomDialog.addOption(
                "Create a backup",
                R.drawable.ic_baseline_file_copy_24,
                {
                    createBackupFile(selectedFiles[0])
                    parentFragment.setSelectAll(false)
                },
                true
            )
        }
        bottomDialog.addOption("Cut", R.drawable.ic_round_content_cut_24, {
            mainViewModel!!.tasks.add(CutTask(selectedFiles))
            parentFragment.setSelectAll(false)
            notifyNewTask()
        }, true)
        if (selectedFiles.size == 1) {
            bottomDialog.addOption(
                "Rename",
                R.drawable.ic_round_edit_24,
                { showRenameDialog(selectedFiles) },
                true
            )
        }
        bottomDialog.addOption(
            "Delete",
            R.drawable.ic_round_delete_forever_24,
            { confirmDeletion(selectedFiles) },
            true
        )
        if (FileUtils.isSingleFile(selectedFiles)) {
            bottomDialog.addOption(
                "Edit with code editor",
                R.drawable.ic_round_edit_note_24,
                {
                    openWithTextEditor(
                        selectedFiles[0]
                    )
                },
                true
            )
        }
        if (FileUtils.isArchiveFiles(selectedFiles)) {
            bottomDialog.addOption("Extract", R.drawable.ic_baseline_logout_24, {
                mainViewModel!!.tasks.add(ExtractTask(selectedFiles))
                parentFragment.setSelectAll(false)
                notifyNewTask()
            }, true)
        }
        bottomDialog.addOption(
            "Compress",
            R.drawable.ic_round_compress_24,
            { compressFiles(CompressTask(selectedFiles)) },
            true
        )
        if (FileUtils.isSingleFile(selectedFiles)) {
            if (selectedFiles[0].extension.equals("jar", ignoreCase = true)) {
                bottomDialog.addOption("Jar2Dex", R.drawable.ic_round_code_24, {
                    jar2Dex(
                        Jar2DexTask(
                            selectedFiles[0]
                        )
                    )
                }, true)
            }
        }
        if (selectedFiles.size == 1) {
            bottomDialog.addOption("Details", R.drawable.ic_baseline_info_24, {
                showFileInfoDialog(
                    selectedFiles[0]
                )
            }, true)
        }
        bottomDialog.addOption("Search", R.drawable.ic_round_manage_search_24, {
            val searchFragment = SearchDialog(
                parentFragment, selectedFiles
            )
            searchFragment.show(parentFragment.parentFragmentManager, "")
            parentFragment.setSelectAll(false)
        }, true)
    }

    private fun createBackupFile(file: File) {
        val backgroundTask = BackgroundTask()
        backgroundTask.setTasks({
            backgroundTask.showProgressDialog(
                "creating a backup file...",
                parentFragment.requireActivity()
            )
        }, {
            try {
                FileUtils.copyFile(
                    file, generateUniqueFileName(
                        file.nameWithoutExtension
                                + "_copy."
                                + file.extension, file.parentFile!!
                    ), file.parentFile!!, true
                )
            } catch (e: Exception) {
                e.printStackTrace()
                showMsg(e.toString())
            }
        }) {
            backgroundTask.dismiss()
            showMsg("New backup file has been created")
            parentFragment.refresh()
        }
        backgroundTask.run()
    }

    private fun generateUniqueFileName(name: String, directory: File): String {
        var file = File(directory, name)
        var i = 2
        while (file.exists()) {
            file = File(directory, name)
            val newName =
                file.nameWithoutExtension + i + "." + file.extension
            file = File(directory, newName)
            i++
        }
        return file.name
    }

    @SuppressLint("SetTextI18n")
    private fun jar2Dex(task: Jar2DexTask) {
        if (task.isValid) {
            val dir = parentFragment.currentDirectory
            task.setActiveDirectory(dir!!)
            val view = progressView
            val progressText = view.findViewById<TextView>(R.id.msg)
            progressText.text = "Processing..."
            val dialog = dialog.setView(view).show()
            task.start(
                object : OnUpdateListener {
                    override fun onUpdate(progress: String) {
                        progressText.text = progress
                    }
                },
                object : OnFinishListener {
                    override fun onFinish(result: String) {
                        showMsg(result)
                        parentFragment.refresh()
                        dialog.dismiss()
                    }
                })
        } else {
            showMsg("The jar file doesn't exist")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun compressFiles(task: CompressTask) {
        val customDialog = CustomDialog()
        val input = customDialog.createInput(parentFragment.requireActivity(), "Archive name")

        input.editText?.setText(".zip")
        FileUtils.setFileValidator(input, (parentFragment).currentDirectory!!)
        CustomDialog()
            .setTitle("Compress")
            .addView(input)
            .setPositiveButton("Save", {
                if (input.error == null) {
                    if (task.isValid) {
                        task.setActiveDirectory(
                            File(
                                parentFragment.currentDirectory, input.editText!!
                                    .text.toString()
                            )
                        )
                        val view = progressView
                        val progressText = view.findViewById<TextView>(R.id.msg)
                        progressText.text = "Processing..."
                        val dialog = dialog.setView(view).show()
                        task.start(
                            object : OnUpdateListener {
                                override fun onUpdate(progress: String) {
                                    progressText.text = progress
                                }
                            },
                            object : OnFinishListener {
                                override fun onFinish(result: String) {
                                    showMsg(result)
                                    parentFragment.refresh()
                                    dialog.dismiss()
                                }
                            })
                    } else {
                        showMsg("The files to compress are missing")
                    }
                } else {
                    showMsg("Compress canceled")
                }
            }, true)
            .show(parentFragment.childFragmentManager, "")
    }

    private fun openWithTextEditor(file: File) {
        val intent = Intent()
        intent.setClass(parentFragment.requireActivity(), TextEditorActivity::class.java)
        intent.putExtra("file", file.absolutePath)
        parentFragment.requireActivity().startActivity(intent)
    }

    private fun notifyNewTask() {
        showMsg("A new task has been added")
    }

    private fun showFileInfoDialog(file: File) {
        FileInfoDialog(file).setUseDefaultFileInfo(true)
            .show(parentFragment.parentFragmentManager, "")
    }

    @get:SuppressLint("InflateParams")
    private val progressView: View
        get() = parentFragment.requireActivity().layoutInflater.inflate(
            R.layout.progress_view,
            null
        )
    private val dialog: AlertDialog.Builder
        get() = MaterialAlertDialogBuilder(parentFragment.requireActivity())
            .setCancelable(false)

    private fun exeJava(file: File) {
        val executor = Executor(file, (parentFragment.requireActivity() as AppCompatActivity))
        val backgroundTask = BackgroundTask()
        val error = AtomicReference("")
        backgroundTask.setTasks({
            backgroundTask.showProgressDialog(
                "compiling files...",
                parentFragment.requireActivity()
            )
        }, {
            try {
                executor.execute()
            } catch (exception: Exception) {
                error.set(Log.getStackTrace(exception))
            }
        }) {
            try {
                if (error.get() != "") {
                    backgroundTask.dismiss()
                    parentFragment.showDialog("Error", error.get())
                    return@setTasks
                }
                executor.invoke()
                backgroundTask.dismiss()
            } catch (exception: Exception) {
                backgroundTask.dismiss()
                parentFragment.showDialog("Error", Log.getStackTrace(exception))
            }
            parentFragment.refresh()
        }
        backgroundTask.run()
    }

    private fun showRenameDialog(selectedFiles: ArrayList<File>) {
        val customDialog = CustomDialog()
        val input = customDialog.createInput(parentFragment.requireActivity(), "File name")

        input.editText?.setText(selectedFiles[0].name)
        input.editText!!.setSingleLine()
        FileUtils.setFileValidator(input, selectedFiles[0], selectedFiles[0].parentFile)

        customDialog.setTitle("Rename")
            .addView(input)
            .setPositiveButton("Save", {
                if (input.error == null) {
                    if (!FileUtils.rename(
                            selectedFiles[0], input.editText!!
                                .text.toString()
                        )
                    ) {
                        showMsg("Cannot rename this file")
                    } else {
                        showMsg("File has been renamed")
                        parentFragment.refresh()
                    }
                } else {
                    showMsg("Rename canceled")
                }
            }, true)
            .show(parentFragment.parentFragmentManager, "")
    }

    private fun confirmDeletion(selectedFiles: ArrayList<File>) {
        MaterialAlertDialogBuilder(parentFragment.requireContext())
            .setTitle("Delete")
            .setMessage("Do you want to delete selected files? this action cannot be redone.")
            .setPositiveButton("Confirm") { _, _ -> doDelete(selectedFiles) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun doDelete(selectedFiles: ArrayList<File>) {
        parentFragment.setSelectAll(false)
        val backgroundTask = BackgroundTask()
        backgroundTask.setTasks({
            backgroundTask.showProgressDialog(
                "Deleting files...",
                parentFragment.requireActivity()
            )
        }, {
            try {
                FileUtils.deleteFiles(selectedFiles)
            } catch (e: Exception) {
                e.printStackTrace()
                showMsg(e.toString())
            }
        }) {
            backgroundTask.dismiss()
            showMsg("Files have been deleted")
            parentFragment.refresh()
        }
        backgroundTask.run()
    }
}