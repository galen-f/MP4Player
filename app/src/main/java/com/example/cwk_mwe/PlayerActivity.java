package com.example.cwk_mwe;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.example.cwk_mwe.databinding.ActivityPlayerBinding;

public class PlayerActivity extends AppCompatActivity {

    private PlayerViewModel playerViewModel;
    private SeekBar seekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        ActivityPlayerBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_player);

        // Initialize ViewModel
        playerViewModel = new ViewModelProvider(this).get(PlayerViewModel.class);
        binding.setViewModel(playerViewModel);
        binding.setLifecycleOwner(this);

        // If playback is stopped, return the user home. This helps to avoid any funky issues with a player with no loaded file.
        playerViewModel.getState().observe(this, state -> {
            if ("stopped".equals(state)) {
                Toast.makeText(this, "Playback stopped: returned to home", Toast.LENGTH_SHORT).show();
                navigateToHome();
            }
        });

        // Play Audio as soon as the activity is created
        playAudio();
    }

    private void playAudio() {
        String filePath = getIntent().getStringExtra("FILE_PATH");
        if (filePath != null) {
            playerViewModel.play(filePath);
            long timestampLong = getIntent().getLongExtra("TIMESTAMP", 0);
            int timestamp = (int) timestampLong;
            if (timestamp > 0) {
                playerViewModel.seek(timestamp);
            }
        } else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
        }

        // Request updated playback speed from AudioService
        float playbackSpeed = playerViewModel.getPlaybackSpeed(); // Add getter in ViewModel
        Intent intent = new Intent(this, AudioService.class);
        intent.setAction(AudioService.ACTION_SET_SPEED);
        intent.putExtra("speed", playbackSpeed);
        startService(intent);
    }

    private void navigateToHome() {
        // Navigate to home activity
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
