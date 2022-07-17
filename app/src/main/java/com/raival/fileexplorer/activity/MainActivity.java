package com.raival.fileexplorer.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.PopupMenu;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.raival.fileexplorer.App;
import com.raival.fileexplorer.R;
import com.raival.fileexplorer.activity.adapter.BookmarksAdapter;
import com.raival.fileexplorer.activity.model.MainViewModel;
import com.raival.fileexplorer.common.view.BottomBarView;
import com.raival.fileexplorer.common.view.TabView;
import com.raival.fileexplorer.tab.BaseDataHolder;
import com.raival.fileexplorer.tab.BaseTabFragment;
import com.raival.fileexplorer.tab.apps.AppsTabDataHolder;
import com.raival.fileexplorer.tab.apps.AppsTabFragment;
import com.raival.fileexplorer.tab.file.FileExplorerTabDataHolder;
import com.raival.fileexplorer.tab.file.FileExplorerTabFragment;
import com.raival.fileexplorer.tab.file.util.FileOpener;
import com.raival.fileexplorer.tab.file.util.FileUtils;
import com.raival.fileexplorer.util.PrefsUtils;
import com.raival.fileexplorer.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends BaseActivity {
    private static final String LINK = "https://github.com/Raival-e/File-Explorer";
    private boolean confirmExit = false;
    private TabView tabView;
    private FragmentContainerView fragmentContainerView;
    private MaterialToolbar toolbar;
    private BottomBarView bottomBarView;
    private MainViewModel mainViewModel;

    private View drawerLayout;
    private DrawerLayout drawer;
    private LinearProgressIndicator drawer_storageSpaceProgress;
    private TextView drawer_storageSpace;
    private LinearProgressIndicator drawer_rootSpaceProgress;
    private TextView drawer_rootSpace;
    private RecyclerView bookmarksList;

    /**
     * Called after read & write permissions are granted
     */
    @Override
    public void init() {
        if (getTabFragments().isEmpty()) {
            loadDefaultTab();
        } else {
            fragmentContainerView.post(this::restoreTabs);
        }
    }

    private List<BaseTabFragment> getTabFragments() {
        List<BaseTabFragment> list = new ArrayList<>();
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment instanceof BaseTabFragment) {
                list.add((BaseTabFragment) fragment);
            }
        }
        return list;
    }

    private MainViewModel getMainViewModel() {
        if (mainViewModel == null) {
            mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        }
        return mainViewModel;
    }

    private void restoreTabs() {
        final String activeFragmentTag = getActiveFragment().getTag();

        for (int i = 0; i < getMainViewModel().getDataHolders().size(); i++) {
            BaseDataHolder dataHolder = getMainViewModel().getDataHolders().get(i);
            // The active fragment will create its own TabView, so we skip it
            if (!dataHolder.getTag().equals(activeFragmentTag)) {
                if (dataHolder instanceof FileExplorerTabDataHolder) {
                    tabView.insertNewTabAt(i, dataHolder.getTag(), false)
                            .setName(FileUtils.getShortLabel(((FileExplorerTabDataHolder) dataHolder).activeDirectory, FileExplorerTabFragment.MAX_NAME_LENGTH));
                } else if (dataHolder instanceof AppsTabDataHolder) {
                    tabView.insertNewTabAt(i, dataHolder.getTag(), false).setName("Apps");
                }
                // handle other types of DataHolders here
            }
        }
    }

    private void loadDefaultTab() {
        getSupportFragmentManager().beginTransaction()
                // This fragment cannot be deleted, and its tag is unique (starts with "0_")
                .replace(R.id.fragment_container, new FileExplorerTabFragment(), BaseTabFragment.DEFAULT_TAB_FRAGMENT_PREFIX + generateRandomTag())
                .setReorderingAllowed(true)
                .commit();
    }

    public String generateRandomTag() {
        return Utils.getRandomString(16);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        tabView = findViewById(R.id.tabs);
        fragmentContainerView = findViewById(R.id.fragment_container);
        toolbar = findViewById(R.id.toolbar);
        bottomBarView = findViewById(R.id.bottom_bar_view);
        drawer = findViewById(R.id.drawer);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_round_menu_24);
        toolbar.setNavigationOnClickListener(null);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.app_name, R.string.app_name) {
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                refreshBookmarks();
            }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        tabView.setOnUpdateTabViewListener((tab, event) -> {
            if (event == TabView.ON_SELECT) {
                if (tab.tag.startsWith(BaseTabFragment.FILE_EXPLORER_TAB_FRAGMENT_PREFIX) || tab.tag.startsWith(BaseTabFragment.DEFAULT_TAB_FRAGMENT_PREFIX)) {
                    if (!Objects.equals(Objects.requireNonNull(getSupportFragmentManager().findFragmentById(R.id.fragment_container)).getTag(), tab.tag)) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, new FileExplorerTabFragment(), tab.tag)
                                .setReorderingAllowed(true)
                                .commit();
                    }
                }
                if (tab.tag.startsWith(BaseTabFragment.APPS_TAB_FRAGMENT_PREFIX)) {
                    if (!Objects.equals(Objects.requireNonNull(getSupportFragmentManager().findFragmentById(R.id.fragment_container)).getTag(), tab.tag)) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, new AppsTabFragment(), tab.tag)
                                .setReorderingAllowed(true)
                                .commit();
                    }
                }
                // Handle other types of tabs here...
            } else if (event == TabView.ON_LONG_CLICK) {
                PopupMenu popupMenu = new PopupMenu(this, tab.view);
                popupMenu.inflate(R.menu.tab_menu);
                // Default tab is un-closable
                if (tab.tag.startsWith(BaseTabFragment.DEFAULT_TAB_FRAGMENT_PREFIX)) {
                    popupMenu.getMenu().findItem(R.id.close).setVisible(false);
                    popupMenu.getMenu().findItem(R.id.close_all).setVisible(false);
                }

                popupMenu.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.close) {
                        BaseTabFragment activeFragment = getActiveFragment();
                        if (tab.tag.equals(activeFragment.getTag())) {
                            activeFragment.closeTab();
                        } else {
                            getMainViewModel().getDataHolders().removeIf(dataHolder1 -> dataHolder1.getTag().equals(tab.tag));
                            closeTab(tab.tag);
                        }
                        return true;
                    } else if (item.getItemId() == R.id.close_all) {
                        BaseTabFragment activeFragment = getActiveFragment();
                        // Remove unselected tabs
                        for (String tag : tabView.getTags()) {
                            if (!tag.startsWith(BaseTabFragment.DEFAULT_TAB_FRAGMENT_PREFIX) && !tag.equals(activeFragment.getTag())) {
                                getMainViewModel().getDataHolders().removeIf(dataHolder1 -> dataHolder1.getTag().equals(tag));
                                closeTab(tag);
                            }
                        }
                        // Remove the active tab
                        activeFragment.closeTab();
                        return true;
                    } else if (item.getItemId() == R.id.close_others) {
                        BaseTabFragment activeFragment = getActiveFragment();
                        for (String tag : tabView.getTags()) {
                            if (!tag.startsWith(BaseTabFragment.DEFAULT_TAB_FRAGMENT_PREFIX) && !tag.equals(activeFragment.getTag()) && !tag.equals(tab.tag)) {
                                getMainViewModel().getDataHolders().removeIf(dataHolder1 -> dataHolder1.getTag().equals(tag));
                                closeTab(tag);
                            }
                        }
                        if (!Objects.requireNonNull(activeFragment.getTag()).equals(tab.tag))
                            activeFragment.closeTab();
                        return true;
                    }
                    return false;
                });
                popupMenu.show();
            }
        });

        findViewById(R.id.tabs_options).setOnClickListener(view -> addNewTab(new FileExplorerTabFragment(), BaseTabFragment.FILE_EXPLORER_TAB_FRAGMENT_PREFIX + generateRandomTag()));

        checkPermissions();
        setupDrawer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        final String newTheme = PrefsUtils.Settings.getThemeMode();
        if (!newTheme.equals(currentTheme)) {
            recreate();
        } else {
            bottomBarView.onUpdatePrefs();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void refreshBookmarks() {
        Objects.requireNonNull(bookmarksList.getAdapter()).notifyDataSetChanged();
    }

    public void onBookmarkSelected(File file) {
        if (file.isDirectory()) {
            FileExplorerTabFragment fragment = new FileExplorerTabFragment(file);
            addNewTab(fragment, BaseTabFragment.FILE_EXPLORER_TAB_FRAGMENT_PREFIX + generateRandomTag());
        } else {
            new FileOpener(this).openFile(file);
        }
        if (drawer.isDrawerOpen(drawerLayout)) drawer.close();
    }

    private void setupDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout);

        drawer_storageSpaceProgress = drawerLayout.findViewById(R.id.storage_space_progress);
        drawer_rootSpaceProgress = drawerLayout.findViewById(R.id.root_space_progress);
        drawer_rootSpace = drawerLayout.findViewById(R.id.root_space);
        drawer_storageSpace = drawerLayout.findViewById(R.id.storage_space);
        bookmarksList = drawerLayout.findViewById(R.id.rv);

        drawerLayout.findViewById(R.id.apps).setOnClickListener((v -> {
            addNewTab(new AppsTabFragment(), BaseTabFragment.APPS_TAB_FRAGMENT_PREFIX + generateRandomTag());
            drawer.close();
        }));

        bookmarksList.setAdapter(new BookmarksAdapter(this));

        MaterialToolbar materialToolbar = drawerLayout.findViewById(R.id.toolbar);
        materialToolbar.setTitle(R.string.app_name);
        materialToolbar.setSubtitle(LINK);
        materialToolbar.getMenu().clear();
        materialToolbar.getMenu().add("GitHub")
                .setOnMenuItemClickListener(item -> openGithubPage())
                .setIcon(R.drawable.ic_baseline_open_in_browser_24)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        materialToolbar.getMenu().add("Settings")
                .setOnMenuItemClickListener(item -> openSettings())
                .setIcon(R.drawable.ic_round_settings_24)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        updateStorageSpace();
        updateRootSpace();
    }

    private boolean openSettings() {
        startActivity(new Intent().setClass(this, SettingsActivity.class));
        return true;
    }

    private boolean openGithubPage() {
        startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(LINK)));
        return true;
    }

    @SuppressLint("SetTextI18n")
    private void updateRootSpace() {
        final long used = FileUtils.getUsedMemoryBytes(Environment.getRootDirectory());
        final long total = FileUtils.getTotalMemoryBytes(Environment.getRootDirectory());
        drawer_rootSpaceProgress.setProgress((int) ((double) used / (double) total * 100));

        drawer_rootSpace.setText(
                FileUtils.getFormattedSize(FileUtils.getUsedMemoryBytes(Environment.getRootDirectory()))
                        + " used, "
                        + FileUtils.getFormattedSize(FileUtils.getAvailableMemoryBytes(Environment.getRootDirectory()))
                        + " available"
        );
    }

    @SuppressLint("SetTextI18n")
    private void updateStorageSpace() {
        final long used = FileUtils.getUsedMemoryBytes(Environment.getExternalStorageDirectory());
        final long total = FileUtils.getTotalMemoryBytes(Environment.getExternalStorageDirectory());
        drawer_storageSpaceProgress.setProgress((int) ((double) used / (double) total * 100));

        drawer_storageSpace.setText(
                FileUtils.getFormattedSize(FileUtils.getUsedMemoryBytes(Environment.getExternalStorageDirectory()))
                        + " used, "
                        + FileUtils.getFormattedSize(FileUtils.getAvailableMemoryBytes(Environment.getExternalStorageDirectory()))
                        + " available"
        );
    }

    private BaseTabFragment getActiveFragment() {
        return (BaseTabFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
    }

    public void addNewTab(BaseTabFragment fragment, String tag) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment, tag)
                .setReorderingAllowed(true)
                .commit();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        ((MenuBuilder) menu).setOptionalIconsVisible(true);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        final String title = item.getTitle().toString();
        if (title.equals("Logs")) {
            showLogFile();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showLogFile() {
        final File logFile = new File(getExternalFilesDir(null).getAbsolutePath() + "/debug/log.txt");
        if (logFile.exists() && logFile.isFile()) {
            Intent intent = new Intent();
            intent.setClass(this, TextEditorActivity.class);
            intent.putExtra("file", logFile.getAbsolutePath());
            startActivity(intent);
            return;
        }
        App.showMsg("No logs found");
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(drawerLayout)) {
            drawer.close();
            return;
        }
        if (getActiveFragment().onBackPressed()) {
            return;
        }
        if (!confirmExit) {
            confirmExit = true;
            App.showMsg("Press again to exit");
            App.appHandler.postDelayed(() -> confirmExit = false, 2000);
            return;
        }
        super.onBackPressed();
    }

    public MaterialToolbar getToolbar() {
        return toolbar;
    }

    public BottomBarView getBottomBarView() {
        return bottomBarView;
    }

    public TabView getTabView() {
        return tabView;
    }

    public void closeTab(String tag) {
        // Remove the tab from TabView. TabView will select another tab which will replace the corresponding fragment.
        // The DataHolder must be removed by the fragment itself, as deletion process differs for each tab.

        // Default fragment (the one added when the app is opened) won't be closed.
        if (tag.startsWith("0_")) return;
        tabView.removeTab(tag);
    }
}