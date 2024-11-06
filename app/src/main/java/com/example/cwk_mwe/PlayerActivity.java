package com.example.cwk_mwe;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class PlayerActivity extends AppCompatActivity {
    private AudiobookPlayer audiobookPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        audiobookPlayer = new AudiobookPlayer();

        // Retrieve file path from intent
        String filePath = getIntent().getStringExtra("FILE_PATH");
        if (filePath != null) {
            audiobookPlayer.load(filePath, 1.0f); // Load file with normal playback speed
        } else {
            Toast.makeText(this, "Error: No file selected", Toast.LENGTH_SHORT).show();
        }

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
            } else if (itemId == R.id.nav_settings){
                startActivity(new Intent(PlayerActivity.this, SettingsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }
}
