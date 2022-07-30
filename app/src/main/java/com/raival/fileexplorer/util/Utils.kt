package com.raival.fileexplorer.util

import android.content.Context
import android.content.res.Configuration
import android.util.TypedValue
import androidx.annotation.ColorInt
import com.raival.fileexplorer.App
import com.raival.fileexplorer.activity.SettingsActivity
import com.raival.fileexplorer.util.PrefsUtils.Settings.themeMode
import java.util.*

object Utils {
    private const val ALLOWED_CHARACTERS = "0123456789qwertyuiopasdfghjklzxcvbnm_"

    @ColorInt
    fun getColorAttribute(id: Int, context: Context): Int {
        val out = TypedValue()
        context.theme.resolveAttribute(id, out, true)
        return out.data
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
}