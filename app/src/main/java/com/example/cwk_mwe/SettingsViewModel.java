package com.example.cwk_mwe;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.lifecycle.ViewModel;

public class SettingsViewModel extends ViewModel {
    private float playbackSpeed = 1.0f;
    private Context context;

    public void setContext(Context context) {
        this.context = context;
    }

    public void updatePlaybackSpeedFromSeekBar(int progress) {
        // Map progress to playback speed
        playbackSpeed = progress == 0 ? 0.5f : progress;

        // Apply playback speed immediately
        applyPlaybackSpeed();
    }

    public void applyPlaybackSpeed() {
        if (context != null) {
            Intent intent = new Intent(context, AudioService.class);
            intent.setAction(AudioService.ACTION_SET_SPEED);
            intent.putExtra("speed", playbackSpeed);
            context.startService(intent);
        }
    }

    public void navigateToHome() {
        if (context != null) {
            Intent intent = new Intent(context, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        }
    }

}
