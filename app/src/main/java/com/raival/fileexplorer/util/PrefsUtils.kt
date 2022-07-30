package com.raival.fileexplorer.util

import com.google.gson.Gson
import com.pixplicity.easyprefs.library.Prefs
import com.raival.fileexplorer.activity.SettingsActivity
import com.raival.fileexplorer.extension.getStringList

object PrefsUtils {
    const val SORT_NAME_A2Z = 1
    const val SORT_NAME_Z2A = 2
    const val SORT_SIZE_SMALLER = 3
    const val SORT_SIZE_BIGGER = 4
    const val SORT_DATE_OLDER = 5
    const val SORT_DATE_NEWER = 6

    object TextEditor {
        var textEditorWordwrap: Boolean
            get() = Prefs.getBoolean("text_editor_wordwrap", false)
            set(wordwrap) {
                Prefs.putBoolean("text_editor_wordwrap", wordwrap)
            }
        var textEditorShowLineNumber: Boolean
            get() = Prefs.getBoolean("text_editor_show_line_number", true)
            set(showLineNumber) {
                Prefs.putBoolean("text_editor_show_line_number", showLineNumber)
            }
        var textEditorPinLineNumber: Boolean
            get() = Prefs.getBoolean("text_editor_pin_line_number", true)
            set(pinLineNumber) {
                Prefs.putBoolean("text_editor_pin_line_number", pinLineNumber)
            }
        var textEditorMagnifier: Boolean
            get() = Prefs.getBoolean("text_editor_magnifier", true)
            set(magnifier) {
                Prefs.putBoolean("text_editor_magnifier", magnifier)
            }
        var textEditorReadOnly: Boolean
            get() = Prefs.getBoolean("text_editor_read_only", false)
            set(readOnly) {
                Prefs.putBoolean("text_editor_read_only", readOnly)
            }
        var textEditorAutocomplete: Boolean
            get() = Prefs.getBoolean("text_editor_autocomplete", false)
            set(autocomplete) {
                Prefs.putBoolean("text_editor_autocomplete", autocomplete)
            }
        val fileExplorerTabBookmarks: ArrayList<String>
            get() = Prefs.getString("file_explorer_tab_bookmarks", "[]").getStringList()
    }

    object FileExplorerTab {
        @JvmStatic
        var sortingMethod: Int
            get() = Prefs.getInt("sorting_method", SORT_NAME_A2Z)
            set(method) {
                Prefs.putInt("sorting_method", method)
            }

        fun setListFoldersFirst(b: Boolean) {
            Prefs.putBoolean("list_folders_first", b)
        }

        @JvmStatic
        fun listFoldersFirst(): Boolean {
            return Prefs.getBoolean("list_folders_first", true)
        }
    }

    object Settings {
        var deepSearchFileSizeLimit: Long
            get() = Prefs.getLong(
                "settings_deep_search_file_size_limit",
                (6 * 1024 * 1024).toLong()
            )
            set(limit) {
                Prefs.putLong("settings_deep_search_file_size_limit", limit)
            }

        @JvmStatic
        var themeMode: String
            get() = Prefs.getString("settings_theme_mode", SettingsActivity.THEME_MODE_AUTO)
            set(themeMode) {
                Prefs.putString("settings_theme_mode", themeMode)
            }
        var logMode: String
            get() = Prefs.getString("settings_log_mode", SettingsActivity.LOG_MODE_ERRORS_ONLY)
            set(logMode) {
                Prefs.putString("settings_log_mode", logMode)
            }
        var showBottomToolbarLabels: Boolean
            get() = Prefs.getBoolean("settings_show_bottom_toolbar_labels", true)
            set(showBottomToolbarLabels) {
                Prefs.putBoolean("settings_show_bottom_toolbar_labels", showBottomToolbarLabels)
            }
    }

    object General {
        fun setFileExplorerTabBookmarks(list: ArrayList<String>) {
            Prefs.putString("file_explorer_tab_bookmarks", Gson().toJson(list))
        }
    }
}