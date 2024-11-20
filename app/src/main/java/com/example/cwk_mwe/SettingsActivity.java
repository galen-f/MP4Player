package com.example.cwk_mwe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

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
        settingsViewModel.setContext(this); // Pass context to ViewModel for navigation
        binding.setViewModel(settingsViewModel);
        binding.setLifecycleOwner(this);
    }
}
