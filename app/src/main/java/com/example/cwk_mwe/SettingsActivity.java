package com.example.cwk_mwe;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.example.cwk_mwe.databinding.ActivitySettingsBinding;


public class SettingsActivity extends AppCompatActivity {
    private SettingsViewModel settingsViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Data binding setup
        ActivitySettingsBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_settings);
        settingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        settingsViewModel.setContext(this);
        binding.setViewModel(settingsViewModel);
        binding.setLifecycleOwner(this);


        // Set button listener to apply playback speed
        EditText playbackSpeedEditText = findViewById(R.id.editText_playback_speed);
        findViewById(R.id.playbackBtn).setOnClickListener(v -> {
            String speedInput = playbackSpeedEditText.getText().toString();
            settingsViewModel.updatePlaybackSpeedInput(speedInput);
            settingsViewModel.applyPlaybackSpeed();
            Log.d("SettingsActivity", "Playback speed input: " + speedInput);
        });
    }
}
