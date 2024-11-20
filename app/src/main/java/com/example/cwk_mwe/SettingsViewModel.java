package com.example.cwk_mwe;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SettingsViewModel extends ViewModel {
    private final MutableLiveData<String> playbackSpeedInput = new MutableLiveData<>();
    private Context context;

    public void setContext(Context context) {
        this.context = context;
    }

    public LiveData<String> getPlaybackSpeedInput() {
        return playbackSpeedInput;
    }

    public void updatePlaybackSpeedInput(String speed) {
        playbackSpeedInput.setValue(speed);
        Log.d("SettingsViewModel", "Playback speed input updated: " + speed);
    }

    public void applyPlaybackSpeed() {
        if (context != null && playbackSpeedInput.getValue() != null) {
            try {
                float speed = Float.parseFloat(playbackSpeedInput.getValue());
                Intent intent = new Intent(context, AudioService.class);
                intent.setAction(AudioService.ACTION_SET_SPEED);
                intent.putExtra("speed", speed);
                context.startService(intent);
                Log.d("SettingsViewModel", "Playback speed sent to AudioService: " + speed);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
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
