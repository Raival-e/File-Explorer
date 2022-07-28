package com.raival.fileexplorer.tab.file.observer

import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.raival.fileexplorer.tab.file.FileExplorerTabFragment
import com.raival.fileexplorer.tab.file.adapter.FileListAdapter

class FileListObserver(
    private val parentFragment: FileExplorerTabFragment,
    private val fileListAdapter: FileListAdapter?
) : AdapterDataObserver() {

    override fun onChanged() {
        super.onChanged()
        checkIfEmpty()
    }

    private fun checkIfEmpty() {
        parentFragment.showPlaceholder(fileListAdapter != null && fileListAdapter.itemCount == 0)
    }

    init {
        checkIfEmpty()
    }
}