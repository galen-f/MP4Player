package com.example.cwk_mwe;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class PlayerActivity extends AppCompatActivity {
    private AudiobookPlayer audiobookPlayer;
    private Button playPauseButton;
    private Button stopButton;
    private Button skipButton;
    private String filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        filePath = getIntent().getStringExtra("FILE_PATH");

        audiobookPlayer = new AudiobookPlayer();

        // Retrieve file path from intent
        String filePath = getIntent().getStringExtra("FILE_PATH");
        if (filePath != null) {
            audiobookPlayer.load(filePath, 1.0f); // Load file with normal playback speed
        } else {
            Toast.makeText(this, "Error: No file selected", Toast.LENGTH_SHORT).show();
        }

        // Initialize UI elements
        playPauseButton = findViewById(R.id.playPauseButton);
        stopButton = findViewById(R.id.stopButton);
        skipButton = findViewById(R.id.skipButton);

        // Button Listeners
        setupButtonListeners();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Set the selected item (navbar)
        bottomNavigationView.setSelectedItemId(R.id.nav_player);

        // Set a listener to handle item selection
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(PlayerActivity.this, MainActivity.class));
                overridePendingTransition(0, 0); // No animation for smoother switch
                return true;
            } else if (itemId == R.id.nav_player) {
                startActivity(new Intent(PlayerActivity.this, PlayerActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_settings) {
                startActivity(new Intent(PlayerActivity.this, SettingsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });

    }

        private void setupButtonListeners() {
            // Play/Pause button logic
            playPauseButton.setOnClickListener(v -> {
                if (playPauseButton.getText().equals("Play")) {
                    startAudioService("PLAY");
                    playPauseButton.setText("Pause");
                } else {
                    startAudioService("PAUSE");
                    playPauseButton.setText("Play");
                }
            });

            // Stop button logic
            stopButton.setOnClickListener(v -> {
                startAudioService("STOP");
                playPauseButton.setText("Play");
                Toast.makeText(this, "Playback stopped", Toast.LENGTH_SHORT).show();
            });

            // Skip button logic
            skipButton.setOnClickListener(v -> {
                Toast.makeText(this, "Skip to next track feature coming soon", Toast.LENGTH_SHORT).show();
            });
        }

        private void startAudioService(String action) {
            if (filePath == null) {
                Toast.makeText(this, "No file selected for playback", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent serviceIntent = new Intent(this, AudioPlaybackService.class);
            serviceIntent.setAction(action);
            serviceIntent.putExtra("FILE_PATH", filePath);
            startService(serviceIntent);
        }

        @Override
        protected void onDestroy() {
            super.onDestroy();
            if (audiobookPlayer != null) {
                audiobookPlayer.stop(); // Stop and release media player resources
            }
        }
    }
