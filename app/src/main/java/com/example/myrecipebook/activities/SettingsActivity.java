package com.example.myrecipebook.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.myrecipebook.R;

public class SettingsActivity extends AppCompatActivity {

    private Switch switchNotifications, switchTheme;
    private Button btnSave;

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "MyPrefs";
    private static final String KEY_NOTIFICATIONS = "notifications";
    private static final String KEY_THEME = "theme";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize views
        switchNotifications = findViewById(R.id.switch_notifications);
        switchTheme = findViewById(R.id.switch_theme);
        btnSave = findViewById(R.id.btn_save);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Load saved settings
        loadSettings();

        // Save button click listener
        btnSave.setOnClickListener(v -> saveSettings());
    }

    private void loadSettings() {
        // Load notification preference
        boolean notificationsEnabled = sharedPreferences.getBoolean(KEY_NOTIFICATIONS, true);
        switchNotifications.setChecked(notificationsEnabled);

        // Load theme preference
        boolean isDarkTheme = sharedPreferences.getBoolean(KEY_THEME, false);
        switchTheme.setChecked(isDarkTheme);

        // Apply theme immediately
        applyTheme(isDarkTheme);
    }

    private void saveSettings() {
        // Save notification preference
        boolean notificationsEnabled = switchNotifications.isChecked();
        sharedPreferences.edit().putBoolean(KEY_NOTIFICATIONS, notificationsEnabled).apply();

        // Save theme preference
        boolean isDarkTheme = switchTheme.isChecked();
        sharedPreferences.edit().putBoolean(KEY_THEME, isDarkTheme).apply();

        // Apply theme immediately
        applyTheme(isDarkTheme);

        // Show confirmation message
        Toast.makeText(this, "Settings saved!", Toast.LENGTH_SHORT).show();
    }

    private void applyTheme(boolean isDarkTheme) {
        if (isDarkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}