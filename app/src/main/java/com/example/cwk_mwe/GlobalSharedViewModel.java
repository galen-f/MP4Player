package com.example.cwk_mwe;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class GlobalSharedViewModel extends AndroidViewModel {
    private final MutableLiveData<Integer> backgroundColor = new MutableLiveData<>(Color.WHITE);
    private final MutableLiveData<Float> playbackSpeed = new MutableLiveData<>(1.0f);

    public GlobalSharedViewModel(@NonNull Application application) {
        super(application);
        loadBackgroundColor();
        loadPlaybackSpeed();
    }

    public LiveData<Integer> getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String color) {
        int parsedColor;
        switch (color) {
            case "Red":
                parsedColor = Color.RED;
                break;
            case "Blue":
                parsedColor = Color.BLUE;
                break;
            case "Green":
                parsedColor = Color.GREEN;
                break;
            case "Yellow":
                parsedColor = Color.YELLOW;
                break;
            default:
                parsedColor = Color.WHITE;
        }
        backgroundColor.setValue(parsedColor);
        saveBackgroundColor(color);
    }

    private void loadBackgroundColor() {
        String color = getApplication()
                .getSharedPreferences("SettingsPrefs", Application.MODE_PRIVATE)
                .getString("background_color", "White");

        setBackgroundColor(color);
    }

    private void saveBackgroundColor(String color) {
        getApplication()
                .getSharedPreferences("SettingsPrefs", Application.MODE_PRIVATE)
                .edit()
                .putString("background_color", color)
                .apply();
    }


    public LiveData<Float> getPlaybackSpeed() {
        return playbackSpeed;
    }

    void setPlaybackSpeed(int progress) {
        float speed = progress == 0 ? 0.5f : progress;
        playbackSpeed.setValue(speed);
        savePlaybackSpeed();
        applyPlaybackSpeed();
    }

    private void loadPlaybackSpeed() {
        float speed = getApplication()
                .getSharedPreferences("SettingsPrefs", Application.MODE_PRIVATE)
                .getFloat("playback_speed", 1.0f);

        playbackSpeed.setValue(speed);
    }

    private void savePlaybackSpeed() {
        getApplication()
                .getSharedPreferences("SettingsPrefs", Application.MODE_PRIVATE)
                .edit()
                .putFloat("playback_speed", playbackSpeed.getValue())
                .apply();
    }

    private void applyPlaybackSpeed() {
        Context context = getApplication().getApplicationContext();
        Intent intent = new Intent(context, AudioService.class);
        intent.setAction(AudioService.ACTION_SET_SPEED);
        intent.putExtra("speed", playbackSpeed.getValue());
        context.startService(intent);
    }

    /**
     * Observes background color changes and applies it dynamically to the given view.
     */
    public void applyBackgroundColor(LifecycleOwner owner, View targetView) {
        getBackgroundColor().observe(owner, color -> {
            if (color != null) {
                targetView.setBackgroundColor(color);
            }
        });
    }
}
