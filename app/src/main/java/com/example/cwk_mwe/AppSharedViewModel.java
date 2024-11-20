package com.example.cwk_mwe;

import android.app.Application;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class AppSharedViewModel extends AndroidViewModel {
    private final MutableLiveData<Integer> backgroundColor = new MutableLiveData<>(Color.WHITE);

    public AppSharedViewModel(@NonNull Application application) {
        super(application);
        loadBackgroundColor();
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
}
