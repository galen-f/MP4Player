package com.example.cwk_mwe;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;

public class SettingsActivity extends AppCompatActivity {
    private SettingsViewModel settingsViewModel;
    private Button homeBtn;
    private SeekBar seekBar;
    private Switch darkModeSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize the ViewModel
        settingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        homeBtn = findViewById(R.id.homeBtn);
        seekBar = findViewById(R.id.seekBar_playback_speed);
        darkModeSwitch = findViewById(R.id.switch_dark_mode);

        // setup observers
        setupObservers();
        setupListeners();
    }

    private void setupObservers() {
        // Darkmode
        settingsViewModel.getDarkModeEnabled().observe(this, isEnabled -> {
            darkModeSwitch.setChecked(isEnabled);
        });

        // Playback speed
        settingsViewModel.getPlaybackSpeed().observe(this, speed -> {
            seekBar.setProgress(speed);
        });
    }

    private void setupListeners() {
        // Darkmode
        darkModeSwitch.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            settingsViewModel.setDarkModeEnabled(isChecked);
        }));

        // Playback Speed
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                settingsViewModel.setPlaybackSpeed(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Home
        homeBtn.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });
    }

}
