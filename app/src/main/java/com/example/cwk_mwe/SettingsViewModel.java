package com.example.cwk_mwe;

import android.content.Context;
import android.content.Intent;

import androidx.lifecycle.ViewModel;

public class SettingsViewModel extends ViewModel {
    private AppSharedViewModel appSharedViewModel;
    private Context context;

    public void setContext(Context context, AppSharedViewModel sharedViewModel) {
        this.context = context;
        this.appSharedViewModel = sharedViewModel;
    }

    public void updatePlaybackSpeed(int progress) {if (appSharedViewModel != null) {
        appSharedViewModel.setPlaybackSpeed(progress);
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
