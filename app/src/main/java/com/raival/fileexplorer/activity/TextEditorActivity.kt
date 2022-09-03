package com.raival.fileexplorer.activity

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.elevation.SurfaceColors
import com.google.android.material.textfield.TextInputLayout
import com.raival.fileexplorer.App
import com.raival.fileexplorer.App.Companion.showMsg
import com.raival.fileexplorer.R
import com.raival.fileexplorer.activity.editor.autocomplete.CustomCompletionItemAdapter
import com.raival.fileexplorer.activity.editor.autocomplete.CustomCompletionLayout
import com.raival.fileexplorer.activity.editor.language.java.JavaCodeLanguage
import com.raival.fileexplorer.activity.editor.language.json.JsonLanguage
import com.raival.fileexplorer.activity.editor.language.kotlin.KotlinCodeLanguage
import com.raival.fileexplorer.activity.editor.language.xml.XmlLanguage
import com.raival.fileexplorer.activity.editor.scheme.DarkScheme
import com.raival.fileexplorer.activity.editor.scheme.LightScheme
import com.raival.fileexplorer.activity.editor.view.SymbolInputView
import com.raival.fileexplorer.activity.model.TextEditorViewModel
import com.raival.fileexplorer.common.BackgroundTask
import com.raival.fileexplorer.tab.file.executor.Executor
import com.raival.fileexplorer.tab.file.misc.FileMimeTypes
import com.raival.fileexplorer.tab.file.misc.FileUtils
import com.raival.fileexplorer.util.Log
import com.raival.fileexplorer.util.PrefsUtils
import com.raival.fileexplorer.util.Utils
import io.github.rosemoe.sora.lang.EmptyLanguage
import io.github.rosemoe.sora.lang.Language
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.EditorSearcher.SearchOptions
import io.github.rosemoe.sora.widget.component.EditorAutoCompletion
import io.github.rosemoe.sora.widget.component.Magnifier
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme
import org.eclipse.tm4e.core.registry.IThemeSource
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.atomic.AtomicReference

class TextEditorActivity : BaseActivity() {
    private lateinit var editor: CodeEditor
    private lateinit var searchPanel: View
    private lateinit var editorViewModel: TextEditorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.text_editor_activity)

        editorViewModel = ViewModelProvider(this).get(TextEditorViewModel::class.java)
        editor = findViewById(R.id.editor)
        val materialToolbar = findViewById<Toolbar>(R.id.toolbar)
        searchPanel = findViewById(R.id.search_panel)

        setupSearchPanel()

        val inputView = findViewById<SymbolInputView>(R.id.symbol_input)
        inputView.bindEditor(editor)
            .setTextColor(Utils.getColorAttribute(R.attr.colorOnSurface, this))
            .setBackgroundColor(SurfaceColors.SURFACE_2.getColor(this))
        inputView.addSymbol("->", "    ")
            .addSymbols(arrayOf("_", "=", "{", "}", "<", ">", "|", "\\", "?", "+", "-", "*", "/"))

        editor.apply {
            getComponent(EditorAutoCompletion::class.java).setLayout(CustomCompletionLayout())
            getComponent(EditorAutoCompletion::class.java)
                .setAdapter(CustomCompletionItemAdapter())
            typefaceText = Typeface.createFromAsset(assets, "font/JetBrainsMono-Regular.ttf")
            props.useICULibToSelectWords = false
            props.symbolPairAutoCompletion = false
            props.deleteMultiSpaces = -1
            props.deleteEmptyLineFast = false

        }

        loadEditorPrefs()

        if (editorViewModel.file == null) editorViewModel.file =
            File(intent.getStringExtra("file")!!)
        detectLanguage(editorViewModel.file!!)

        materialToolbar.title = editorViewModel.file!!.name
        setSupportActionBar(materialToolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }
        materialToolbar.setNavigationOnClickListener { onBackPressed() }

        if (!editorViewModel.file!!.exists()) {
            showMsg("File not found")
            finish()
        }

        if (editorViewModel.file!!.isDirectory) {
            showMsg("Invalid file")
            finish()
        }

        try {
            if (editorViewModel.content != null) {
                editor.setText(editorViewModel.content.toString())
            } else {
                editor.setText(editorViewModel.file?.readText())
            }
        } catch (exception: Exception) {
            Log.e(
                TAG,
                Log.SOMETHING_WENT_WRONG + " while reading file: " + editorViewModel.file!!.absolutePath,
                exception
            )
            showMsg("Failed to read file: " + editorViewModel.file!!.absolutePath)
            finish()
        }

        editor.post {
            if (editor.text.toString().isEmpty()) {
                if ("Main.java".equals(editorViewModel.file!!.name, ignoreCase = true)) {
                    askToLoadCodeSample(true)
                } else if ("Main.kt".equals(editorViewModel.file!!.name, ignoreCase = true)) {
                    askToLoadCodeSample(false)
                }
            }
        }
    }

    private fun askToLoadCodeSample(isJava: Boolean) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Help")
            .setMessage("Do you want to use an executable code sample in this file?")
            .setPositiveButton("Yes") { _, _ -> editor.setText(getCodeSample(isJava)) }
            .setNegativeButton("No", null)
            .show()
    }

    private fun getCodeSample(isJava: Boolean): String {
        return try {
            if (isJava) javaSampleCode else kotlinSampleCode
        } catch (e: Exception) {
            Log.e(
                TAG,
                Log.SOMETHING_WENT_WRONG
                        + " while loading "
                        + (if (isJava) FileMimeTypes.javaType else "kotlin")
                        + " sample code",
                e
            )
            showMsg("Failed to load sample code")
            ""
        }
    }

    @get:Throws(Exception::class)
    private val javaSampleCode: String
        get() {
            val customSampleCode = File(
                App.appContext.getExternalFilesDir(null),
                "sample_code/java_sample_code.java"
            )
            return if (customSampleCode.exists() && customSampleCode.isFile) {
                customSampleCode.readText()
            } else FileUtils.copyFromInputStream(assets.open("sample_code/java_sample_code.java"))
        }

    @get:Throws(Exception::class)
    private val kotlinSampleCode: String
        get() {
            val customSampleCode = File(
                App.appContext.getExternalFilesDir(null),
                "sample_code/kotlin_sample_code.kt"
            )
            return if (customSampleCode.exists() && customSampleCode.isFile) {
                customSampleCode.readText()
            } else FileUtils.copyFromInputStream(assets.open("sample_code/kotlin_sample_code.kt"))
        }

    private fun detectLanguage(file: File) {
        when (file.extension.lowercase(Locale.getDefault())) {
            FileMimeTypes.javaType -> setEditorLanguage(LANGUAGE_JAVA)
            FileMimeTypes.kotlinType -> setEditorLanguage(LANGUAGE_KOTLIN)
            "json" -> setEditorLanguage(LANGUAGE_JSON)
            "xml" -> setEditorLanguage(LANGUAGE_XML)
            else -> setEditorLanguage(-1)
        }
    }

    private fun setupSearchPanel() {
        val findInput = searchPanel.findViewById<TextInputLayout>(R.id.find_input)
        findInput.hint = "Find text"
        val replaceInput = searchPanel.findViewById<TextInputLayout>(R.id.replace_input)
        replaceInput.hint = "Replacement"

        findInput.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                if (editable.isNotEmpty()) {
                    editor.searcher.search(
                        editable.toString(),
                        SearchOptions(false, false)
                    )
                } else {
                    editor.searcher.stopSearch()
                }
            }
        })
        searchPanel.apply {
            findViewById<View>(R.id.next)
                .setOnClickListener { if (editor.searcher.hasQuery()) editor.searcher.gotoNext() }
            findViewById<View>(R.id.previous)
                .setOnClickListener { if (editor.searcher.hasQuery()) editor.searcher.gotoPrevious() }
            findViewById<View>(R.id.replace).setOnClickListener {
                if (editor.searcher.hasQuery()) editor.searcher.replaceThis(
                    replaceInput.editText?.text.toString()
                )
            }
            findViewById<View>(R.id.replace_all).setOnClickListener {
                if (editor.searcher.hasQuery()) editor.searcher.replaceAll(
                    replaceInput.editText?.text.toString()
                )
            }
        }
    }

    public override fun onStop() {
        super.onStop()
        editorViewModel.content = editor.text
    }

    override fun onBackPressed() {
        if (searchPanel.visibility == View.VISIBLE) {
            searchPanel.visibility = View.GONE
            editor.searcher.stopSearch()
            return
        }
        try {
            if (editorViewModel.file?.readText() != editor.text.toString()) {
                MaterialAlertDialogBuilder(this)
                    .setTitle("Save File")
                    .setMessage("Do you want to save this file before exit?")
                    .setPositiveButton("Yes") { _, _ ->
                        saveFile(editor.text.toString())
                        finish()
                    }
                    .setNegativeButton("No") { _, _ -> finish() }
                    .show()
                return
            }
        } catch (exception: Exception) {
            Log.w(TAG, exception)
        }
        super.onBackPressed()
    }

    private fun canExecute(): Boolean {
        return (File(editorViewModel.file?.parentFile, "Main.java").exists()
                || File(editorViewModel.file?.parentFile, "Main.kt").exists())
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.text_editor_menu, menu)
        (menu as MenuBuilder).setOptionalIconsVisible(true)
        menu.findItem(R.id.editor_option_wordwrap).isChecked =
            PrefsUtils.TextEditor.textEditorWordwrap
        menu.findItem(R.id.editor_option_magnifier).isChecked =
            PrefsUtils.TextEditor.textEditorMagnifier
        menu.findItem(R.id.editor_option_pin_line_number).isChecked =
            PrefsUtils.TextEditor.textEditorPinLineNumber
        menu.findItem(R.id.editor_option_line_number).isChecked =
            PrefsUtils.TextEditor.textEditorShowLineNumber
        menu.findItem(R.id.editor_option_read_only).isChecked =
            PrefsUtils.TextEditor.textEditorReadOnly
        menu.findItem(R.id.editor_option_autocomplete).isChecked =
            PrefsUtils.TextEditor.textEditorAutocomplete

        if (!canExecute()) menu.findItem(R.id.editor_execute).isVisible = false
        return super.onCreateOptionsMenu(menu)
    }

    private fun loadEditorPrefs() {
        editor.setPinLineNumber(PrefsUtils.TextEditor.textEditorPinLineNumber)
        editor.isWordwrap = PrefsUtils.TextEditor.textEditorWordwrap
        editor.isLineNumberEnabled = PrefsUtils.TextEditor.textEditorShowLineNumber
        editor.getComponent(Magnifier::class.java).isEnabled =
            PrefsUtils.TextEditor.textEditorMagnifier
        editor.isEditable = !PrefsUtils.TextEditor.textEditorReadOnly
        editor.getComponent(EditorAutoCompletion::class.java).isEnabled =
            PrefsUtils.TextEditor.textEditorAutocomplete
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.editor_format) {
            editor.formatCodeAsync()
        } else if (id == R.id.editor_execute) {
            saveFile(editor.text.toString())
            executeFile()
        } else if (id == R.id.editor_language_def) {
            item.isChecked = true
            editor.setEditorLanguage(null)
        } else if (id == R.id.editor_language_java) {
            item.isChecked = true
            setEditorLanguage(LANGUAGE_JAVA)
        } else if (id == R.id.editor_language_kotlin) {
            item.isChecked = true
            setEditorLanguage(LANGUAGE_KOTLIN)
        } else if (id == R.id.editor_option_read_only) {
            item.isChecked = !item.isChecked
            PrefsUtils.TextEditor.textEditorReadOnly = item.isChecked
            editor.isEditable = !item.isChecked
        } else if (id == R.id.editor_option_search) {
            if (searchPanel.visibility == View.GONE) {
                searchPanel.visibility = View.VISIBLE
            } else {
                searchPanel.visibility = View.GONE
                editor.searcher.stopSearch()
            }
        } else if (id == R.id.editor_option_save) {
            saveFile(editor.text.toString())
            showMsg("Saved successfully")
        } else if (id == R.id.editor_option_text_undo) {
            editor.undo()
        } else if (id == R.id.editor_option_text_redo) {
            editor.redo()
        } else if (id == R.id.editor_option_wordwrap) {
            item.isChecked = !item.isChecked
            PrefsUtils.TextEditor.textEditorWordwrap = item.isChecked
            editor.isWordwrap = item.isChecked
        } else if (id == R.id.editor_option_magnifier) {
            item.isChecked = !item.isChecked
            editor.getComponent(Magnifier::class.java).isEnabled =
                item.isChecked
            PrefsUtils.TextEditor.textEditorMagnifier = item.isChecked
        } else if (id == R.id.editor_option_line_number) {
            item.isChecked = !item.isChecked
            PrefsUtils.TextEditor.textEditorShowLineNumber = item.isChecked
            editor.isLineNumberEnabled = item.isChecked
        } else if (id == R.id.editor_option_pin_line_number) {
            item.isChecked = !item.isChecked
            PrefsUtils.TextEditor.textEditorPinLineNumber = item.isChecked
            editor.setPinLineNumber(item.isChecked)
        } else if (id == R.id.editor_option_autocomplete) {
            item.isChecked = !item.isChecked
            PrefsUtils.TextEditor.textEditorAutocomplete = item.isChecked
            editor.getComponent(EditorAutoCompletion::class.java).isEnabled = item.isChecked
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setEditorLanguage(language: Int) {
        when (language) {
            LANGUAGE_JAVA -> {
                editor.apply {
                    colorScheme = getColorScheme(false)
                    setEditorLanguage(javaLanguage)
                }
            }
            LANGUAGE_KOTLIN -> {
                editor.apply {
                    colorScheme = getColorScheme(true)
                    setEditorLanguage(kotlinLang)
                }
            }
            LANGUAGE_XML -> {
                editor.apply {
                    colorScheme = getColorScheme(true)
                    setEditorLanguage(xmlLang)
                }
            }
            LANGUAGE_JSON -> {
                editor.apply {
                    colorScheme = getColorScheme(true)
                    setEditorLanguage(jsonLang)
                }
            }
            else -> {
                editor.apply {
                    colorScheme = getColorScheme(false)
                    setEditorLanguage(EmptyLanguage())
                }
            }
        }
    }

    private val javaLanguage: Language
        get() = JavaCodeLanguage()

    private val jsonLang: Language
        get() = JsonLanguage((getColorScheme(true) as TextMateColorScheme).themeSource)
    private val xmlLang: Language
        get() = XmlLanguage((getColorScheme(true) as TextMateColorScheme).themeSource)
    private val kotlinLang: Language
        get() = KotlinCodeLanguage((getColorScheme(true) as TextMateColorScheme).themeSource)

    private fun getColorScheme(isTextmate: Boolean): EditorColorScheme {
        return if (Utils.isDarkMode) getDarkScheme(isTextmate) else getLightScheme(isTextmate)
    }

    private fun getLightScheme(isTextmate: Boolean): EditorColorScheme {
        val scheme: EditorColorScheme = if (isTextmate) {
            try {
                TextMateColorScheme.create(
                    IThemeSource.fromInputStream(
                        assets.open("textmate/light.tmTheme"),
                        "light.tmTheme",
                        null
                    )
                )
            } catch (e: Exception) {
                Log.e(
                    TAG,
                    Log.SOMETHING_WENT_WRONG + " while creating light scheme for textmate language",
                    e
                )
                showMsg(Log.UNABLE_TO + " load: textmate/light.tmTheme")
                LightScheme()
            }
        } else {
            LightScheme()
        }
        scheme.apply {
            setColor(
                EditorColorScheme.WHOLE_BACKGROUND,
                SurfaceColors.SURFACE_0.getColor(this@TextEditorActivity)
            )
            setColor(
                EditorColorScheme.LINE_NUMBER_BACKGROUND,
                SurfaceColors.SURFACE_0.getColor(this@TextEditorActivity)
            )
            setColor(
                EditorColorScheme.COMPLETION_WND_BACKGROUND,
                SurfaceColors.SURFACE_1.getColor(this@TextEditorActivity)
            )
            setColor(EditorColorScheme.HIGHLIGHTED_DELIMITERS_FOREGROUND, Color.RED)
        }
        return scheme
    }

    private fun getDarkScheme(isTextmate: Boolean): EditorColorScheme {
        val scheme: EditorColorScheme = if (isTextmate) {
            try {
                TextMateColorScheme.create(
                    IThemeSource.fromInputStream(
                        assets.open("textmate/dark.json"),
                        "dark.json",
                        null
                    )
                )
            } catch (e: Exception) {
                Log.e(
                    TAG,
                    Log.SOMETHING_WENT_WRONG + " while creating dark scheme for textmate language",
                    e
                )
                showMsg(Log.UNABLE_TO + " load: textmate/dark.json")
                DarkScheme()
            }
        } else {
            DarkScheme()
        }
        scheme.apply {
            setColor(
                EditorColorScheme.WHOLE_BACKGROUND,
                SurfaceColors.SURFACE_0.getColor(this@TextEditorActivity)
            )
            setColor(
                EditorColorScheme.LINE_NUMBER_BACKGROUND,
                SurfaceColors.SURFACE_0.getColor(this@TextEditorActivity)
            )
            setColor(
                EditorColorScheme.COMPLETION_WND_BACKGROUND,
                SurfaceColors.SURFACE_1.getColor(this@TextEditorActivity)
            )
            setColor(EditorColorScheme.HIGHLIGHTED_DELIMITERS_FOREGROUND, Color.RED)
        }
        return scheme
    }

    private fun executeFile() {
        val executor = Executor(editorViewModel.file?.parentFile!!, this)
        val backgroundTask = BackgroundTask()
        val error = AtomicReference("")
        backgroundTask.setTasks({ backgroundTask.showProgressDialog("compiling files...", this) }, {
            try {
                executor.execute()
            } catch (exception: Exception) {
                error.set(Log.getStackTrace(exception))
            }
        }) {
            try {
                if (error.get().isNotEmpty()) {
                    backgroundTask.dismiss()
                    showDialog("Error", error.get())
                    return@setTasks
                }
                executor.invoke()
                backgroundTask.dismiss()
            } catch (exception: Exception) {
                backgroundTask.dismiss()
                showDialog("Error", Log.getStackTrace(exception))
            }
        }
        backgroundTask.run()
    }

    private fun showDialog(title: String, msg: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(msg)
            .setPositiveButton("Ok", null)
            .show()
    }

    private fun saveFile(content: String) {
        try {
            editorViewModel.file?.writeText(content)
        } catch (e: IOException) {
            Log.e(TAG, Log.UNABLE_TO + " write to file " + editorViewModel.file, e)
            showMsg(Log.SOMETHING_WENT_WRONG + ", check app debug for more details")
        }
    }

    companion object {
        private const val TAG = "TextEditorActivity"
        private const val LANGUAGE_JAVA = 0
        private const val LANGUAGE_KOTLIN = 1
        private const val LANGUAGE_JSON = 2
        private const val LANGUAGE_XML = 3
    }
}