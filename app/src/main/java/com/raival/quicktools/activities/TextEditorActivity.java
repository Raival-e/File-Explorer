package com.raival.quicktools.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputLayout;
import com.raival.quicktools.App;
import com.raival.quicktools.R;
import com.raival.quicktools.common.QDialog;
import com.raival.quicktools.utils.FileUtil;

import java.io.File;
import java.io.IOException;

import io.github.rosemoe.sora.langs.css3.CSS3Language;
import io.github.rosemoe.sora.langs.html.HTMLLanguage;
import io.github.rosemoe.sora.langs.java.JavaLanguage;
import io.github.rosemoe.sora.langs.python.PythonLanguage;
import io.github.rosemoe.sora.widget.CodeEditor;
import io.github.rosemoe.sora.widget.schemes.SchemeDarcula;
import io.github.rosemoe.sora.widget.schemes.SchemeGitHub;

public class TextEditorActivity extends AppCompatActivity {
    private CodeEditor editor;
    private File file;
    private View searchPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.text_editor_activity_layout);

        editor = findViewById(R.id.editor);
        Toolbar materialToolbar = findViewById(R.id.toolbar);
        searchPanel = findViewById(R.id.search_panel);
        setupSearchPanel();

        editor.setAutoCompletionEnabled(false);
        editor.setAutoIndentEnabled(false);
        editor.setHardwareAcceleratedDrawAllowed(true);
        editor.setSymbolCompletionEnabled(false);
        editor.setColorScheme(new SchemeDarcula());
        editor.setTextSize(14);

        file = new File(getIntent().getStringExtra("file"));

        materialToolbar.setTitle(file.getName());

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

    private void setupSearchPanel() {
        TextInputLayout findInput = searchPanel.findViewById(R.id.find_input);
        findInput.setHint("Find text");
        TextInputLayout replaceInput = searchPanel.findViewById(R.id.replace_input);
        replaceInput.setHint("Replacement");

        findInput.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                editor.getSearcher().search(editable.toString());
            }
        });

        searchPanel.findViewById(R.id.next).setOnClickListener(view -> editor.getSearcher().gotoNext());
        searchPanel.findViewById(R.id.previous).setOnClickListener(view -> editor.getSearcher().gotoLast());

        searchPanel.findViewById(R.id.replace).setOnClickListener(view -> editor.getSearcher().replaceThis(
                replaceInput.getEditText().getText().toString()
        ));
        searchPanel.findViewById(R.id.replace_all).setOnClickListener(view -> editor.getSearcher().replaceAll(
                replaceInput.getEditText().getText().toString()
        ));

    }

    @Override
    public void onBackPressed(){
        if(searchPanel.getVisibility()==View.VISIBLE){
            searchPanel.setVisibility(View.GONE);
            editor.getSearcher().stopSearch();
            return;
        }
        try {
            if(!FileUtil.readFile(file).equals(editor.getText().toString())){
                new QDialog()
                        .setTitle("Save File")
                        .setMsg("Do you want to save this file before exit?")
                        .setPositiveButton("Yes", view -> {
                            saveFile(editor.getText().toString());
                            finish();
                        }, true)
                        .setNegativeButton("No", view -> finish(), true)
                        .showDialog(getSupportFragmentManager(), "");
                return;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        super.onBackPressed();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.text_editor_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.editor_language_def) {
            item.setChecked(true);
            editor.setEditorLanguage(null);
        } else if (id == R.id.editor_language_java) {
            item.setChecked(true);
            editor.setEditorLanguage(new JavaLanguage());
        } else if (id == R.id.editor_language_python) {
            item.setChecked(true);
            editor.setEditorLanguage(new PythonLanguage());
            item.setChecked(true);
        } else if (id == R.id.editor_language_css3) {
            item.setChecked(true);
            editor.setEditorLanguage(new CSS3Language());
        } else if (id == R.id.editor_language_html) {
            item.setChecked(true);
            editor.setEditorLanguage(new HTMLLanguage());
        } else if (id == R.id.editor_option_read_only) {
            item.setChecked(!item.isChecked());
            editor.setEditable(!item.isChecked());
        } else if (id == R.id.editor_option_search) {
            if(searchPanel.getVisibility()==View.GONE){
                searchPanel.setVisibility(View.VISIBLE);
            } else {
                searchPanel.setVisibility(View.GONE);
                editor.getSearcher().stopSearch();
            }
        } else if (id == R.id.editor_option_save) {
            saveFile(editor.getText().toString());
            App.showMsg("Saved successfully");
        } else if (id == R.id.editor_option_text_undo) {
            editor.undo();
        } else if (id == R.id.editor_option_text_redo) {
            editor.redo();
        } else if (id == R.id.editor_option_wordwrap) {
            item.setChecked(!item.isChecked());
            editor.setWordwrap(item.isChecked());
        } else if (id == R.id.editor_option_magnifier) {
            editor.setMagnifierEnabled(!editor.isMagnifierEnabled());
            item.setChecked(editor.isMagnifierEnabled());
        } else if (id == R.id.editor_option_line_number) {
            item.setChecked(!item.isChecked());
            editor.setLineNumberEnabled(item.isChecked());
        } else if (id == R.id.editor_option_pin_line_number) {
            item.setChecked(!item.isChecked());
            editor.setPinLineNumber(item.isChecked());
        } else if (id == R.id.editor_option_light_mode) {
            item.setChecked(!item.isChecked());
            if(item.isChecked()){
                editor.setColorScheme(new SchemeGitHub());
            } else {
                editor.setColorScheme(new SchemeDarcula());
            }
        }
            return super.onOptionsItemSelected(item);
    }

    private void saveFile(String content) {
        try{
            FileUtil.writeFile(file, content);
        } catch (IOException e) {
            e.printStackTrace();
            App.showMsg("Something went wrong, check app debug for more details");
            App.log(e);
        }

    }
}
