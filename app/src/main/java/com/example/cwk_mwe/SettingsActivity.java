package com.example.cwk_mwe;

import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.example.cwk_mwe.databinding.ActivitySettingsBinding;

/**
 * View class for settings, including playback speed and background color rendering.
 * Provides UI components for user interaction and sends changes off to control the application.
 */

public class SettingsActivity extends AppCompatActivity {
    private SettingsViewModel settingsViewModel;
    private GlobalSharedViewModel globalSharedViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        globalSharedViewModel = new ViewModelProvider(this).get(GlobalSharedViewModel.class);
//        globalSharedViewModel.applyBackgroundColor(this, findViewById(android.R.id.content));

        // Data binding setup
        ActivitySettingsBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_settings);
        settingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        settingsViewModel.setContext(this, globalSharedViewModel);
        binding.setViewModel(settingsViewModel);
        binding.setLifecycleOwner(this);

        setupSpeedSeekBar();
//        setupColorSpinner();
    }

    private void setupSpeedSeekBar() {
        // Setup SeekBar for playback speed
        SeekBar speedSeekBar = findViewById(R.id.speed_seekbar);
        speedSeekBar.setMax(4); // Already set in XML, but just in case
        speedSeekBar.setProgress(1); // Default playback speed of 1.0x

        speedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                settingsViewModel.updatePlaybackSpeed(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // No action needed
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // No action needed
            }
        });
    }

////    private void setupColorSpinner() { // Why is it called a spinner and not a dropdown? I don't know, I have have so much caffeine why is this coursework only 25%
////        Spinner colorSpinner = findViewById(R.id.color_spinner);
////        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
////                this,
////                R.array.color_options,
////                android.R.layout.simple_spinner_item
////        );
////        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
////        colorSpinner.setAdapter(adapter);
////
////        // Flag to avoid feedback loop
////        final boolean[] isSpinnerUpdateFromObserver = {false};
////
////        // Observe the background color LiveData
////        globalSharedViewModel.getBackgroundColor().observe(this, color -> {
////            if (!isSpinnerUpdateFromObserver[0]) {
////                // Get the current color name
////                String currentColor = getApplication()
////                        .getSharedPreferences("SettingsPrefs", MODE_PRIVATE)
////                        .getString("background_color", "White");
////
////                int position = adapter.getPosition(currentColor);
////                if (position >= 0 && position != colorSpinner.getSelectedItemPosition()) {
////                    // Only set spinner selection if it has changed
////                    colorSpinner.setSelection(position);
////                }
////            }
////        });
//
//        // Set up a listener for color selection
//        colorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
//                String selectedColor = parent.getItemAtPosition(position).toString();
//
//                // Prevent feedback loop by marking updates from spinner
//                isSpinnerUpdateFromObserver[0] = true;
//                globalSharedViewModel.setBackgroundColor(selectedColor);
//                isSpinnerUpdateFromObserver[0] = false; // Reset the flag
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//                // No action needed
//            }
//        });
//    }
}
