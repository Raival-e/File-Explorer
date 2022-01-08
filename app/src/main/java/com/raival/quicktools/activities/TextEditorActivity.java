package com.raival.quicktools.activities;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.appbar.MaterialToolbar;
import com.raival.quicktools.App;
import com.raival.quicktools.R;
import com.raival.quicktools.utils.FileUtil;

import java.io.File;

import io.github.rosemoe.sora.widget.CodeEditor;
import io.github.rosemoe.sora.widget.EditorColorScheme;
import io.github.rosemoe.sora.widget.schemes.SchemeDarcula;
import io.github.rosemoe.sora.widget.schemes.SchemeGitHub;

public class TextEditorActivity extends AppCompatActivity {
    private CodeEditor editor;
    private File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.text_editor_activity_layout);

        editor = findViewById(R.id.editor);
        Toolbar materialToolbar = findViewById(R.id.toolbar);

        editor.setAutoCompletionEnabled(false);
        editor.setAutoIndentEnabled(false);
        editor.setHardwareAcceleratedDrawAllowed(true);
        editor.setSymbolCompletionEnabled(false);
        editor.setColorScheme(new SchemeDarcula());
        editor.setTextSize(14);

        file = new File(getIntent().getStringExtra("file"));

        materialToolbar.setTitle(file.getName());
        materialToolbar.setSubtitle(file.getAbsolutePath());

        setSupportActionBar(materialToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        materialToolbar.setNavigationOnClickListener(view -> onBackPressed());


        if(!file.exists()){
            App.showMsg("File not found");
            finish();
        }
        if(file.isDirectory()){
            App.showMsg("Invalid file");
            finish();
        }

        try {
            editor.setText(FileUtil.readFile(file));
        } catch (Exception exception) {
            exception.printStackTrace();
            App.showMsg("couldn't read file " + file.getAbsolutePath());
            App.log(exception);
            finish();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.text_editor_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.text_undo) {
            editor.undo();
        } else if (id == R.id.text_redo) {
            editor.redo();
        } else if (id == R.id.text_wordwrap) {
            item.setChecked(!item.isChecked());
            editor.setWordwrap(item.isChecked());
        } else if (id == R.id.magnifier) {
            editor.setMagnifierEnabled(!editor.isMagnifierEnabled());
            item.setChecked(editor.isMagnifierEnabled());
        } else if (id == R.id.editor_line_number) {
            item.setChecked(!item.isChecked());
            editor.setLineNumberEnabled(item.isChecked());
        } else if (id == R.id.pin_line_number) {
            item.setChecked(!item.isChecked());
            editor.setPinLineNumber(item.isChecked());
        } else if (id == R.id.light_mode) {
            item.setChecked(!item.isChecked());
            if(item.isChecked()){
                editor.setColorScheme(new SchemeGitHub());
            } else {
                editor.setColorScheme(new SchemeDarcula());
            }
        }
            return super.onOptionsItemSelected(item);
    }
}
