package com.raival.fileexplorer.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.util.TypedValue
import androidx.annotation.ColorInt
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.raival.fileexplorer.App
import com.raival.fileexplorer.activity.SettingsActivity
import com.raival.fileexplorer.util.PrefsUtils.Settings.themeMode
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object Utils {
    private const val REGULAR_DATE_FORMAT = "MMM dd , hh:mm a"
    const val TAB = "  "
    private const val ALLOWED_CHARACTERS = "0123456789qwertyuiopasdfghjklzxcvbnm_"

    @ColorInt
    fun getColorAttribute(id: Int, context: Context): Int {
        val out = TypedValue()
        context.theme.resolveAttribute(id, out, true)
        return out.data
    }

    fun pxToDp(`val`: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            `val`,
            App.appContext.resources.displayMetrics
        )
    }

    @JvmStatic
    @SuppressLint("SimpleDateFormat")
    fun getLastModifiedDate(file: File): String {
        return SimpleDateFormat(REGULAR_DATE_FORMAT).format(file.lastModified())
    }

    val isDarkMode: Boolean
        get() = when (themeMode) {
            SettingsActivity.THEME_MODE_DARK -> {
                true
            }
            SettingsActivity.THEME_MODE_LIGHT -> {
                false
            }
            else -> {
                (App.appContext.resources.configuration.uiMode
                        and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            }
        }

    fun getRandomString(sizeOfRandomString: Int): String {
        val random = Random()
        val sb = StringBuilder(sizeOfRandomString)
        for (i in 0 until sizeOfRandomString) {
            sb.append(ALLOWED_CHARACTERS[random.nextInt(ALLOWED_CHARACTERS.length)])
        }
        return sb.toString()
    }

    fun getStringList(json: String?): ArrayList<String> {
        return Gson().fromJson(json, object : TypeToken<ArrayList<String?>?>() {}.type)
    }

    fun surroundWithBrackets(string: String): String {
        return "[$string]"
    }
}