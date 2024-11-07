package com.example.cwk_mwe;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PlayerActivity extends AppCompatActivity {
    private Button playPauseButton;
    private Button stopButton;
    private boolean isPlaying = false;
    private String filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        filePath = getIntent().getStringExtra("FILE_PATH");

        playPauseButton = findViewById(R.id.playPauseButton);
        stopButton = findViewById(R.id.stopButton);

        playPauseButton.setOnClickListener(v -> togglePlayPause());
        stopButton.setOnClickListener(v -> stopAudioService());

        if (filePath == null) {
            Toast.makeText(this, "Error: No file selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void togglePlayPause() {
        if (filePath != null) {
            Intent intent = new Intent(this, AudioService.class);
            if (isPlaying) {
                intent.setAction(AudioService.ACTION_PAUSE);
                playPauseButton.setText("Play");
            } else {
                intent.setAction(AudioService.ACTION_PLAY);
                intent.putExtra("FILE_PATH", filePath);
                playPauseButton.setText("Pause");
            }
            startService(intent);
            isPlaying = !isPlaying;
        }
    }

    private void stopAudioService() {
        Intent intent = new Intent(this, AudioService.class);
        intent.setAction(AudioService.ACTION_STOP);
        startService(intent);
        playPauseButton.setText("Play");
        isPlaying = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAudioService();
    }
}
