package com.raival.fileexplorer.activity

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.slider.Slider
import com.raival.fileexplorer.R
import com.raival.fileexplorer.common.dialog.CustomDialog
import com.raival.fileexplorer.common.dialog.OptionsDialog
import com.raival.fileexplorer.tab.file.misc.FileUtils
import com.raival.fileexplorer.util.PrefsUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SettingsActivity : BaseActivity() {
    private lateinit var logModeValue: TextView
    private lateinit var themeModeValue: TextView
    private lateinit var deepSearchSizeLimitValue: TextView
    private lateinit var showBottomToolbarLabels: SwitchCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        val materialToolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(materialToolbar)
        supportActionBar?.title = "Settings"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        materialToolbar.setNavigationOnClickListener { onBackPressed() }

        themeModeValue = findViewById(R.id.settings_theme_value)
        themeModeValue.text = PrefsUtils.Settings.themeMode

        findViewById<View>(R.id.settings_theme).setOnClickListener {
            OptionsDialog("Select theme mode")
                .addOption(
                    label = THEME_MODE_AUTO,
                    listener = { setThemeMode(THEME_MODE_AUTO) },
                    dismissOnClick = true
                )
                .addOption(
                    label = THEME_MODE_DARK,
                    listener = { setThemeMode(THEME_MODE_DARK) },
                    dismissOnClick = true
                )
                .addOption(
                    label = THEME_MODE_LIGHT,
                    listener = { setThemeMode(THEME_MODE_LIGHT) },
                    dismissOnClick = true
                )
                .show(supportFragmentManager, "")
        }

        showBottomToolbarLabels = findViewById(R.id.settings_bottom_toolbar_labels_value)
        showBottomToolbarLabels.isChecked = PrefsUtils.Settings.showBottomToolbarLabels
        findViewById<View>(R.id.settings_bottom_toolbar_labels).setOnClickListener {
            showBottomToolbarLabels.isChecked = !showBottomToolbarLabels.isChecked
            PrefsUtils.Settings.showBottomToolbarLabels = showBottomToolbarLabels.isChecked
        }

        logModeValue = findViewById(R.id.settings_log_mode_value)
        logModeValue.text = PrefsUtils.Settings.logMode
        findViewById<View>(R.id.settings_log_mode).setOnClickListener {
            OptionsDialog("Select log mode")
                .addOption(LOG_MODE_DISABLE, { setLogMode(LOG_MODE_DISABLE) }, true)
                .addOption(
                    LOG_MODE_ERRORS_ONLY,
                    { setLogMode(LOG_MODE_ERRORS_ONLY) },
                    true
                )
                .addOption(LOG_MODE_ALL, { setLogMode(LOG_MODE_ALL) }, true)
                .show(supportFragmentManager, "")
        }

        deepSearchSizeLimitValue = findViewById(R.id.settings_deep_search_limit_value)
        deepSearchSizeLimitValue.text = FileUtils.getFormattedSize(
            PrefsUtils.Settings.deepSearchFileSizeLimit,
            "%.0f"
        )
        findViewById<View>(R.id.settings_deep_search_limit).setOnClickListener {
            val seekBar = Slider(this).apply {
                valueFrom = 0f
                valueTo = 80f
                stepSize = 1f
                value = PrefsUtils.Settings.deepSearchFileSizeLimit.toFloat() / 1024 / 1024
            }

            val customDialog = CustomDialog()
            customDialog.setTitle("Max file size limit (MB)")
                .setMsg(
                    "Any file larger than "
                            + FileUtils.getFormattedSize(PrefsUtils.Settings.deepSearchFileSizeLimit)
                            + " will be ignored"
                )
                .addView(seekBar)
                .setPositiveButton("Save", {
                    PrefsUtils.Settings.deepSearchFileSizeLimit =
                        seekBar.value.toLong() * 1024 * 1024
                    deepSearchSizeLimitValue.text = FileUtils.getFormattedSize(
                        PrefsUtils.Settings.deepSearchFileSizeLimit,
                        "%.0f"
                    )
                }, true)
                .setNegativeButton("Cancel", null, true)
                .show(supportFragmentManager, "")

            seekBar.addOnChangeListener(Slider.OnChangeListener { _: Slider?, value: Float, _: Boolean ->
                customDialog.setMsg(
                    "Any file larger than "
                            + FileUtils.getFormattedSize((value * 1024 * 1024).toLong(), "%.0f")
                            + " will be ignored"
                )
            })
        }
    }

    private fun setLogMode(mode: String) {
        logModeValue.text = mode
        PrefsUtils.Settings.logMode = mode
    }

    private fun setThemeMode(mode: String) {
        themeModeValue.text = mode
        PrefsUtils.Settings.themeMode = mode
        CoroutineScope(Dispatchers.Main).launch {
            delay(150)
            recreate()
        }
    }

    companion object {
        const val LOG_MODE_DISABLE = "Disable"
        const val LOG_MODE_ERRORS_ONLY = "Errors only"
        const val LOG_MODE_ALL = "All logs"
        const val THEME_MODE_AUTO = "Auto"
        const val THEME_MODE_DARK = "Dark"
        const val THEME_MODE_LIGHT = "Light"
    }
}