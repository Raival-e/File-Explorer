package com.raival.fileexplorer.activities;

import android.os.Bundle;

import com.dvdb.materialchecklist.MaterialChecklist;
import com.google.android.material.appbar.MaterialToolbar;
import com.raival.fileexplorer.App;
import com.raival.fileexplorer.R;
import com.raival.fileexplorer.utils.FileUtil;

import java.io.File;
import java.io.IOException;

public class CheckListActivity extends BaseActivity {
    File file;
    MaterialChecklist materialChecklist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checklist_activity_layout);

        materialChecklist = findViewById(R.id.checklist);

        if (getIntent().hasExtra("file")) {
            file = new File(getIntent().getStringExtra("file"));
        } else {
            App.showMsg("the file is missing");
            finish();
        }

        setTitle(FileUtil.getFileNameWithoutExtension(file));
        setupToolbar(findViewById(R.id.toolbar));

        try {
            materialChecklist.setItems(FileUtil.readFile(file));
        } catch (Exception e) {
            App.showMsg("couldn't load checklist content");
            e.printStackTrace();
        }
    }

    private void setupToolbar(MaterialToolbar toolbar) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        toolbar.setNavigationOnClickListener((v) -> super.onBackPressed());
    }

    @Override
    protected void onStop() {
        try {
            FileUtil.writeFile(file, materialChecklist.getItems(true, true));
        } catch (IOException e) {
            App.showMsg("couldn't save items");
            e.printStackTrace();
        }
        super.onStop();
    }
}
