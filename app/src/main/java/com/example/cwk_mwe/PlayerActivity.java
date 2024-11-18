package com.example.cwk_mwe;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.File;

public class PlayerActivity extends AppCompatActivity {
    private Button homeBtn, playBtn, pauseBtn, stopBtn, skipBtn, bookmarkBtn;
    private SeekBar seekBar;
    private boolean isPlaying = false, isStopped = false;
    private String filePath, title;
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

        homeBtn = findViewById(R.id.homeBtn);
        playBtn = findViewById(R.id.playBtn);
        pauseBtn = findViewById(R.id.pauseBtn);
        stopBtn = findViewById(R.id.stopBtn);
        skipBtn = findViewById(R.id.skipBtn);
        bookmarkBtn = findViewById(R.id.bookmarkBtn);

        seekBar = findViewById(R.id.seekBar);

        // Register positionReceiver to update SeekBar
        LocalBroadcastManager.getInstance(this).registerReceiver(positionReceiver,
                new IntentFilter("position_update"));

        filePath = getIntent().getStringExtra("FILE_PATH");
        title = filePath != null ? new File(filePath).getName() : "Unknown";

        homeBtn.setOnClickListener(v -> {
                    Intent intent = new Intent(PlayerActivity.this, MainActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                });
        playBtn.setOnClickListener(v -> audioPlay());
        pauseBtn.setOnClickListener(v -> audioPause());
        stopBtn.setOnClickListener(v -> stopAudioService());
        skipBtn.setOnClickListener(v -> audioSkip());
        bookmarkBtn.setOnClickListener(v -> addBookmark());

        // Start the service on creation if a file path is provided
        if (filePath != null) {
            Intent intent = new Intent(this, AudioService.class);
            intent.putExtra("FILE_PATH", filePath);
            //intent.setAction(AudioService.ACTION_PLAY);
            startService(intent);
            isPlaying = true;
        } else {
            Toast.makeText(this, "Error: No file selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void audioPlay() {
        if (filePath != null) {
            Intent intent = new Intent(this, AudioService.class);
            if (isStopped) {
                Toast.makeText(this, "Audio has been stopped, please load new track", Toast.LENGTH_SHORT).show();
            } else if (!isPlaying && !isStopped) {
                intent.putExtra("FILE_PATH", filePath);
                intent.setAction(AudioService.ACTION_PLAY);
                startService(intent);
                isPlaying = true;
            } else {
                Toast.makeText(this, "Audio is already playing", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No file path selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void audioPause() {
        if (filePath != null) {
            Intent intent = new Intent(this, AudioService.class);
            if (isStopped) {
                Toast.makeText(this, "Audio has been stopped, please load new track", Toast.LENGTH_SHORT).show();
            } else if (isPlaying && !isStopped) {
                intent.setAction(AudioService.ACTION_PAUSE);
                //Log.d("PlayerActivity", "Pause command sent");
                isPlaying = false;
            } else {
                Toast.makeText(this, "Audio is already paused", Toast.LENGTH_SHORT).show();
            }
            startService(intent);
        } else {
            Toast.makeText(this, "No file path selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void audioSkip() {
        if (filePath != null) {
            Intent intent = new Intent(this, AudioService.class);
            intent.setAction(AudioService.ACTION_SKIP);
            startService(intent);
            isPlaying = true;
            isStopped = false;
            Toast.makeText(this, "Skipped to next track", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No file path selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopAudioService() {
        Intent intent = new Intent(this, AudioService.class);
        intent.setAction(AudioService.ACTION_STOP);
        startService(intent);
        isStopped = true;
    }

    private void addBookmark() {
        int currentPosition = seekBar.getProgress();
        BookmarkData bookmark = new BookmarkData(filePath, currentPosition, title);

        boolean success = BookmarkManager.addBookmark(this, bookmark);
        if (success) {
            Toast.makeText(this, "Bookmark added successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to add bookmark", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAudioService();
    }
}
