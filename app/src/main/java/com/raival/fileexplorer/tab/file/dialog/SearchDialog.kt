package com.raival.fileexplorer.tab.file.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputLayout
import com.raival.fileexplorer.R
import com.raival.fileexplorer.extension.getFileDetails
import com.raival.fileexplorer.tab.file.FileExplorerTabDataHolder
import com.raival.fileexplorer.tab.file.FileExplorerTabFragment
import com.raival.fileexplorer.tab.file.misc.IconHelper
import com.raival.fileexplorer.tab.file.model.FileItem
import com.raival.fileexplorer.util.Log
import com.raival.fileexplorer.util.PrefsUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import org.apache.commons.io.LineIterator
import java.io.File
import java.util.regex.Pattern

class SearchDialog : BottomSheetDialogFragment {
    private val tab: FileExplorerTabFragment
    private val filesToSearchIn: ArrayList<File>
    private lateinit var recyclerView: RecyclerView
    private lateinit var input: TextInputLayout
    private lateinit var deepSearch: CheckBox
    private lateinit var optimizedSearching: CheckBox
    private lateinit var regEx: CheckBox
    private lateinit var suffix: CheckBox
    private lateinit var prefix: CheckBox
    private lateinit var searchButton: Button
    private lateinit var progress: ProgressBar
    private lateinit var fileCount: TextView
    private lateinit var query: String
    private var active = false

    constructor(tab: FileExplorerTabFragment, directory: File) {
        filesToSearchIn = ArrayList()
        filesToSearchIn.add(directory)
        this.tab = tab
    }

    constructor(tab: FileExplorerTabFragment, files: ArrayList<File>) {
        this.tab = tab
        filesToSearchIn = files
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        return inflater.inflate(R.layout.search_fragment, container, false)
    }

    override fun getTheme(): Int {
        return R.style.ThemeOverlay_Material3_BottomSheetDialog
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.rv)
        input = view.findViewById(R.id.input)
        deepSearch = view.findViewById(R.id.search_option_deep_search)
        optimizedSearching = view.findViewById(R.id.search_option_optimized_searching)
        regEx = view.findViewById(R.id.search_option_regex)
        suffix = view.findViewById(R.id.search_option_suffix)
        prefix = view.findViewById(R.id.search_option_prefix)
        searchButton = view.findViewById(R.id.search_button)
        progress = view.findViewById(R.id.progress)
        fileCount = view.findViewById(R.id.file_count)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = RecyclerViewAdapter()
        progress.visibility = View.GONE
        searchButton.setOnClickListener {
            if (active) {
                searchButton.text = "Search"
                progress.visibility = View.GONE
                recyclerView.adapter?.notifyDataSetChanged()
                active = false
                isCancelable = true
            } else {
                isCancelable = false
                searchButton.text = "Stop"
                (tab.dataHolder as FileExplorerTabDataHolder?)!!.searchList.clear()
                recyclerView.adapter?.notifyDataSetChanged()
                progress.visibility = View.VISIBLE
                loseFocus(input)
                active = true
                query = input.editText?.text.toString()
                CoroutineScope(Dispatchers.IO).launch {
                    for (file in filesToSearchIn) {
                        if (!active) break
                        searchIn(
                            file,
                            deepSearch.isChecked,
                            optimizedSearching.isChecked,
                            regEx.isChecked,
                            prefix.isChecked,
                            suffix.isChecked
                        )
                    }
                    withContext(Dispatchers.Main) {
                        searchButton.text = "Search"
                        progress.visibility = View.GONE
                        recyclerView.adapter!!.notifyDataSetChanged()
                        active = false
                        isCancelable = true
                        updateFileCount()
                    }
                }
            }
        }
        if ((tab.dataHolder as FileExplorerTabDataHolder?)!!.searchList.size > 0) {
            fileCount.visibility = View.VISIBLE
            updateFileCount()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateFileCount() {
        fileCount.text =
            (tab.dataHolder as FileExplorerTabDataHolder?)!!.searchList.size.toString() + " results found"
    }

    private fun searchIn(
        file: File,
        isDeepSearch: Boolean,
        optimized: Boolean,
        useRegex: Boolean,
        startWith: Boolean,
        endWith: Boolean
    ) {
        if (file.isFile) {
            if (isDeepSearch) {
                if (PrefsUtils.Settings.deepSearchFileSizeLimit >= file.length()) {
                    if (optimized) {
                        var lineIterator: LineIterator? = null
                        try {
                            lineIterator = FileUtils.lineIterator(file)
                            while (lineIterator.hasNext()) {
                                if (useRegex) {
                                    if (Pattern.compile(query).matcher(lineIterator.nextLine())
                                            .find()
                                    ) {
                                        addFileItem(file)
                                        break
                                    }
                                } else {
                                    if (lineIterator.nextLine().contains(query)) {
                                        addFileItem(file)
                                        break
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, e)
                        } finally {
                            LineIterator.closeQuietly(lineIterator)
                        }
                    } else {
                        try {
                            if (useRegex) {
                                if (Pattern.compile(query).matcher(file.readText()).find()) {
                                    addFileItem(file)
                                }
                            } else {
                                if (file.readText().contains(query)) {
                                    addFileItem(file)
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, e)
                        }
                    }
                }
            } else {
                if (startWith) {
                    if (file.name.startsWith(query)) addFileItem(file)
                } else if (endWith) {
                    if (file.name.endsWith(query)) addFileItem(file)
                } else {
                    if (file.name.contains(query)) addFileItem(file)
                }
            }
        } else {
            val children = file.listFiles()
            if (children != null) {
                for (child in children) {
                    if (!active) break
                    searchIn(child, isDeepSearch, optimized, useRegex, startWith, endWith)
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun addFileItem(file: File) {
        (tab.dataHolder as FileExplorerTabDataHolder?)!!.searchList.add(FileItem(file))
        recyclerView.post {
            updateFileCount()
            recyclerView.adapter?.notifyDataSetChanged()
        }
    }

    private fun loseFocus(view: View) {
        view.isEnabled = false
        view.isEnabled = true
    }

    private inner class RecyclerViewAdapter :
        RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater =
                requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            @SuppressLint("InflateParams") val view =
                inflater.inflate(R.layout.file_explorer_tab_file_item, null)
            val lp = RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            view.layoutParams = lp
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind()
        }

        override fun getItemCount(): Int {
            return (tab.dataHolder as FileExplorerTabDataHolder?)!!.searchList.size
        }

        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            var name: TextView
            var details: TextView
            var icon: ImageView
            var background: View
            fun bind() {
                val position = adapterPosition
                val fileItem = (tab.dataHolder as FileExplorerTabDataHolder?)!!.searchList[position]
                name.text = fileItem.file.name
                details.text = fileItem.file.getFileDetails()
                IconHelper.setFileIcon(icon, fileItem.file)
                icon.alpha = if (fileItem.file.isHidden) 0.5f else 1f
                background.setOnClickListener {
                    tab.currentDirectory = fileItem.file.parentFile!!
                    tab.refresh()
                    tab.focusOn(fileItem.file)
                    dismiss()
                }
            }

            init {
                name = v.findViewById(R.id.file_name)
                details = v.findViewById(R.id.file_details)
                icon = v.findViewById(R.id.file_icon)
                background = v.findViewById(R.id.background)
            }
        }
    }

    companion object {
        private const val TAG = "Search Dialog"
    }
}