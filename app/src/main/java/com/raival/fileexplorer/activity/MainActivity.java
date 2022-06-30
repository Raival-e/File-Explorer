package com.raival.fileexplorer.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.PopupMenu;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.raival.fileexplorer.App;
import com.raival.fileexplorer.R;
import com.raival.fileexplorer.activity.model.MainViewModel;
import com.raival.fileexplorer.common.view.BottomBarView;
import com.raival.fileexplorer.common.view.TabView;
import com.raival.fileexplorer.tab.BaseDataHolder;
import com.raival.fileexplorer.tab.BaseTabFragment;
import com.raival.fileexplorer.tab.apps.AppsTabDataHolder;
import com.raival.fileexplorer.tab.apps.AppsTabFragment;
import com.raival.fileexplorer.tab.checklist.ChecklistTabDataHolder;
import com.raival.fileexplorer.tab.checklist.ChecklistTabFragment;
import com.raival.fileexplorer.tab.file.FileExplorerTabDataHolder;
import com.raival.fileexplorer.tab.file.FileExplorerTabFragment;
import com.raival.fileexplorer.tab.file.util.FileUtils;
import com.raival.fileexplorer.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends BaseActivity {
    private boolean confirmExit = false;
    private TabView tabView;
    private FragmentContainerView fragmentContainerView;
    private MaterialToolbar toolbar;
    private BottomBarView bottomBarView;
    private MainViewModel mainViewModel;

    private DrawerLayout drawer;
    private LinearProgressIndicator drawer_storageSpaceProgress;
    private TextView drawer_storageSpace;
    private LinearProgressIndicator drawer_rootSpaceProgress;
    private TextView drawer_rootSpace;

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
                } else if (dataHolder instanceof ChecklistTabDataHolder) {
                    tabView.insertNewTabAt(i, dataHolder.getTag(), false)
                            .setName(FileUtils.getShortLabel(((ChecklistTabDataHolder) dataHolder).file, FileExplorerTabFragment.MAX_NAME_LENGTH));
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
                .replace(R.id.fragment_container, new FileExplorerTabFragment(), "0_FileExplorerTabFragment_" + generateRandomTag())
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

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.app_name, R.string.app_name);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        tabView.setOnUpdateTabViewListener((tab, event) -> {
            if (event == TabView.ON_SELECT) {
                if (tab.tag.startsWith("FileExplorerTabFragment_") || tab.tag.startsWith("0_FileExplorerTabFragment_")) {
                    if (!Objects.equals(Objects.requireNonNull(getSupportFragmentManager().findFragmentById(R.id.fragment_container)).getTag(), tab.tag)) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, new FileExplorerTabFragment(), tab.tag)
                                .setReorderingAllowed(true)
                                .commit();
                    }
                }
                if (tab.tag.startsWith("ChecklistTabFragment_")) {
                    if (!Objects.equals(Objects.requireNonNull(getSupportFragmentManager().findFragmentById(R.id.fragment_container)).getTag(), tab.tag)) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, new ChecklistTabFragment(), tab.tag)
                                .setReorderingAllowed(true)
                                .commit();
                    }
                }
                if (tab.tag.startsWith("AppsTabFragment_")) {
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
                if (tab.tag.startsWith("0_")) {
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
                            if (!tag.startsWith("0_") && !tag.equals(activeFragment.getTag())) {
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
                            if (!tag.startsWith("0_") && !tag.equals(activeFragment.getTag()) && !tag.equals(tab.tag)) {
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

        findViewById(R.id.tabs_options).setOnClickListener(view -> addNewTab(new FileExplorerTabFragment(), "FileExplorerTabFragment_" + generateRandomTag()));

        checkPermissions();
        setupDrawer();
    }

    private void setupDrawer() {
        View drawerLayout = findViewById(R.id.drawer_layout);

        drawer_storageSpaceProgress = drawerLayout.findViewById(R.id.storage_space_progress);
        drawer_rootSpaceProgress = drawerLayout.findViewById(R.id.root_space_progress);
        drawer_rootSpace = drawerLayout.findViewById(R.id.root_space);
        drawer_storageSpace = drawerLayout.findViewById(R.id.storage_space);

        drawerLayout.findViewById(R.id.apps).setOnClickListener((v -> {
            addNewTab(new AppsTabFragment(), "AppsTabFragment_" + generateRandomTag());
            drawer.close();
        }));

        MaterialToolbar materialToolbar = drawerLayout.findViewById(R.id.toolbar);
        materialToolbar.setTitle(R.string.app_name);
        materialToolbar.setSubtitle("https://github.com/Raival-e/File-Explorer");
        materialToolbar.getMenu().clear();
        materialToolbar.getMenu().add("Settings")
                .setIcon(R.drawable.ic_round_settings_24)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        updateStorageSpace();
        updateRootSpace();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
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