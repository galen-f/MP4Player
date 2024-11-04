package com.example.cwk_mwe;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Set the default selected item
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        // Set a listener to handle item selection
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(SettingsActivity.this, HomeActivity.class));
                overridePendingTransition(0, 0); // No animation for smoother switch
                return true;
            } else if (itemId == R.id.nav_player) {
                startActivity(new Intent(SettingsActivity.this, PlayerActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_settings){
                startActivity(new Intent(SettingsActivity.this, SettingsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }
}
