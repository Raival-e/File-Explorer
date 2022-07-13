package com.raival.fileexplorer.activity;

import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.raival.fileexplorer.R;
import com.raival.fileexplorer.common.dialog.OptionsDialog;
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
