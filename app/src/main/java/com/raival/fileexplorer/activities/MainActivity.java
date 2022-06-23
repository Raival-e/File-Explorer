package com.raival.fileexplorer.activities;

import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.pixplicity.easyprefs.library.Prefs;
import com.raival.fileexplorer.App;
import com.raival.fileexplorer.R;
import com.raival.fileexplorer.activities.model.MainViewModel;
import com.raival.fileexplorer.common.view.BottomBarView;
import com.raival.fileexplorer.common.view.TabView;
import com.raival.fileexplorer.tabs.BaseDataHolder;
import com.raival.fileexplorer.tabs.BaseTabFragment;
import com.raival.fileexplorer.tabs.checklist.ChecklistTabDataHolder;
import com.raival.fileexplorer.tabs.checklist.ChecklistTabFragment;
import com.raival.fileexplorer.tabs.file.FileExplorerTabFragment;
import com.raival.fileexplorer.tabs.file.FileExplorerTabDataHolder;
import com.raival.fileexplorer.utils.FileUtils;
import com.raival.fileexplorer.utils.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {
    private TabView tabView;
    private FragmentContainerView fragmentContainerView;
    private MaterialToolbar toolbar;
    private BottomBarView bottomBarView;
    private final boolean confirmExit = false;

    /**
     * Called after read & write permissions are granted
     */
    @Override
    public void init() {
        initPrefs();
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

    private void restoreTabs() {
        final String activeFragmentTag = getTabFragments().get(0).getTag();
        MainViewModel mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        for (int i = 0; i < mainViewModel.getDataHolders().size(); i++) {
            BaseDataHolder dataHolder = mainViewModel.getDataHolders().get(i);
            if (!dataHolder.getTag().equals(activeFragmentTag)) {
                // Handle FileExplorerTabDataHolder
                if (dataHolder instanceof FileExplorerTabDataHolder) {
                    tabView.insertNewTabAt(i, dataHolder.getTag(), false)
                            .setName(FileUtils.getShortLabel(((FileExplorerTabDataHolder) dataHolder).activeDirectory, FileExplorerTabFragment.MAX_NAME_LENGTH));
                } else if (dataHolder instanceof ChecklistTabDataHolder) {
                    tabView.insertNewTabAt(i, dataHolder.getTag(), false)
                            .setName(FileUtils.getShortLabel(((ChecklistTabDataHolder) dataHolder).file, FileExplorerTabFragment.MAX_NAME_LENGTH));
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
        return TextUtils.getRandomString(16);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        tabView = findViewById(R.id.tabs);
        fragmentContainerView = findViewById(R.id.fragment_container);
        toolbar = findViewById(R.id.toolbar);
        bottomBarView = findViewById(R.id.bottom_bar_view);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_round_menu_24);
        toolbar.setNavigationOnClickListener(null);

        tabView.setOnUpdateTabViewListener((tab, event) -> {
            if (event == TabView.ON_SELECT) {
                if (tab.tag.startsWith("FileExplorerTabFragment_") || tab.tag.startsWith("0_FileExplorerTabFragment_")) {
                    if (!getSupportFragmentManager().findFragmentById(R.id.fragment_container).getTag().equals(tab.tag)) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, new FileExplorerTabFragment(), tab.tag)
                                .setReorderingAllowed(true)
                                .commit();
                    }
                }
                if (tab.tag.startsWith("ChecklistTabFragment_")) {
                    if (!getSupportFragmentManager().findFragmentById(R.id.fragment_container).getTag().equals(tab.tag)) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, new ChecklistTabFragment(), tab.tag)
                                .setReorderingAllowed(true)
                                .commit();
                    }
                }
                // Handle other types here...
            }
        });

        findViewById(R.id.tabs_options).setOnClickListener(view -> {
            addNewTab(new FileExplorerTabFragment(), "FileExplorerTabFragment_" + generateRandomTag());
        });

        checkPermissions();
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
        BaseTabFragment fragment = (BaseTabFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment != null) {
            if (fragment.onBackPressed()) {
                return;
            }
        }
        super.onBackPressed();
    }

    private void initPrefs() {
        new Prefs.Builder()
                .setContext(this)
                .setPrefsName("Prefs")
                .setMode(ContextWrapper.MODE_PRIVATE)
                .build();
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