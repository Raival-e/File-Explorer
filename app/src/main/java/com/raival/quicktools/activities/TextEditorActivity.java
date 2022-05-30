package com.raival.quicktools.activities;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputLayout;
import com.raival.quicktools.App;
import com.raival.quicktools.R;
import com.raival.quicktools.common.BackgroundTask;
import com.raival.quicktools.common.QDialog;
import com.raival.quicktools.exe.java.JavaExecutor;
import com.raival.quicktools.utils.FileUtil;
import com.raival.quicktools.utils.PrefsUtil;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import io.github.rosemoe.sora.langs.java.JavaLanguage;
import io.github.rosemoe.sora.widget.CodeEditor;
import io.github.rosemoe.sora.widget.EditorSearcher;
import io.github.rosemoe.sora.widget.SymbolInputView;
import io.github.rosemoe.sora.widget.component.EditorAutoCompletion;
import io.github.rosemoe.sora.widget.component.Magnifier;
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

        SymbolInputView inputView = findViewById(R.id.symbol_input);
        inputView.bindEditor(editor);
        inputView.setBackgroundColor(getColor(R.color.surface));
        inputView.setTextColor(getColor(R.color.onSurfaceContrast));
        inputView.addSymbols(new String[]{"->", "_", "=", "{", "}", "<", ">", "|", "\\", "?", "+", "-", "*", "/"},
                new String[]{"\t", "_", "=", "{", "}", "<", ">", "|", "\\", "?", "+", "-", "*", "/"});

        editor.setHardwareAcceleratedDrawAllowed(true);
        editor.getComponent(EditorAutoCompletion.class).setEnabled(false);
        editor.setTextSize(14);
        editor.setLigatureEnabled(true);
        editor.setHighlightCurrentBlock(true);

        editor.getProps().symbolPairAutoCompletion = false;
        editor.getProps().deleteMultiSpaces = -1;
        editor.getProps().deleteEmptyLineFast = false;
        editor.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE);
        editor.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);

        loadEditorPrefs();

        file = new File(getIntent().getStringExtra("file"));
        detectLanguage(file);

        materialToolbar.setTitle(file.getName());

        setSupportActionBar(materialToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        materialToolbar.setNavigationOnClickListener(view -> onBackPressed());


        if (!file.exists()) {
            App.showMsg("File not found");
            finish();
        }
        if (file.isDirectory()) {
            App.showMsg("Invalid file");
            finish();
        }

        try {
            editor.setText(FileUtil.readFile(file));
        } catch (Exception exception) {
            exception.printStackTrace();
            App.showMsg("Failed to read file: " + file.getAbsolutePath());
            App.log(exception);
            finish();
        }

        editor.post(() -> {
            if (FileUtil.isEmpty(editor.getText().toString())) {
                if ("Main.java".equalsIgnoreCase(file.getName())) {
                    askToLoadCodeSample();
                }
            }

        });
    }

    private void askToLoadCodeSample() {
        new QDialog()
                .setTitle("Help")
                .setMsg("Do you want to use an executable code sample in this file?")
                .setPositiveButton("Yes", (v) -> editor.setText(getCodeSample()), true)
                .setNegativeButton("No", null, true)
                .showDialog(getSupportFragmentManager(), "");
    }

    private String getCodeSample() {
        try {
            return FileUtil.copyFromInputStream(getAssets().open("java_exe_sample_code.txt"));
        } catch (IOException e) {
            App.log(e);
            App.showWarning("Failed to load sample code");
            return "";
        }
    }

    private void detectLanguage(File file) {
        String ext = FileUtil.getFileExtension(file).toLowerCase();
        switch (ext) {
            case "java":
            case "kt":
                editor.setEditorLanguage(new JavaLanguage());
                editor.setTypefaceText(Typeface.MONOSPACE);
                break;
        }
    }

    private void setupSearchPanel() {
        TextInputLayout findInput = searchPanel.findViewById(R.id.find_input);
        findInput.setHint("Find text");
        TextInputLayout replaceInput = searchPanel.findViewById(R.id.replace_input);
        replaceInput.setHint("Replacement");

        findInput.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                editor.getSearcher().search(editable.toString(), new EditorSearcher.SearchOptions(false, false));
            }
        });

        searchPanel.findViewById(R.id.next).setOnClickListener(view -> editor.getSearcher().gotoNext());
        searchPanel.findViewById(R.id.previous).setOnClickListener(view -> editor.getSearcher().gotoPrevious());

        searchPanel.findViewById(R.id.replace).setOnClickListener(view -> editor.getSearcher().replaceThis(
                replaceInput.getEditText().getText().toString()
        ));
        searchPanel.findViewById(R.id.replace_all).setOnClickListener(view -> editor.getSearcher().replaceAll(
                replaceInput.getEditText().getText().toString()
        ));

    }

    @Override
    public void onBackPressed() {
        if (searchPanel.getVisibility() == View.VISIBLE) {
            searchPanel.setVisibility(View.GONE);
            editor.getSearcher().stopSearch();
            return;
        }
        try {
            if (!FileUtil.readFile(file).equals(editor.getText().toString())) {
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

        menu.findItem(R.id.editor_option_wordwrap).setChecked(PrefsUtil.getTextEditorWordwrap());
        menu.findItem(R.id.editor_option_magnifier).setChecked(PrefsUtil.getTextEditorMagnifier());
        menu.findItem(R.id.editor_option_light_mode).setChecked(PrefsUtil.getTextEditorLightTheme());
        menu.findItem(R.id.editor_option_pin_line_number).setChecked(PrefsUtil.getTextEditorPinLineNumber());
        menu.findItem(R.id.editor_option_line_number).setChecked(PrefsUtil.getTextEditorShowLineNumber());
        menu.findItem(R.id.editor_option_read_only).setChecked(PrefsUtil.getTextEditorReadOnly());

        if ("java".equalsIgnoreCase(FileUtil.getFileExtension(file))) menu.add("Format");
        if ("Main.java".equalsIgnoreCase(file.getName())) menu.add("Execute")
                .setIcon(R.drawable.ic_round_play_arrow_24)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    private void loadEditorPrefs() {
        editor.setPinLineNumber(PrefsUtil.getTextEditorPinLineNumber());
        editor.setWordwrap(PrefsUtil.getTextEditorWordwrap());
        editor.setLineNumberEnabled(PrefsUtil.getTextEditorShowLineNumber());
        editor.getComponent(Magnifier.class).setEnabled(PrefsUtil.getTextEditorMagnifier());
        editor.setColorScheme(PrefsUtil.getTextEditorLightTheme()? new SchemeGitHub() : new SchemeDarcula());
        editor.setEditable(!PrefsUtil.getTextEditorReadOnly());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (item.getTitle().equals("Format")) {
            editor.formatCodeAsync();
        } else if (item.getTitle().equals("Execute")) {
            saveFile(editor.getText().toString());
            if (file.getName().endsWith(".kt")) {
                //TODO: kotlin executor
            } else {
                executeFile();
            }
        } else if (id == R.id.editor_language_def) {
            item.setChecked(true);
            editor.setEditorLanguage(null);
        } else if (id == R.id.editor_language_java) {
            item.setChecked(true);
            editor.setEditorLanguage(new JavaLanguage());

        } else if (id == R.id.editor_language_kotlin) {
            item.setChecked(true);
            editor.setEditorLanguage(new JavaLanguage());
            item.setChecked(true);
        } else if (id == R.id.editor_option_read_only) {
            item.setChecked(!item.isChecked());
            PrefsUtil.setTextEditorReadOnly(item.isChecked());
            editor.setEditable(!item.isChecked());
        } else if (id == R.id.editor_option_search) {
            if (searchPanel.getVisibility() == View.GONE) {
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
            PrefsUtil.setTextEditorWordwrap(item.isChecked());
            editor.setWordwrap(item.isChecked());
        } else if (id == R.id.editor_option_magnifier) {
            item.setChecked(!item.isChecked());
            editor.getComponent(Magnifier.class).setEnabled(item.isChecked());
            PrefsUtil.setTextEditorMagnifier(item.isChecked());
        } else if (id == R.id.editor_option_line_number) {
            item.setChecked(!item.isChecked());
            PrefsUtil.setTextEditorShowLineNumber(item.isChecked());
            editor.setLineNumberEnabled(item.isChecked());
        } else if (id == R.id.editor_option_pin_line_number) {
            item.setChecked(!item.isChecked());
            PrefsUtil.setTextEditorPinLineNumber(item.isChecked());
            editor.setPinLineNumber(item.isChecked());
        } else if (id == R.id.editor_option_light_mode) {
            item.setChecked(!item.isChecked());
            PrefsUtil.setTextEditorLightTheme(item.isChecked());
            if (item.isChecked()) {
                editor.setColorScheme(new SchemeGitHub());
            } else {
                editor.setColorScheme(new SchemeDarcula());
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void executeFile() {
        JavaExecutor javaExecutor = new JavaExecutor(file.getParentFile(), this);
        BackgroundTask backgroundTask = new BackgroundTask();

        AtomicReference<String> error = new AtomicReference<>("");

        backgroundTask.setTasks(() -> {
            backgroundTask.showProgressDialog("compiling files...", this);
        }, () -> {
            try {
                javaExecutor.execute();
            } catch (Exception exception) {
                error.set(App.getStackTrace(exception));
            }
        }, () -> {
            try {
                if (!error.get().equals("")) {
                    backgroundTask.dismiss();
                    App.log(error.get());
                    showDialog("Error", error.get());
                    return;
                }
                javaExecutor.invoke();
                backgroundTask.dismiss();
            } catch (Exception exception) {
                backgroundTask.dismiss();
                App.log(exception);
                showDialog("Error", App.getStackTrace(exception));
            }
        });
        backgroundTask.run();
    }

    private void showDialog(String title, String msg) {
        new QDialog()
                .setTitle(title)
                .setMsg(msg)
                .setPositiveButton("Ok", null, true)
                .showDialog(getSupportFragmentManager(), "");
    }

    private void saveFile(String content) {
        try {
            FileUtil.writeFile(file, content);
        } catch (IOException e) {
            e.printStackTrace();
            App.showMsg("Something went wrong, check app debug for more details");
            App.log(e);
        }

    }
}
