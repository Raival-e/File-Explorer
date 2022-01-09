package com.raival.quicktools;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textfield.TextInputLayout;
import com.pixplicity.easyprefs.library.Prefs;
import com.raival.quicktools.common.QDialog;
import com.raival.quicktools.common.TasksDialog;
import com.raival.quicktools.interfaces.QTab;
import com.raival.quicktools.interfaces.QTask;
import com.raival.quicktools.tabs.normal.NormalTab;
import com.raival.quicktools.utils.FileUtil;
import com.raival.quicktools.utils.PrefsUtil;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    TabLayout tabLayout;
    ViewPager2 viewPager2;
    RecyclerView pathTreeView;

    ArrayList<QTab> tabs = new ArrayList<>();
    ArrayList<QTask> tasks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(savedInstanceState != null) savedInstanceState.clear();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabLayout = findViewById(R.id.tabs);
        viewPager2 = findViewById(R.id.view_pager);
        pathTreeView = findViewById(R.id.path_treeview);

        viewPager2.setUserInputEnabled(false);

        initPrefs();

        if(grantStoragePermissions()){
           init();
        }
    }

    @Override
    public void onBackPressed(){
        if(!getCurrentTab().onBackPressed()){
            if(viewPager2.getCurrentItem() != 0){
                closeTabAt(viewPager2.getCurrentItem());
                return;
            }
            super.onBackPressed();
        }
    }

    // this doesn't close the app.
    public void simulateOnBackPressed(){
        if(!getCurrentTab().onBackPressed()){
            if(viewPager2.getCurrentItem() != 0){
                closeTabAt(viewPager2.getCurrentItem());
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private boolean grantStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if(!Environment.isExternalStorageManager()){
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.fromParts("package", getPackageName(), null));
                startActivity(intent);
                return false;
            }
        } else {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
            || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 9011);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 9011){
            init();
        }
    }

    private void init(){
        AddDefaultTab();
        initTabs();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) { }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                showTabsOptionsMenu();
            }
        });

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                try {
                    getCurrentTab().refresh();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        });

        initBottomBar();

        setListeners();
    }

    private void setListeners() {
        findViewById(R.id.home).setOnClickListener(view -> getCurrentTab().onTreeViewPathSelected(0));
        findViewById(R.id.tasks).setOnClickListener(view -> showTasksDialog());
    }

    private void showTasksDialog() {
        TasksDialog tasksDialog = new TasksDialog(tasks, getCurrentTab());
        tasksDialog.show(getSupportFragmentManager(), "tasks_dialog");
    }

    public void updatePathTreeView(){
        if(pathTreeView.getAdapter() == null){
            initPathTreeViewRV();
            return;
        }
        pathTreeView.getAdapter().notifyDataSetChanged();
        pathTreeView.scrollToPosition(pathTreeView.getAdapter().getItemCount()-1);
    }

    private void initPathTreeViewRV() {
        pathTreeView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        pathTreeView.setAdapter(new PathTreeViewRvAdapter());
    }

    private void initPrefs() {
        new Prefs.Builder()
                .setContext(this)
                .setPrefsName("Prefs")
                .setMode(ContextWrapper.MODE_PRIVATE)
                .build();
    }

    private void initBottomBar() {
        findViewById(R.id.option_back).setOnClickListener(view -> simulateOnBackPressed());
        findViewById(R.id.option_select_all).setOnClickListener(view -> getCurrentTab().selectAll());
        findViewById(R.id.option_refresh).setOnClickListener(view -> {
            getCurrentTab().refresh();
            ObjectAnimator.ofFloat(viewPager2, "alpha", 1.0f, 0.1f, 1f).setDuration(300).start();
        });
        findViewById(R.id.option_sort).setOnClickListener(this::showSortOptionsMenu);
        findViewById(R.id.option_add).setOnClickListener(view -> showAddNewFileDialog());
    }

    private void showAddNewFileDialog() {
        if(!getCurrentTab().canCreateFile()){
            App.showMsg("Creating files isn't possible here");
            return;
        }

        TextInputLayout input = (TextInputLayout) getLayoutInflater().inflate(R.layout.input, null, false);
        input.setHint("File name");

        new QDialog()
                .setTitle("Create new file")
                .addView(input)
                .setPositiveButton("File", view ->
                getCurrentTab()
                        .createFile(input.getEditText().getText().toString(), false), true)
                .setNegativeButton("Folder", view ->
                getCurrentTab()
                        .createFile(input.getEditText().getText().toString(), true), true)
                .setNeutralButton("Cancel", null, true)
                .show(getSupportFragmentManager(), "");
    }

    private void showSortOptionsMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);

        popupMenu.getMenu().add("Sort by:").setEnabled(false);

        popupMenu.getMenu().add("Name (A-Z)").setCheckable(true).setChecked(PrefsUtil.getSortingMethod()==PrefsUtil.SORT_NAME_A2Z);
        popupMenu.getMenu().add("Name (Z-A)").setCheckable(true).setChecked(PrefsUtil.getSortingMethod()==PrefsUtil.SORT_NAME_Z2A);

        popupMenu.getMenu().add("Size (Bigger)").setCheckable(true).setChecked(PrefsUtil.getSortingMethod()==PrefsUtil.SORT_SIZE_BIGGER);
        popupMenu.getMenu().add("Size (Smaller)").setCheckable(true).setChecked(PrefsUtil.getSortingMethod()==PrefsUtil.SORT_SIZE_SMALLER);

        popupMenu.getMenu().add("Date (Newer)").setCheckable(true).setChecked(PrefsUtil.getSortingMethod()==PrefsUtil.SORT_DATE_NEWER);
        popupMenu.getMenu().add("Date (Older)").setCheckable(true).setChecked(PrefsUtil.getSortingMethod()==PrefsUtil.SORT_DATE_OLDER);

        popupMenu.getMenu().add("Other options:").setEnabled(false);

        popupMenu.getMenu().add("Folders first").setCheckable(true).setChecked(PrefsUtil.listFoldersFirst());

        popupMenu.setOnMenuItemClickListener(menuItem -> {
            menuItem.setChecked(!menuItem.isChecked());
            switch (menuItem.getTitle().toString()){
                case "Name (A-Z)":{
                    PrefsUtil.setSortingMethod(PrefsUtil.SORT_NAME_A2Z);
                    break;
                }
                case "Name (Z-A)":{
                    PrefsUtil.setSortingMethod(PrefsUtil.SORT_NAME_Z2A);
                    break;
                }
                case "Size (Bigger)":{
                    PrefsUtil.setSortingMethod(PrefsUtil.SORT_SIZE_BIGGER);
                    break;
                }
                case "Size (Smaller)":{
                    PrefsUtil.setSortingMethod(PrefsUtil.SORT_SIZE_SMALLER);
                    break;
                }
                case "Date (Older)":{
                    PrefsUtil.setSortingMethod(PrefsUtil.SORT_DATE_OLDER);
                    break;
                }
                case "Date (Newer)":{
                    PrefsUtil.setSortingMethod(PrefsUtil.SORT_DATE_NEWER);
                    break;
                }
                case "Folders first":{
                    PrefsUtil.setListFoldersFirst(menuItem.isChecked());
                    break;
                }
            }
            getCurrentTab().refresh();
            return true;
        });
        popupMenu.show();
    }

    private void AddDefaultTab() {
        findViewById(R.id.tabs_options).setOnClickListener(view -> showTabsOptionsMenu());
        tabs.add(new NormalTab(Environment.getExternalStorageDirectory()));
    }

    private void showTabsOptionsMenu() {
        PopupMenu popupMenu = new PopupMenu(this, (View)findViewById(R.id.tabs_options).getParent());

        popupMenu.getMenu().add("Add new tab");
        if(getCurrentTab() instanceof NormalTab){
            popupMenu.getMenu().add("Clone tab");
        }

        if(tabs.size() > 1){
            popupMenu.getMenu().add("Close tab");
            popupMenu.getMenu().add("Close others");
        }

        popupMenu.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getTitle().toString()){
                case "Add new tab":{
                    addNewTab(Environment.getExternalStorageDirectory());
                    return true;
                }
                case "Clone tab":{
                    addNewTab(((NormalTab)getCurrentTab()).getCurrentPath());
                    return true;
                }
                case "Close tab":{
                    closeTabAt(viewPager2.getCurrentItem());
                    return true;
                }
                case "Close others":{
                    closeOtherTabs(viewPager2.getCurrentItem());
                    return true;
                }
                default:{
                    return false;
                }
            }
        });
        popupMenu.show();
    }

    private void closeOtherTabs(int currentItem) {
        QTab q = tabs.get(currentItem);
        tabs = new ArrayList<>();
        tabs.add(q);
        initTabs();
    }

    private void closeTabAt(int currentItem) {
        tabs.remove(currentItem);
        reInitTabs();
        if(currentItem >= tabs.size()){
            viewPager2.setCurrentItem(tabs.size()-1, false);
        } else if(currentItem != 0){
            viewPager2.setCurrentItem(currentItem - 1, false);
        }
    }

    private void reInitTabs() {
        initTabs();
    }

    private void linkTabs() {
        for(int i = 0; i < tabs.size(); i++){
            tabs.get(i).setTab(tabLayout.getTabAt(i));
        }
    }

    public void addNewTab(File path) {
        int i = viewPager2.getCurrentItem() + 1;
        tabs.add(i, new NormalTab(path));
        reInitTabs();
        viewPager2.setCurrentItem(i, true);
    }

    private void initTabs() {
        viewPager2.setAdapter(new TabsFragmentAdapter(this));
        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {
            String tabName = tabs.get(position).getName();
            tab.setText(tabName);
        }).attach();
        linkTabs();
    }
    
    private QTab getCurrentTab(){
        return tabs.get(viewPager2.getCurrentItem());
    }

    public void setPageSubtitle(String subtitle){
        ((TextView)findViewById(R.id.subtitle)).setText(subtitle);
    }

    public void AddTask(QTask task) {
        tasks.add(task);
    }

    public class TabsFragmentAdapter extends FragmentStateAdapter {

        public TabsFragmentAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return tabs.get(position).getFragment();
        }

        @Override
        public int getItemCount() {
            return tabs.size();
        }
    }

    public class PathTreeViewRvAdapter extends RecyclerView.Adapter<PathTreeViewRvAdapter.ViewHolder>{
        public PathTreeViewRvAdapter(){ }
        @NonNull
        @Override
        public PathTreeViewRvAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(getLayoutInflater().inflate(R.layout.path_tree_view, null));
        }

        @Override
        public void onBindViewHolder(@NonNull PathTreeViewRvAdapter.ViewHolder holder, int position) {
            holder.bind();
        }

        @Override
        public int getItemCount() {
            return getCurrentTab().getTreeViewList().size();
        }

        private class ViewHolder extends RecyclerView.ViewHolder{
            TextView label;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                label = itemView.findViewById(R.id.text);
            }

            public void bind() {
                final int position = getAdapterPosition();
                label.setText(FileUtil.isExternalStorageFolder(getCurrentTab().getTreeViewList().get(position))
                        ? FileUtil.INTERNAL_STORAGE
                        : getCurrentTab().getTreeViewList().get(position).getName());
                label.setTextColor((position==getItemCount()-1)
                        ? getResources().getColor(R.color.orange, getTheme())
                        : getResources().getColor(R.color.onSurfaceContrast, getTheme()));
                itemView.setOnClickListener(view -> getCurrentTab().onTreeViewPathSelected(position));
            }
        }
    }
}