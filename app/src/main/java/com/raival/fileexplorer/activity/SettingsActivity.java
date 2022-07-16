package com.raival.fileexplorer.activity;

import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.slider.Slider;
import com.raival.fileexplorer.R;
import com.raival.fileexplorer.common.dialog.CustomDialog;
import com.raival.fileexplorer.common.dialog.OptionsDialog;
import com.raival.fileexplorer.tab.file.util.FileUtils;
import com.raival.fileexplorer.util.PrefsUtils;

import java.util.Objects;

public class SettingsActivity extends BaseActivity {
    public final static String LOG_MODE_DISABLE = "Disable";
    public final static String LOG_MODE_ERRORS_ONLY = "Errors only";
    public final static String LOG_MODE_ALL = "All logs";

    public final static String THEME_MODE_AUTO = "Auto";
    public final static String THEME_MODE_DARK = "Dark";
    public final static String THEME_MODE_LIGHT = "Light";

    private TextView logModeValue;
    private TextView themeModeValue;
    private TextView deepSearchSizeLimitValue;
    private SwitchCompat showBottomToolbarLabels;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        MaterialToolbar materialToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(materialToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        materialToolbar.setNavigationOnClickListener(v -> onBackPressed());

        themeModeValue = findViewById(R.id.settings_theme_value);
        themeModeValue.setText(PrefsUtils.Settings.getThemeMode());
        findViewById(R.id.settings_theme).setOnClickListener(v -> {
            new OptionsDialog("Select theme mode")
                    .addOption(THEME_MODE_AUTO, v1 -> setThemeMode(THEME_MODE_AUTO), true)
                    .addOption(THEME_MODE_DARK, v1 -> setThemeMode(THEME_MODE_DARK), true)
                    .addOption(THEME_MODE_LIGHT, v1 -> setThemeMode(THEME_MODE_LIGHT), true)
                    .show(getSupportFragmentManager(), "");
        });

        showBottomToolbarLabels = findViewById(R.id.settings_bottom_toolbar_labels_value);
        showBottomToolbarLabels.setChecked(PrefsUtils.Settings.getShowBottomToolbarLabels());
        findViewById(R.id.settings_bottom_toolbar_labels).setOnClickListener(v -> {
            showBottomToolbarLabels.setChecked(!showBottomToolbarLabels.isChecked());
            PrefsUtils.Settings.setShowBottomToolbarLabels(showBottomToolbarLabels.isChecked());
        });

        logModeValue = findViewById(R.id.settings_log_mode_value);
        logModeValue.setText(PrefsUtils.Settings.getLogMode());
        findViewById(R.id.settings_log_mode).setOnClickListener(v -> {
            new OptionsDialog("Select log mode")
                    .addOption(LOG_MODE_DISABLE, v1 -> setLogMode(LOG_MODE_DISABLE), true)
                    .addOption(LOG_MODE_ERRORS_ONLY, v1 -> setLogMode(LOG_MODE_ERRORS_ONLY), true)
                    .addOption(LOG_MODE_ALL, v1 -> setLogMode(LOG_MODE_ALL), true)
                    .show(getSupportFragmentManager(), "");
        });

        deepSearchSizeLimitValue = findViewById(R.id.settings_deep_search_limit_value);
        deepSearchSizeLimitValue.setText(FileUtils.getFormattedSize(PrefsUtils.Settings.getDeepSearchFileSizeLimit(), "%.0f"));
        findViewById(R.id.settings_deep_search_limit).setOnClickListener(v -> {
            Slider seekBar = new Slider(this);
            seekBar.setValueFrom(0);
            seekBar.setValueTo(80);
            seekBar.setStepSize(1);
            seekBar.setValue((float) PrefsUtils.Settings.getDeepSearchFileSizeLimit() / 1024 / 1024);

            CustomDialog customDialog = new CustomDialog();
            customDialog.setTitle("Max file size limit (MB)")
                    .setMsg("Any file larger than "
                            + FileUtils.getFormattedSize(PrefsUtils.Settings.getDeepSearchFileSizeLimit())
                            + " will be ignored")
                    .addView(seekBar)
                    .setPositiveButton("Save", (v1 -> {
                        PrefsUtils.Settings.setDeepSearchFileSizeLimit((long) seekBar.getValue() * 1024 * 1024);
                        deepSearchSizeLimitValue.setText(FileUtils.getFormattedSize(PrefsUtils.Settings.getDeepSearchFileSizeLimit(), "%.0f"));
                    }), true)
                    .setNegativeButton("Cancel", null, true)
                    .show(getSupportFragmentManager(), "");

            seekBar.addOnChangeListener((slider, value, fromUser) -> customDialog.setMsg("Any file larger than "
                    + FileUtils.getFormattedSize((long) (value * 1024 * 1024), "%.0f")
                    + " will be ignored"));
        });
    }

    private void setLogMode(String mode) {
        logModeValue.setText(mode);
        PrefsUtils.Settings.setLogMode(mode);
    }

    private void setThemeMode(String mode) {
        themeModeValue.setText(mode);
        PrefsUtils.Settings.setThemeMode(mode);
        new Handler().post(this::recreate);
    }
}
