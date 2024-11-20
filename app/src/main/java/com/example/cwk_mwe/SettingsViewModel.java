package com.example.cwk_mwe;

import android.content.Context;
import android.content.Intent;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SettingsViewModel extends ViewModel {
    private final MutableLiveData<Integer> playbackSpeed = new MutableLiveData<>(1);

    private Context context;

    public void setContext(Context context) {
        this.context = context;
    }

    public LiveData<Integer> getPlaybackSpeed() {
        return playbackSpeed;
    }

    public void setPlaybackSpeed(int speed) {
        // Placeholder
    }

    public void navigateToHome() {
        if (context != null) {
            Intent intent = new Intent(context, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        }
    }

}
