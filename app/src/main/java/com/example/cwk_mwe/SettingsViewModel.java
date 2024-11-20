package com.example.cwk_mwe;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SettingsViewModel extends ViewModel {
    private float playbackSpeed = 1.0f;
    private final MutableLiveData<String> selectedColor = new MutableLiveData<>("White");
    private Context context;

    public void setContext(Context context) {
        this.context = context;

        loadSettings();
    }

    public float getPlaybackSpeed() {
        return playbackSpeed;
    }

    public MutableLiveData<String> getSelectedColor() {
        return selectedColor;
    }

    public void updatePlaybackSpeed(int progress) {
        // Map progress to playback speed
        playbackSpeed = progress == 0 ? 0.5f : progress;

        // Apply playback speed immediately
        applyPlaybackSpeed();
    }

    public void updateSelectedColor(String color) {
        selectedColor.setValue(color);
        saveSelectedColor(color);
    }

    private void saveSelectedColor(String color) {
        if (context!= null) {
            SharedPreferences prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("background_color", color);
            editor.apply();
            Log.d("SettingsViewModel", "Saved background color: " + color);
        }
    }

    private void loadSettings() {
        if (context != null) {
            SharedPreferences prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
            playbackSpeed = prefs.getFloat("playback_speed", 1.0f);
            String savedColor = prefs.getString("background_Color", "White");
            selectedColor.setValue(savedColor);
            Log.d("SettingsViewModel", "Settings loaded: playback speed = " + playbackSpeed + ", color = " + savedColor);
        }
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
