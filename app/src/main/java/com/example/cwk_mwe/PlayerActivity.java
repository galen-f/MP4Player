package com.example.cwk_mwe;

import android.content.Intent;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

public class PlayerActivity extends AppCompatActivity {

    private PlayerViewModel playerViewModel;
    private SeekBar seekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        // Initialize ViewModel
        playerViewModel = new ViewModelProvider(this).get(PlayerViewModel.class);

        // UI Components Initialization
        findViewById(R.id.homeBtn).setOnClickListener(v -> navigateToHome());
        findViewById(R.id.playBtn).setOnClickListener(v -> playAudio());
        findViewById(R.id.pauseBtn).setOnClickListener(v -> pauseAudio());
        findViewById(R.id.stopBtn).setOnClickListener(v -> stopAudio());
        findViewById(R.id.skipBtn).setOnClickListener(v -> skipAudio());
        findViewById(R.id.bookmarkBtn).setOnClickListener(v -> addBookmark());

        seekBar = findViewById(R.id.seekBar);

        // Observe LiveData from PlayerViewModel
        playerViewModel.getCurrentPosition().observe(this, position -> seekBar.setProgress(position));
        playerViewModel.getDuration().observe(this, duration -> seekBar.setMax(duration));
        playerViewModel.getState().observe(this, state -> {
            if ("stopped".equals(state)) {
                Toast.makeText(this, "Playback stopped: returned to home", Toast.LENGTH_SHORT).show();
                navigateToHome();
            }
        });
    }

    private void playAudio() {
        String filePath = getIntent().getStringExtra("FILE_PATH");
        if (filePath != null) {
            playerViewModel.play(filePath);
        } else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void pauseAudio() {
        playerViewModel.pause();
    }

    private void stopAudio() {
        playerViewModel.stop();
    }

    private void skipAudio() {
        playerViewModel.skip();
    }

    private void addBookmark() {
        int position = seekBar.getProgress();
        // Handle bookmark logic here (Broken)
    }

    private void navigateToHome() {
        // Navigate to home activity
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
