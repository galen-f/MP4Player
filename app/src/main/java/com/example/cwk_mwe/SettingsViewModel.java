package com.example.cwk_mwe;

import android.content.Context;
import android.content.Intent;

import androidx.lifecycle.ViewModel;

/**
 * ViewModel for SettingsActivity, manages the interaction between the settings
 * and the shared app settings (playback speed and bg color). Provides methods for updating playback
 * speed and background color which are then sent to GlobalSharedViewModel.
 */

public class SettingsViewModel extends ViewModel {
    private GlobalSharedViewModel globalSharedViewModel;
    private Context context;

    public void setContext(Context context, GlobalSharedViewModel sharedViewModel) {
        this.context = context;
        this.globalSharedViewModel = sharedViewModel;
    }

    public void updatePlaybackSpeed(int progress) {
        if (globalSharedViewModel != null) {
            globalSharedViewModel.setPlaybackSpeed(progress);
        }
    }

    public void navigateToHome() { // This is here to enable databinding
        if (context != null) {
            Intent intent = new Intent(context, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        }
    }

}
