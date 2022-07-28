package com.raival.fileexplorer.tab

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.appbar.MaterialToolbar
import com.raival.fileexplorer.activity.MainActivity
import com.raival.fileexplorer.activity.model.MainViewModel
import com.raival.fileexplorer.common.view.BottomBarView
import com.raival.fileexplorer.common.view.TabView

/**
 * Each TabFragment must handle the creation of its DataHolder and the related TabView using
 * the provided APIs or custom ones.
 */
abstract class BaseTabFragment : Fragment() {
    var mainViewModel: MainViewModel? = null
        get() {
            if (field == null) {
                field = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
            }
            return field
        }
        private set

    var bottomBarView: BottomBarView? = null
        get() {
            if (field == null) field = (requireActivity() as MainActivity).bottomBarView
            return field
        }
        private set

    var toolbar: MaterialToolbar? = null
        get() {
            if (field == null) field = (requireActivity() as MainActivity).toolbar
            return field
        }
        private set

    private var tabView: TabView.Tab? = null

    var dataHolder: BaseDataHolder? = null
        get() {
            if (field == null && mainViewModel!!.getDataHolder(tag!!).also { field = it } == null) {
                field = createNewDataHolder()
                mainViewModel!!.addDataHolder(field!!)
            }
            return field
        }
        private set

    abstract fun onBackPressed(): Boolean
    abstract fun createNewDataHolder(): BaseDataHolder?

    fun getTabView(): TabView.Tab? {
        if (tabView == null && (requireActivity() as MainActivity).tabView.getTabByTag(tag)
                .also { tabView = it } == null
        ) {
            tabView = (requireActivity() as MainActivity).tabView.addNewTab(tag!!)
            // set Default name
            tabView!!.setName("Untitled")
        }
        return tabView
    }

    open fun closeTab() {
        mainViewModel!!.getDataHolders()
            .removeIf { dataHolder1: BaseDataHolder -> dataHolder1.tag == tag }
        (requireActivity() as MainActivity).closeTab(tag!!)
    }

    override fun onResume() {
        super.onResume()
        // create TabView if necessary (important when a tab fragment doesn't update its TabView)
        getTabView()
    }

    companion object {
        const val DEFAULT_TAB_FRAGMENT_PREFIX = "0_FileExplorerTabFragment_"
        const val FILE_EXPLORER_TAB_FRAGMENT_PREFIX = "FileExplorerTabFragment_"
        const val APPS_TAB_FRAGMENT_PREFIX = "AppsTabFragment_"
    }
}