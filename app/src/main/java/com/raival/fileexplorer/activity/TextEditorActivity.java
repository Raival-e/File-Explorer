package com.raival.fileexplorer.activity;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.elevation.SurfaceColors;
import com.google.android.material.textfield.TextInputLayout;
import com.raival.fileexplorer.App;
import com.raival.fileexplorer.R;
import com.raival.fileexplorer.activity.editor.autocomplete.CustomCompletionItemAdapter;
import com.raival.fileexplorer.activity.editor.autocomplete.CustomCompletionLayout;
import com.raival.fileexplorer.activity.editor.scheme.DarkScheme;
import com.raival.fileexplorer.activity.editor.scheme.LightScheme;
import com.raival.fileexplorer.activity.model.TextEditorViewModel;
import com.raival.fileexplorer.common.BackgroundTask;
import com.raival.fileexplorer.common.dialog.CustomDialog;
import com.raival.fileexplorer.tab.file.executor.Executor;
import com.raival.fileexplorer.util.FileUtils;
import com.raival.fileexplorer.util.PrefsUtils;
import com.raival.fileexplorer.util.Utils;

import org.eclipse.tm4e.core.internal.theme.reader.ThemeReader;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import io.github.rosemoe.sora.lang.EmptyLanguage;
import io.github.rosemoe.sora.lang.Language;
import io.github.rosemoe.sora.langs.java.JavaLanguage;
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme;
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage;
import io.github.rosemoe.sora.widget.CodeEditor;
import io.github.rosemoe.sora.widget.EditorSearcher;
import io.github.rosemoe.sora.widget.SymbolInputView;
import io.github.rosemoe.sora.widget.component.EditorAutoCompletion;
import io.github.rosemoe.sora.widget.component.Magnifier;
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;

public class TextEditorActivity extends BaseActivity {
    private static final int LANGUAGE_JAVA = 0;
    private static final int LANGUAGE_KOTLIN = 1;
    private static final int LANGUAGE_JSON = 2;
    private static final int LANGUAGE_XML = 3;

    private CodeEditor editor;
    private View searchPanel;
    private TextEditorViewModel editorViewModel;

    @Override
    public void init() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.text_editor_activity);

        editorViewModel = new ViewModelProvider(this).get(TextEditorViewModel.class);

        editor = findViewById(R.id.editor);
        Toolbar materialToolbar = findViewById(R.id.toolbar);
        searchPanel = findViewById(R.id.search_panel);
        setupSearchPanel();

        SymbolInputView inputView = findViewById(R.id.symbol_input);
        inputView.bindEditor(editor);
        inputView.setBackgroundColor(SurfaceColors.SURFACE_1.getColor(this));
        inputView.setTextColor(Utils.getColorAttribute(R.attr.colorOnSurface, this));
        inputView.addSymbols(new String[]{"->", "_", "=", "{", "}", "<", ">", "|", "\\", "?", "+", "-", "*", "/"},
                new String[]{"\t", "_", "=", "{", "}", "<", ">", "|", "\\", "?", "+", "-", "*", "/"});

        editor.getComponent(EditorAutoCompletion.class).setLayout(new CustomCompletionLayout());
        editor.getComponent(EditorAutoCompletion.class).setAdapter(new CustomCompletionItemAdapter());
        editor.setTypefaceText(Typeface.createFromAsset(getAssets(), "font/JetBrainsMono-Regular.ttf"));
        editor.getProps().useICULibToSelectWords = false;
        editor.getProps().symbolPairAutoCompletion = false;
        editor.getProps().deleteMultiSpaces = -1;
        editor.getProps().deleteEmptyLineFast = false;

        loadEditorPrefs();

        if (editorViewModel.file == null)
            editorViewModel.file = new File(getIntent().getStringExtra("file"));

        detectLanguage(editorViewModel.file);

        materialToolbar.setTitle(editorViewModel.file.getName());
        setSupportActionBar(materialToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        materialToolbar.setNavigationOnClickListener(view -> onBackPressed());


        if (!editorViewModel.file.exists()) {
            App.showMsg("File not found");
            finish();
        }
        if (editorViewModel.file.isDirectory()) {
            App.showMsg("Invalid file");
            finish();
        }

        try {
            if (editorViewModel.content != null) {
                editor.setText(editorViewModel.content);
            } else {
                editor.setText(FileUtils.readFile(editorViewModel.file));
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            App.showMsg("Failed to read file: " + editorViewModel.file.getAbsolutePath());
            App.log(exception);
            finish();
        }

        editor.post(() -> {
            if (FileUtils.isEmpty(editor.getText().toString())) {
                if ("Main.java".equalsIgnoreCase(editorViewModel.file.getName())) {
                    askToLoadCodeSample(true);
                } else if ("Main.kt".equalsIgnoreCase(editorViewModel.file.getName())) {
                    askToLoadCodeSample(false);
                }
            }

        });
    }

    private void askToLoadCodeSample(boolean isJava) {
        new CustomDialog()
                .setTitle("Help")
                .setMsg("Do you want to use an executable code sample in this file?")
                .setPositiveButton("Yes", (v) -> editor.setText(getCodeSample(isJava)), true)
                .setNegativeButton("No", null, true)
                .showDialog(getSupportFragmentManager(), "");
    }

    private String getCodeSample(boolean isJava) {
        try {
            return isJava
                    ? FileUtils.copyFromInputStream(getAssets().open("sample_code/java_sample_code.txt"))
                    : FileUtils.copyFromInputStream(getAssets().open("sample_code/kotlin_sample_code.txt"));
        } catch (IOException e) {
            App.log(e);
            App.showWarning("Failed to load sample code");
            return "";
        }
    }

    private void detectLanguage(File file) {
        String ext = FileUtils.getFileExtension(file).toLowerCase();
        switch (ext) {
            case "java":
                setEditorLanguage(LANGUAGE_JAVA);
                break;
            case "kt":
                setEditorLanguage(LANGUAGE_KOTLIN);
                break;
            case "json":
                setEditorLanguage(LANGUAGE_JSON);
                break;
            case "xml":
                setEditorLanguage(LANGUAGE_XML);
                break;
            default:
                setEditorLanguage(-1);
        }
    }

    private void setupSearchPanel() {
        TextInputLayout findInput = searchPanel.findViewById(R.id.find_input);
        findInput.setHint("Find text");
        TextInputLayout replaceInput = searchPanel.findViewById(R.id.replace_input);
        replaceInput.setHint("Replacement");

        Objects.requireNonNull(findInput.getEditText()).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    editor.getSearcher().search(editable.toString(),
                            new EditorSearcher.SearchOptions(false, false));
                } else {
                    editor.getSearcher().stopSearch();
                }
            }
        });

        searchPanel.findViewById(R.id.next).setOnClickListener(view -> {
            if (editor.getSearcher().hasQuery()) editor.getSearcher().gotoNext();
        });
        searchPanel.findViewById(R.id.previous).setOnClickListener(view -> {
            if (editor.getSearcher().hasQuery()) editor.getSearcher().gotoPrevious();
        });

        searchPanel.findViewById(R.id.replace).setOnClickListener(view -> {
            if (editor.getSearcher().hasQuery())
                editor.getSearcher().replaceThis(Objects.requireNonNull(replaceInput.getEditText()).getText().toString());
        });
        searchPanel.findViewById(R.id.replace_all).setOnClickListener(view -> {
            if (editor.getSearcher().hasQuery())
                editor.getSearcher().replaceAll(Objects.requireNonNull(replaceInput.getEditText()).getText().toString());
        });

    }

    @Override
    public void onStop() {
        super.onStop();
        editorViewModel.content = editor.getText();
    }

    @Override
    public void onBackPressed() {
        if (searchPanel.getVisibility() == View.VISIBLE) {
            searchPanel.setVisibility(View.GONE);
            editor.getSearcher().stopSearch();
            return;
        }
        try {
            if (!FileUtils.readFile(editorViewModel.file).equals(editor.getText().toString())) {
                new CustomDialog()
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

    private boolean canExecute() {
        return new File(editorViewModel.file.getParentFile(), "Main.java").exists()
                || new File(editorViewModel.file.getParentFile(), "Main.kt").exists();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.text_editor_menu, menu);

        menu.findItem(R.id.editor_option_wordwrap).setChecked(PrefsUtils.getTextEditorWordwrap());
        menu.findItem(R.id.editor_option_magnifier).setChecked(PrefsUtils.getTextEditorMagnifier());
        menu.findItem(R.id.editor_option_pin_line_number).setChecked(PrefsUtils.getTextEditorPinLineNumber());
        menu.findItem(R.id.editor_option_line_number).setChecked(PrefsUtils.getTextEditorShowLineNumber());
        menu.findItem(R.id.editor_option_read_only).setChecked(PrefsUtils.getTextEditorReadOnly());

        if (!"java".equals(FileUtils.getFileExtension(editorViewModel.file))) {
            menu.findItem(R.id.editor_format).setVisible(false);
        }
        if (!canExecute()) menu.findItem(R.id.editor_execute).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    private void loadEditorPrefs() {
        editor.setPinLineNumber(PrefsUtils.getTextEditorPinLineNumber());
        editor.setWordwrap(PrefsUtils.getTextEditorWordwrap());
        editor.setLineNumberEnabled(PrefsUtils.getTextEditorShowLineNumber());
        editor.getComponent(Magnifier.class).setEnabled(PrefsUtils.getTextEditorMagnifier());
        editor.setEditable(!PrefsUtils.getTextEditorReadOnly());
        editor.getComponent(EditorAutoCompletion.class).setEnabled(PrefsUtils.getTextEditorAutocomplete());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.editor_format) {
            editor.formatCodeAsync();
        } else if (id == R.id.editor_execute) {
            saveFile(editor.getText().toString());
            executeFile();
        } else if (id == R.id.editor_language_def) {
            item.setChecked(true);
            editor.setEditorLanguage(null);
        } else if (id == R.id.editor_language_java) {
            item.setChecked(true);
            setEditorLanguage(LANGUAGE_JAVA);
        } else if (id == R.id.editor_language_kotlin) {
            item.setChecked(true);
            setEditorLanguage(LANGUAGE_KOTLIN);
        } else if (id == R.id.editor_option_read_only) {
            item.setChecked(!item.isChecked());
            PrefsUtils.setTextEditorReadOnly(item.isChecked());
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
            PrefsUtils.setTextEditorWordwrap(item.isChecked());
            editor.setWordwrap(item.isChecked());
        } else if (id == R.id.editor_option_magnifier) {
            item.setChecked(!item.isChecked());
            editor.getComponent(Magnifier.class).setEnabled(item.isChecked());
            PrefsUtils.setTextEditorMagnifier(item.isChecked());
        } else if (id == R.id.editor_option_line_number) {
            item.setChecked(!item.isChecked());
            PrefsUtils.setTextEditorShowLineNumber(item.isChecked());
            editor.setLineNumberEnabled(item.isChecked());
        } else if (id == R.id.editor_option_pin_line_number) {
            item.setChecked(!item.isChecked());
            PrefsUtils.setTextEditorPinLineNumber(item.isChecked());
            editor.setPinLineNumber(item.isChecked());
        } else if (id == R.id.editor_option_autocomplete) {
            item.setChecked(!item.isChecked());
            PrefsUtils.setTextEditorAutocomplete(item.isChecked());
            editor.getComponent(EditorAutoCompletion.class).setEnabled(item.isChecked());
        }
        return super.onOptionsItemSelected(item);
    }

    private void setEditorLanguage(int language) {
        if (language == LANGUAGE_JAVA) {
            editor.setColorScheme(getColorScheme(false));
            editor.setEditorLanguage(new JavaLanguage());
        } else if (language == LANGUAGE_KOTLIN) {
            editor.setColorScheme(getColorScheme(true));
            editor.setEditorLanguage(getKotlinLang());
        } else if (language == LANGUAGE_XML) {
            editor.setColorScheme(getColorScheme(true));
            editor.setEditorLanguage(getXmlLang());
        } else if (language == LANGUAGE_JSON) {
            editor.setColorScheme(getColorScheme(true));
            editor.setEditorLanguage(getJsonLang());
        } else {
            editor.setColorScheme(getColorScheme(false));
            editor.setEditorLanguage(new EmptyLanguage());
        }
    }

    private Language getJsonLang() {
        try {
            return TextMateLanguage.create("json.tmLanguage.json",
                    getAssets().open("textmate/json/syntax/json.tmLanguage.json"),
                    new InputStreamReader(getAssets().open("textmate/json/language-configuration.json")),
                    ((TextMateColorScheme) getColorScheme(true)).getRawTheme());
        } catch (IOException e) {
            e.printStackTrace();
            App.log(e);
            App.showMsg("Unable to set the language: textmate/json/syntax/kotlin.tmLanguage.json");
            return new EmptyLanguage();
        }
    }

    private Language getXmlLang() {
        try {
            return TextMateLanguage.create("xml.tmLanguage.json",
                    getAssets().open("textmate/xml/syntax/xml.tmLanguage.json"),
                    new InputStreamReader(getAssets().open("textmate/xml/language-configuration.json")),
                    ((TextMateColorScheme) getColorScheme(true)).getRawTheme());
        } catch (IOException e) {
            e.printStackTrace();
            App.log(e);
            App.showMsg("Unable to set the language: textmate/xml/syntax/xml.tmLanguage.json");
            return new EmptyLanguage();
        }
    }

    private Language getKotlinLang() {
        try {
            return TextMateLanguage.create("kotlin.tmLanguage",
                    getAssets().open("textmate/kotlin/syntax/kotlin.tmLanguage"),
                    new InputStreamReader(getAssets().open("textmate/kotlin/language-configuration.json")),
                    ((TextMateColorScheme) getColorScheme(true)).getRawTheme());
        } catch (IOException e) {
            e.printStackTrace();
            App.log(e);
            App.showMsg("Unable to set the language: textmate/kotlin/syntax/kotlin.tmLanguage");
            return new EmptyLanguage();
        }
    }

    private EditorColorScheme getColorScheme(boolean isTextmate) {
        return Utils.isDarkMode()
                ? getDarkScheme(isTextmate)
                : getLightScheme(isTextmate);
    }

    private EditorColorScheme getLightScheme(boolean isTextmate) {
        if (isTextmate) {
            try {
                return TextMateColorScheme.create(ThemeReader.readThemeSync("light.tmTheme",
                        getAssets().open("textmate/light.tmTheme")));
            } catch (Exception e) {
                e.printStackTrace();
                App.log(e);
                App.showMsg("Unable to load: textmate/light.tmTheme");
            }
        }
        EditorColorScheme scheme = new LightScheme();
        scheme.setColor(EditorColorScheme.WHOLE_BACKGROUND, SurfaceColors.SURFACE_0.getColor(this));
        scheme.setColor(EditorColorScheme.LINE_NUMBER_BACKGROUND, SurfaceColors.SURFACE_0.getColor(this));
        scheme.setColor(EditorColorScheme.AUTO_COMP_PANEL_BG, SurfaceColors.SURFACE_1.getColor(this));
        return scheme;
    }

    private EditorColorScheme getDarkScheme(boolean isTextmate) {
        if (isTextmate) {
            try {
                return TextMateColorScheme.create(ThemeReader.readThemeSync("dark.json",
                        getAssets().open("textmate/dark.json")));
            } catch (Exception e) {
                e.printStackTrace();
                App.log(e);
                App.showMsg("Unable to load: textmate/dark.json");
            }
        }
        EditorColorScheme scheme = new DarkScheme();
        scheme.setColor(EditorColorScheme.WHOLE_BACKGROUND, SurfaceColors.SURFACE_0.getColor(this));
        scheme.setColor(EditorColorScheme.LINE_NUMBER_BACKGROUND, SurfaceColors.SURFACE_0.getColor(this));
        scheme.setColor(EditorColorScheme.AUTO_COMP_PANEL_BG, SurfaceColors.SURFACE_1.getColor(this));
        return scheme;
    }


    private void executeFile() {
        Executor executor = new Executor(Objects.requireNonNull(editorViewModel.file.getParentFile()), this);
        BackgroundTask backgroundTask = new BackgroundTask();

        AtomicReference<String> error = new AtomicReference<>("");

        backgroundTask.setTasks(() -> backgroundTask.showProgressDialog("compiling files...", this), () -> {
            try {
                executor.execute();
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
                executor.invoke();
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
        new CustomDialog()
                .setTitle(title)
                .setMsg(msg)
                .setPositiveButton("Ok", null, true)
                .showDialog(getSupportFragmentManager(), "");
    }

    private void saveFile(String content) {
        try {
            FileUtils.writeFile(editorViewModel.file, content);
        } catch (IOException e) {
            e.printStackTrace();
            App.showMsg("Something went wrong, check app debug for more details");
            App.log(e);
        }

    }
}
