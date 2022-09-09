package com.raival.fileexplorer.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.raival.fileexplorer.App.Companion.showMsg
import com.raival.fileexplorer.R
import com.raival.fileexplorer.activity.adapter.BookmarksAdapter
import com.raival.fileexplorer.activity.model.MainViewModel
import com.raival.fileexplorer.common.view.BottomBarView
import com.raival.fileexplorer.common.view.TabView
import com.raival.fileexplorer.common.view.TabView.OnUpdateTabViewListener
import com.raival.fileexplorer.extension.getAvailableMemoryBytes
import com.raival.fileexplorer.extension.getShortLabel
import com.raival.fileexplorer.extension.getTotalMemoryBytes
import com.raival.fileexplorer.extension.getUsedMemoryBytes
import com.raival.fileexplorer.tab.BaseDataHolder
import com.raival.fileexplorer.tab.BaseTabFragment
import com.raival.fileexplorer.tab.apps.AppsTabDataHolder
import com.raival.fileexplorer.tab.apps.AppsTabFragment
import com.raival.fileexplorer.tab.file.FileExplorerTabDataHolder
import com.raival.fileexplorer.tab.file.FileExplorerTabFragment
import com.raival.fileexplorer.tab.file.misc.FileOpener
import com.raival.fileexplorer.tab.file.misc.FileUtils
import com.raival.fileexplorer.util.PrefsUtils
import com.raival.fileexplorer.util.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : BaseActivity() {
    private var confirmExit = false

    lateinit var tabView: TabView
    lateinit var toolbar: MaterialToolbar
    lateinit var bottomBarView: BottomBarView

    private lateinit var drawerLayout: View
    private lateinit var drawer: DrawerLayout
    private lateinit var drawerStorageSpaceProgress: LinearProgressIndicator
    private lateinit var drawerStorageSpace: TextView
    private lateinit var drawerRootSpaceProgress: LinearProgressIndicator
    private lateinit var drawerRootSpace: TextView
    private lateinit var bookmarksList: RecyclerView
    private lateinit var fragmentContainerView: FragmentContainerView

    private val mainViewModel: MainViewModel
        get() {
            return ViewModelProvider(this)[MainViewModel::class.java]
        }

    private val tabFragments: List<BaseTabFragment>
        get() {
            val list: MutableList<BaseTabFragment> = ArrayList()
            for (fragment in supportFragmentManager.fragments) {
                if (fragment is BaseTabFragment) {
                    list.add(fragment)
                }
            }
            return list
        }

    private val activeFragment: BaseTabFragment
        get() = supportFragmentManager.findFragmentById(R.id.fragment_container) as BaseTabFragment

    /**
     * Called after read & write permissions are granted
     */
    override fun init() {
        if (tabFragments.isEmpty()) {
            loadDefaultTab()
        } else {
            fragmentContainerView.post { restoreTabs() }
        }
    }

    private fun restoreTabs() {
        val activeFragmentTag = activeFragment.tag
        for (i in mainViewModel.getDataHolders().indices) {
            val dataHolder = mainViewModel.getDataHolders()[i]
            // The active fragment will create its own TabView, so we skip it
            if (dataHolder.tag != activeFragmentTag) {
                when (dataHolder) {
                    is FileExplorerTabDataHolder -> {
                        tabView.insertNewTabAt(i, dataHolder.tag, false).setName(
                            dataHolder.activeDirectory!!.getShortLabel(
                                FileExplorerTabFragment.MAX_NAME_LENGTH
                            )
                        )
                    }
                    is AppsTabDataHolder -> {
                        tabView.insertNewTabAt(i, dataHolder.tag, false).setName("Apps")
                    }
                    // handle other types of DataHolders here
                }
            }
        }
    }

    /**
     * The default fragment cannot be deleted, and its tag is unique (starts with "0_")
     */
    private fun loadDefaultTab() {
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.fragment_container,
                FileExplorerTabFragment(),
                BaseTabFragment.DEFAULT_TAB_FRAGMENT_PREFIX + generateRandomTag()
            )
            .setReorderingAllowed(true)
            .commit()
    }

    fun generateRandomTag(): String {
        return Utils.getRandomString(16)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        receiveCommands()

        tabView = findViewById(R.id.tabs)
        fragmentContainerView = findViewById(R.id.fragment_container)
        toolbar = findViewById(R.id.toolbar)
        bottomBarView = findViewById(R.id.bottom_bar_view)
        drawer = findViewById(R.id.drawer)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_round_menu_24)
        toolbar.setNavigationOnClickListener(null)

        val toggle: ActionBarDrawerToggle = object :
            ActionBarDrawerToggle(this, drawer, toolbar, R.string.app_name, R.string.app_name) {
            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
                refreshBookmarks()
            }
        }

        drawer.addDrawerListener(toggle)
        toggle.syncState()

        tabView.setOnUpdateTabViewListener(object : OnUpdateTabViewListener {
            override fun onUpdate(tab: TabView.Tab?, event: Int) {
                if (tab == null) return
                if (event == TabView.ON_SELECT) {
                    if (tab.tag.startsWith(BaseTabFragment.FILE_EXPLORER_TAB_FRAGMENT_PREFIX)
                        || tab.tag.startsWith(BaseTabFragment.DEFAULT_TAB_FRAGMENT_PREFIX)
                    ) {
                        if (supportFragmentManager.findFragmentById(R.id.fragment_container)?.tag != tab.tag) {
                            supportFragmentManager.beginTransaction()
                                .replace(
                                    R.id.fragment_container,
                                    FileExplorerTabFragment(),
                                    tab.tag
                                )
                                .setReorderingAllowed(true)
                                .commit()
                        }
                    }
                    if (tab.tag.startsWith(BaseTabFragment.APPS_TAB_FRAGMENT_PREFIX)) {
                        if (supportFragmentManager.findFragmentById(R.id.fragment_container)?.tag != tab.tag) {
                            supportFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, AppsTabFragment(), tab.tag)
                                .setReorderingAllowed(true)
                                .commit()
                        }
                    }
                    // Handle other types of tabs here...
                } else if (event == TabView.ON_LONG_CLICK) {
                    val popupMenu = PopupMenu(this@MainActivity, tab.view)
                    popupMenu.inflate(R.menu.tab_menu)
                    // Default tab is un-closable
                    if (tab.tag.startsWith(BaseTabFragment.DEFAULT_TAB_FRAGMENT_PREFIX)) {
                        popupMenu.menu.findItem(R.id.close).isVisible = false
                        popupMenu.menu.findItem(R.id.close_all).isVisible = false
                    }
                    popupMenu.setOnMenuItemClickListener { item: MenuItem ->
                        when (item.itemId) {
                            R.id.close -> {
                                val activeFragment = activeFragment
                                if (tab.tag == activeFragment.tag) {
                                    activeFragment.closeTab()
                                } else {
                                    mainViewModel.getDataHolders()
                                        .removeIf { dataHolder1: BaseDataHolder -> dataHolder1.tag == tab.tag }
                                    closeTab(tab.tag)
                                }
                                return@setOnMenuItemClickListener true
                            }
                            R.id.close_all -> {
                                val activeFragment = activeFragment
                                // Remove unselected tabs
                                for (tag in tabView.tags) {
                                    if (!tag.startsWith(BaseTabFragment.DEFAULT_TAB_FRAGMENT_PREFIX) && tag != activeFragment.tag) {
                                        mainViewModel.getDataHolders()
                                            .removeIf { dataHolder1: BaseDataHolder -> dataHolder1.tag == tag }
                                        closeTab(tag)
                                    }
                                }
                                // Remove the active tab
                                activeFragment.closeTab()
                                return@setOnMenuItemClickListener true
                            }
                            R.id.close_others -> {
                                val activeFragment = activeFragment
                                for (tag in tabView.tags) {
                                    if (!tag.startsWith(BaseTabFragment.DEFAULT_TAB_FRAGMENT_PREFIX) && tag != activeFragment.tag && tag != tab.tag) {
                                        mainViewModel.getDataHolders()
                                            .removeIf { dataHolder1: BaseDataHolder -> dataHolder1.tag == tag }
                                        closeTab(tag)
                                    }
                                }
                                if (activeFragment.tag != tab.tag) activeFragment.closeTab()
                                return@setOnMenuItemClickListener true
                            }
                            else -> false
                        }
                    }
                    popupMenu.show()
                }
            }
        })

        findViewById<View>(R.id.tabs_options).setOnClickListener {
            addNewTab(
                FileExplorerTabFragment(),
                BaseTabFragment.FILE_EXPLORER_TAB_FRAGMENT_PREFIX + generateRandomTag()
            )
        }

        checkPermissions()
        setupDrawer()
    }

    private fun receiveCommands() {
        val command = "run_extension"
        val commandValue = "value"

        intent.getStringExtra("command")?.let {
            if (it == command) {
                intent.getStringExtra(commandValue)?.let { path ->
                    FileOpener(this).openFile(File(path))
                    intent.removeExtra(command)
                    intent.removeExtra(commandValue)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val newTheme = PrefsUtils.Settings.themeMode
        if (newTheme != currentTheme) {
            recreate()
        } else {
            bottomBarView.onUpdatePrefs()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun refreshBookmarks() {
        bookmarksList.adapter?.notifyDataSetChanged()
    }

    fun onBookmarkSelected(file: File) {
        if (file.isDirectory) {
            val fragment = FileExplorerTabFragment(file)
            addNewTab(
                fragment,
                BaseTabFragment.FILE_EXPLORER_TAB_FRAGMENT_PREFIX + generateRandomTag()
            )
        } else {
            FileOpener(this).openFile(file)
        }
        if (drawer.isDrawerOpen(drawerLayout)) drawer.close()
    }

    private fun setupDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout)
        drawerStorageSpaceProgress = drawerLayout.findViewById(R.id.storage_space_progress)
        drawerRootSpaceProgress = drawerLayout.findViewById(R.id.root_space_progress)
        drawerRootSpace = drawerLayout.findViewById(R.id.root_space)
        drawerStorageSpace = drawerLayout.findViewById(R.id.storage_space)
        bookmarksList = drawerLayout.findViewById(R.id.rv)

        drawerLayout.findViewById<View>(R.id.apps).setOnClickListener {
            addNewTab(
                AppsTabFragment(),
                BaseTabFragment.APPS_TAB_FRAGMENT_PREFIX + generateRandomTag()
            )
            drawer.close()
        }

        bookmarksList.adapter = BookmarksAdapter(this)

        drawerLayout.findViewById<MaterialToolbar>(R.id.toolbar).apply {
            setTitle(R.string.app_name)
            subtitle = LINK
            menu.apply {
                clear()
                add("GitHub")
                    .setOnMenuItemClickListener { openGithubPage() }
                    .setIcon(R.drawable.ic_baseline_open_in_browser_24)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                add("Settings")
                    .setOnMenuItemClickListener { openSettings() }
                    .setIcon(R.drawable.ic_round_settings_24)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            }
        }

        updateStorageSpace()
        updateRootSpace()
    }

    private fun openSettings(): Boolean {
        startActivity(Intent().setClass(this, SettingsActivity::class.java))
        return true
    }

    private fun openGithubPage(): Boolean {
        startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse(LINK)))
        return true
    }

    @SuppressLint("SetTextI18n")
    private fun updateRootSpace() {
        val used = Environment.getRootDirectory().getUsedMemoryBytes()
        val total = Environment.getRootDirectory().getTotalMemoryBytes()
        val available = Environment.getRootDirectory().getAvailableMemoryBytes()

        drawerRootSpaceProgress.progress = (used.toDouble() / total.toDouble() * 100).toInt()
        drawerRootSpace.text = (FileUtils.getFormattedSize(used)
                + " used, "
                + FileUtils.getFormattedSize(available)
                + " available")
    }

    @SuppressLint("SetTextI18n")
    private fun updateStorageSpace() {
        val used = Environment.getExternalStorageDirectory().getUsedMemoryBytes()
        val total = Environment.getExternalStorageDirectory().getTotalMemoryBytes()
        val available = Environment.getExternalStorageDirectory().getAvailableMemoryBytes()

        drawerStorageSpaceProgress.progress = (used.toDouble() / total.toDouble() * 100).toInt()
        drawerStorageSpace.text = (FileUtils.getFormattedSize(used)
                + " used, "
                + FileUtils.getFormattedSize(available)
                + " available")
    }

    fun addNewTab(fragment: BaseTabFragment, tag: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment, tag)
            .setReorderingAllowed(true)
            .commit()
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        (menu as MenuBuilder).setOptionalIconsVisible(true)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val title = item.title.toString()
        if (title == "Logs") {
            showLogFile()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showLogFile() {
        val logFile = File(getExternalFilesDir(null)!!.absolutePath + "/debug/log.txt")
        if (logFile.exists() && logFile.isFile) {
            val intent = Intent()
            intent.setClass(this, TextEditorActivity::class.java)
            intent.putExtra("file", logFile.absolutePath)
            startActivity(intent)
            return
        }
        showMsg("No logs found")
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen(drawerLayout)) {
            drawer.close()
            return
        }
        if (activeFragment.onBackPressed()) {
            return
        }
        if (!confirmExit) {
            confirmExit = true
            showMsg("Press again to exit")
            CoroutineScope(Dispatchers.Main).launch {
                delay(2000)
                confirmExit = false
            }
            return
        }
        super.onBackPressed()
    }

    fun closeTab(tag: String) {
        // Remove the tab from TabView. TabView will select another tab which will replace the corresponding fragment.
        // The DataHolder must be removed by the fragment itself, as deletion process differs for each tab.

        // Default fragment (the one added when the app is opened) won't be closed.
        if (tag.startsWith("0_")) return
        tabView.removeTab(tag)
    }

    companion object {
        private const val LINK = "https://github.com/Raival-e/File-Explorer"
    }
}