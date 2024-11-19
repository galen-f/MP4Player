package com.example.cwk_mwe;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SettingsViewModel extends ViewModel {
    private final MutableLiveData<Boolean> darkModeEnabled = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> playbackSpeed = new MutableLiveData<>(1);

    // Getters for LiveData
    public LiveData<Boolean> getDarkModeEnabled() {
        return darkModeEnabled;
    }

    public LiveData<Integer> getPlaybackSpeed() {
        return playbackSpeed;
    }

    // Setters to update values
    public void setDarkModeEnabled(boolean isEnabled) {
        darkModeEnabled.setValue(isEnabled);
    }

    public void setPlaybackSpeed(int speed) {
        playbackSpeed.setValue(speed);
    }
}
