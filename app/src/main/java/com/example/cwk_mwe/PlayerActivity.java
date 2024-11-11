package com.example.cwk_mwe;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class PlayerActivity extends AppCompatActivity {
    private Button playButton;
    private Button pauseButton;
    private Button stopButton;
    private SeekBar seekBar;
    private boolean isPlaying = false;
    private String filePath;
    private MediaPlayer startmusic;
    private Handler handler = new Handler();
    private BroadcastReceiver positionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int currentPosition = intent.getIntExtra("current_position", 0);
            int duration = intent.getIntExtra("duration", 0);
            seekBar.setMax(duration);
            seekBar.setProgress(currentPosition);
//            Log.d("PlayerActivity", "Current seekbar position: " + currentPosition + ", Duration: " + duration); // Debug log (triggers every second so left out for now)
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        // Register positionReceiver to update SeekBar
        LocalBroadcastManager.getInstance(this).registerReceiver(positionReceiver,
                new IntentFilter("position_update"));

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

        seekBar = findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                if (fromTouch && startmusic != null && isPlaying) {
                    Intent intent = new Intent(PlayerActivity.this, AudioService.class);
                    intent.setAction(AudioService.ACTION_SEEK);
                    intent.putExtra("seek_position", progress);
                    startService(intent);
                    //Log.d("PlayerActivity", "Seekbar progress (inside loop): " + progress);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(positionReceiver,
                new IntentFilter("position_update"));

        filePath = getIntent().getStringExtra("FILE_PATH");

        playButton = findViewById(R.id.playButton);
        pauseButton = findViewById(R.id.pauseButton);
        stopButton = findViewById(R.id.stopButton);

        playButton.setOnClickListener(v -> togglePlay());
        pauseButton.setOnClickListener(v -> togglePause());
        stopButton.setOnClickListener(v -> stopAudioService());

        // Start the service on creation if a file path is provided
        if (filePath != null) {
            Intent intent = new Intent(this, AudioService.class);
            intent.putExtra("FILE_PATH", filePath);
            //intent.setAction(AudioService.ACTION_PLAY);
            startService(intent);
            isPlaying = true; // Set isPlaying to true since we're starting playback
        } else {
            Toast.makeText(this, "Error: No file selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void togglePlay() {
        if (filePath != null) {
            Intent intent = new Intent(this, AudioService.class);
            if (!isPlaying) {
                intent.putExtra("FILE_PATH", filePath);
                intent.setAction(AudioService.ACTION_PLAY);
                startService(intent);
                isPlaying = true;
            } else {
                Toast.makeText(this, "Audio is already playing", Toast.LENGTH_SHORT).show();
            }
//            startService(intent);
//            isPlaying = !isPlaying;
        }
    }

    private void togglePause() {
        if (filePath != null) {
            Intent intent = new Intent(this, AudioService.class);
            if (isPlaying) {
                intent.setAction(AudioService.ACTION_PAUSE);
                //Log.d("PlayerActivity", "Pause command sent");
                isPlaying = false;
            } else {
                Toast.makeText(this, "Audio is already paused", Toast.LENGTH_SHORT).show();
            }
            startService(intent);
//            isPlaying = !isPlaying;
        }
    }

    private void stopAudioService() {
        Intent intent = new Intent(this, AudioService.class);
        intent.setAction(AudioService.ACTION_STOP);
        startService(intent);
        isPlaying = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAudioService();
    }
}
