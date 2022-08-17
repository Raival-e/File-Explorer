package com.raival.fileexplorer.tab.file

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import com.raival.fileexplorer.App.Companion.showMsg
import com.raival.fileexplorer.R
import com.raival.fileexplorer.activity.MainActivity
import com.raival.fileexplorer.common.dialog.CustomDialog
import com.raival.fileexplorer.extension.getFormattedFileCount
import com.raival.fileexplorer.extension.getShortLabel
import com.raival.fileexplorer.extension.toDp
import com.raival.fileexplorer.tab.BaseDataHolder
import com.raival.fileexplorer.tab.BaseTabFragment
import com.raival.fileexplorer.tab.file.adapter.FileListAdapter
import com.raival.fileexplorer.tab.file.adapter.PathHistoryAdapter
import com.raival.fileexplorer.tab.file.dialog.SearchDialog
import com.raival.fileexplorer.tab.file.dialog.TasksDialog
import com.raival.fileexplorer.tab.file.misc.FileOpener
import com.raival.fileexplorer.tab.file.misc.FileUtils
import com.raival.fileexplorer.tab.file.model.FileItem
import com.raival.fileexplorer.tab.file.options.FileOptionsHandler
import com.raival.fileexplorer.util.Log
import com.raival.fileexplorer.util.PrefsUtils
import java.io.File
import java.io.IOException
import java.util.*

class FileExplorerTabFragment : BaseTabFragment {
    val files = ArrayList<FileItem>()
    var pathHistory = ArrayList<File>()

    private lateinit var fileList: RecyclerView
    private lateinit var pathHistoryRv: RecyclerView
    private lateinit var placeHolder: View
    private lateinit var fileOptionsHandler: FileOptionsHandler
    private var requireRefresh = false
    var previousDirectory: File? = null

    @JvmField
    var currentDirectory: File? = null

    constructor() : super()
    constructor(directory: File) : super() {
        currentDirectory = directory
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.file_explorer_tab_fragment, container, false)
        fileList = view.findViewById(R.id.rv)
        pathHistoryRv = view.findViewById(R.id.path_history)
        placeHolder = view.findViewById(R.id.place_holder)
        val homeButton = view.findViewById<View>(R.id.home)
        homeButton.setOnClickListener {
            setCurrentDirectory(defaultHomeDirectory)
        }
        homeButton.setOnLongClickListener { showSetPathDialog() }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        prepareBottomBarView()
        initFileList()
        loadData()
        // restore RecyclerView state
        restoreRecyclerViewState()
    }

    private fun loadData() {
        setCurrentDirectory((dataHolder as FileExplorerTabDataHolder).activeDirectory!!)
    }

    private fun prepareBottomBarView() {
        bottomBarView!!.clear()
        bottomBarView!!.addItem("Tasks", R.drawable.ic_baseline_assignment_24) {
            TasksDialog(this).show(
                parentFragmentManager, ""
            )
        }
        bottomBarView!!.addItem("Search", R.drawable.ic_round_search_24) {
            val searchFragment = SearchDialog(this, currentDirectory!!)
            searchFragment.show(parentFragmentManager, "")
            setSelectAll(false)
        }
        bottomBarView!!.addItem(
            "Create",
            R.drawable.ic_baseline_add_24
        ) { showAddNewFileDialog() }
        bottomBarView!!.addItem(
            "Sort",
            R.drawable.ic_baseline_sort_24
        ) { view: View -> showSortOptionsMenu(view) }
        bottomBarView!!.addItem(
            "Select All",
            R.drawable.ic_baseline_select_all_24
        ) { setSelectAll(true) }
        bottomBarView!!.addItem(
            "refresh",
            R.drawable.ic_baseline_restart_alt_24
        ) { refresh() }
    }

    private fun showSortOptionsMenu(view: View) {
        val popupMenu = PopupMenu(requireActivity(), view)
        popupMenu.menu.add("Sort by:").isEnabled = false
        popupMenu.menu.add("Name (A-Z)").setCheckable(true).isChecked =
            PrefsUtils.FileExplorerTab.sortingMethod == PrefsUtils.SORT_NAME_A2Z
        popupMenu.menu.add("Name (Z-A)").setCheckable(true).isChecked =
            PrefsUtils.FileExplorerTab.sortingMethod == PrefsUtils.SORT_NAME_Z2A
        popupMenu.menu.add("Size (Bigger)").setCheckable(true).isChecked =
            PrefsUtils.FileExplorerTab.sortingMethod == PrefsUtils.SORT_SIZE_BIGGER
        popupMenu.menu.add("Size (Smaller)").setCheckable(true).isChecked =
            PrefsUtils.FileExplorerTab.sortingMethod == PrefsUtils.SORT_SIZE_SMALLER
        popupMenu.menu.add("Date (Newer)").setCheckable(true).isChecked =
            PrefsUtils.FileExplorerTab.sortingMethod == PrefsUtils.SORT_DATE_NEWER
        popupMenu.menu.add("Date (Older)").setCheckable(true).isChecked =
            PrefsUtils.FileExplorerTab.sortingMethod == PrefsUtils.SORT_DATE_OLDER
        popupMenu.menu.add("Other options:").isEnabled = false
        popupMenu.menu.add("Folders first").setCheckable(true).isChecked =
            PrefsUtils.FileExplorerTab.listFoldersFirst()
        popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
            menuItem.isChecked = !menuItem.isChecked
            when (menuItem.title.toString()) {
                "Name (A-Z)" -> {
                    PrefsUtils.FileExplorerTab.sortingMethod = PrefsUtils.SORT_NAME_A2Z
                }
                "Name (Z-A)" -> {
                    PrefsUtils.FileExplorerTab.sortingMethod = PrefsUtils.SORT_NAME_Z2A
                }
                "Size (Bigger)" -> {
                    PrefsUtils.FileExplorerTab.sortingMethod = PrefsUtils.SORT_SIZE_BIGGER
                }
                "Size (Smaller)" -> {
                    PrefsUtils.FileExplorerTab.sortingMethod = PrefsUtils.SORT_SIZE_SMALLER
                }
                "Date (Older)" -> {
                    PrefsUtils.FileExplorerTab.sortingMethod = PrefsUtils.SORT_DATE_OLDER
                }
                "Date (Newer)" -> {
                    PrefsUtils.FileExplorerTab.sortingMethod = PrefsUtils.SORT_DATE_NEWER
                }
                "Folders first" -> {
                    PrefsUtils.FileExplorerTab.setListFoldersFirst(menuItem.isChecked)
                }
            }
            refresh()
            true
        }
        popupMenu.show()
    }

    @SuppressLint("SetTextI18n")
    private fun showSetPathDialog(): Boolean {
        val customDialog = CustomDialog()
        val input = customDialog.createInput(requireActivity(), "File path")
        input.editText?.setSingleLine()
        val textView = MaterialTextView(requireContext())
        textView.setPadding(0, 8.toDp(), 0, 0)
        textView.alpha = 0.7f
        textView.text = "Quick Links:"
        val layout = ChipGroup(requireContext())
        val internal = Chip(requireContext())
        internal.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        internal.text = "Internal Data Files"
        internal.setOnClickListener {
            setCurrentDirectory(requireActivity().filesDir)
            customDialog.dismiss()
        }
        val external = Chip(requireContext())
        external.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        external.text = "External Data Files"
        external.setOnClickListener {
            setCurrentDirectory(requireActivity().getExternalFilesDir(null)!!)
            customDialog.dismiss()
        }
        layout.addView(internal)
        layout.addView(external)
        customDialog.setTitle("Jump to path")
            .addView(input)
            .addView(textView)
            .addView(layout)
            .setPositiveButton("Go", {
                val file = File(
                    input.editText!!.text.toString()
                )
                if (file.exists()) {
                    if (file.canRead()) {
                        if (file.isFile) {
                            FileOpener(requireActivity() as MainActivity).openFile(file)
                        } else {
                            setCurrentDirectory(file)
                        }
                    } else {
                        showMsg(Log.UNABLE_TO + " read the provided file")
                    }
                } else {
                    showMsg("The destination path doesn't exist!")
                }
            }, true)
            .show(parentFragmentManager, "")
        return true
    }

    private fun showAddNewFileDialog() {
        val customDialog = CustomDialog()
        val input = customDialog.createInput(requireActivity(), "File name")

        input.editText?.setSingleLine()
        FileUtils.setFileValidator(input, currentDirectory!!)
        CustomDialog()
            .setTitle("Create new file")
            .addView(input)
            .setPositiveButton("File", {
                createFile(
                    input.editText!!.text.toString(), false
                )
            }, true)
            .setNegativeButton("Folder", {
                createFile(
                    input.editText!!.text.toString(), true
                )
            }, true)
            .setNeutralButton("Cancel", null, true)
            .show(parentFragmentManager, "")
    }

    private fun createFile(name: String, isFolder: Boolean) {
        val file = File(currentDirectory, name)
        if (isFolder) {
            if (!file.mkdir()) {
                showMsg(Log.UNABLE_TO + " create folder: " + file.absolutePath)
            } else {
                refresh()
                focusOn(file)
            }
        } else {
            try {
                if (!file.createNewFile()) {
                    showMsg(Log.UNABLE_TO + " " + FileUtils.CREATE_FILE + ": " + file.absolutePath)
                } else {
                    refresh()
                    focusOn(file)
                }
            } catch (e: IOException) {
                Log.e(TAG, e)
                showMsg(e.toString())
            }
        }
    }

    override fun closeTab() {
        // Close the tab (if not default tab)
        if (tag != null && !tag!!.startsWith("0_")) {
            super.closeTab()
        }
    }

    override fun onBackPressed(): Boolean {
        //  Unselect selected files (if any)
        if (selectedFiles.size > 0) {
            setSelectAll(false)
            return true
        }
        // Go back if possible
        val parent = currentDirectory?.parentFile
        if (parent != null && parent.exists() && parent.canRead()) {
            setCurrentDirectory(currentDirectory?.parentFile!!)
            // restore RecyclerView state
            restoreRecyclerViewState()
            // Clear any selected files from the DataHolder (it also gets cleared
            // when a FileItem is clicked)
            (dataHolder as FileExplorerTabDataHolder).selectedFiles.clear()
            return true
        }
        // Close the tab (if not default tab)
        if (tag != null && !tag!!.startsWith("0_")) {
            closeTab()
            return true
        }
        return false
    }

    override fun createNewDataHolder(): BaseDataHolder {
        val dataHolder = FileExplorerTabDataHolder(tag!!)
        dataHolder.activeDirectory =
            if (currentDirectory == null) defaultHomeDirectory else currentDirectory
        return dataHolder
    }

    override fun onStop() {
        super.onStop()
        requireRefresh = true
    }

    override fun onPause() {
        super.onPause()
        requireRefresh = true
    }

    override fun onResume() {
        super.onResume()
        if (requireRefresh) {
            requireRefresh = false
            refresh()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setSelectAll(select: Boolean) {
        if (!select) (dataHolder as FileExplorerTabDataHolder).selectedFiles.clear()
        for (item in files) {
            item.isSelected = select
            if (select) {
                (dataHolder as FileExplorerTabDataHolder).selectedFiles.add(item.file)
            }
        }
        // Don't call refresh(), because it will recreate the tab and reset the selection
        fileList.adapter?.notifyDataSetChanged()
    }

    val selectedFiles: ArrayList<FileItem>
        get() {
            val list = ArrayList<FileItem>()
            for (item in files) {
                if (item.isSelected) list.add(item)
            }
            return list
        }

    /**
     * Show/Hide placeholder
     */
    fun showPlaceholder(isShow: Boolean) {
        placeHolder.visibility = if (isShow) View.VISIBLE else View.GONE
    }

    /**
     * Used to update the title of attached tabView
     */
    private fun updateTabTitle() {
        getTabView()!!.setName(name)
    }

    /**
     * This method is called once from #onViewCreated(View, Bundle)
     */
    private fun initFileList() {
        fileList.adapter = FileListAdapter(this)
        fileList.setHasFixedSize(true)
        initPathHistory()
    }

    private fun initPathHistory() {
        pathHistoryRv.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        pathHistoryRv.adapter = PathHistoryAdapter(this)
    }

    /**
     * RecyclerView state should be saved when the fragment is destroyed and recreated.
     * #getDataHolder() isn't used here because we don't want to create a new DataHolder if the fragment is about
     * to close (note that the DataHolder gets removed just right before the fragment is closed)
     */
    override fun onDestroy() {
        super.onDestroy()
        val fileExplorerTabDataHolder = mainViewModel!!.getDataHolder(
            tag!!
        ) as FileExplorerTabDataHolder?
        fileExplorerTabDataHolder?.recyclerViewStates?.put(
            currentDirectory!!, fileList.layoutManager!!
                .onSaveInstanceState()!!
        )
    }

    /**
     * This method handles the following (in order):
     * - Updating currentDirectory and previousDirectory fields
     * - Updating recyclerViewStates in DataHolder
     * - Sorting files based on the preferences
     * - Updating tabView title
     * - Update pathHistory list
     * - Refreshing adapters (fileList & pathHistory)
     * - Updating activeDirectory in DataHolder
     *
     * @param file the directory to open
     */
    fun setCurrentDirectory(file: File) {
        if (currentDirectory != null) previousDirectory = currentDirectory
        currentDirectory = file
        // Save only when previousDirectory is set (so that it can restore the state before onDestroy())
        if (previousDirectory != null) (dataHolder as FileExplorerTabDataHolder).recyclerViewStates[previousDirectory!!] =
            fileList.layoutManager!!
                .onSaveInstanceState()!!
        prepareFiles()
        updateTabTitle()
        updatePathHistoryList()
        refreshFileList()
        (dataHolder as FileExplorerTabDataHolder).activeDirectory = currentDirectory
    }

    /**
     * This method automatically removes the restored state from DataHolder recyclerViewStates
     * This method is called when:
     * - Create the fragment
     * - #onBackPressed()
     * - when select a directory from pathHistory RecyclerView
     */
    fun restoreRecyclerViewState() {
        val savedState =
            (dataHolder as FileExplorerTabDataHolder).recyclerViewStates[currentDirectory]
        if (savedState != null) {
            fileList.layoutManager?.onRestoreInstanceState(savedState)
            (dataHolder as FileExplorerTabDataHolder).recyclerViewStates.remove(currentDirectory)
        }
    }

    /**
     * Refreshes both fileList and pathHistory recyclerview (used by #setCurrentDirectory(File) ONLY)
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun refreshFileList() {
        fileList.adapter?.notifyDataSetChanged()
        pathHistoryRv.adapter?.notifyDataSetChanged()
        pathHistoryRv.scrollToPosition(pathHistoryRv.adapter!!.itemCount - 1)
        fileList.scrollToPosition(0)
        if (toolbar != null) toolbar!!.subtitle =
            currentDirectory?.getFormattedFileCount()
    }

    /**
     * Used to refresh the tab
     */
    fun refresh() {
        setCurrentDirectory(currentDirectory!!)
        restoreRecyclerViewState()
    }

    private val defaultHomeDirectory: File
        get() = Environment.getExternalStorageDirectory()

    private fun prepareFiles() {
        // Make sure current file is ready
        if (currentDirectory == null) {
            loadData()
            return
        }
        // Clear previous list
        files.clear()
        // Load all files in the current File
        val files = currentDirectory!!.listFiles()
        if (files != null) {
            for (comparator in FileUtils.comparators) {
                Arrays.sort(files, comparator)
            }
            for (file in files) {
                val fileItem = FileItem(file)
                if ((dataHolder as FileExplorerTabDataHolder).selectedFiles.contains(fileItem.file)) {
                    fileItem.isSelected = true
                }
                this.files.add(fileItem)
            }
        }
    }

    fun focusOn(file: File) {
        for (i in files.indices) {
            if (file == files[i].file) {
                fileList.scrollToPosition(i)
                return
            }
        }
    }

    /**
     * @return the name associated with this tab (currently used for tabView)
     */
    val name: String
        get() = currentDirectory?.getShortLabel(MAX_NAME_LENGTH)!!

    fun showFileOptions(fileItem: FileItem?) {
        if (!this::fileOptionsHandler.isInitialized) {
            fileOptionsHandler = FileOptionsHandler(this)
        }
        fileOptionsHandler.showOptions(fileItem!!)
    }

    fun openFile(fileItem: FileItem) {
        FileOpener(requireActivity() as MainActivity).openFile(fileItem.file)
    }

    fun showDialog(title: String?, msg: String?) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title!!)
            .setMessage(msg!!)
            .setPositiveButton("Ok", null)
            .show()
    }

    private fun updatePathHistoryList() {
        val list = ArrayList<File>()
        var file = currentDirectory
        while (file != null && file.canRead()) {
            list.add(file)
            file = file.parentFile
        }
        list.reverse()
        if (list.size > 0) pathHistory = list
    }

    companion object {
        const val MAX_NAME_LENGTH = 26
        private const val TAG = "FileExplorerTabFragment"
    }
}