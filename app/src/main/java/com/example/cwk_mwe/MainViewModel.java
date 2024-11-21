package com.example.cwk_mwe;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The ViewModel for MainActivity, managing and exposing data related to audio tracks
 * and bookmarks. Handles permissions, loading tracks from storage, and navigating to settings.
 */

public class MainViewModel extends AndroidViewModel {
    private final MutableLiveData<List<TrackData>> _trackData = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<TrackData>> trackData = _trackData;

    private final MutableLiveData<Boolean> _permissionsGranted = new MutableLiveData<>();
    public LiveData<Boolean> permissionsGranted = _permissionsGranted;

    private final MutableLiveData<List<BookmarkData>> _bookmarks = new MutableLiveData<>();
    public LiveData<List<BookmarkData>> bookmarks = _bookmarks;

    public MainViewModel(@NonNull Application application) {
        super(application);
    }

    public void checkAndSetPermissions(boolean granted) {
        _permissionsGranted.postValue(granted);
        if (granted) {
            loadTracks();
        }
    }

    public void loadTracks() {
        File musicDir = new File(Environment.getExternalStorageDirectory().getPath() + "/Music");
        List<TrackData> tracks = new ArrayList<>();
        if (musicDir.exists() && musicDir.isDirectory()) {
            File[] files = musicDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".mp3")) {
                        tracks.add(new TrackData(file.getName(), file.getAbsolutePath()));
                    }
                }
            }
        }
        _trackData.postValue(tracks); // Update LiveData with the loaded tracks
    }

    public void loadBookmarks() {
        try {
            List<BookmarkData> bookmarks = BookmarkManager.loadBookmarks(getApplication());
            _bookmarks.postValue(bookmarks);
            Log.d("MainViewModel", "Loaded bookmarks button pressed");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void navigateToSettings(Context context) {
        Intent intent = new Intent(context, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
        Log.d("MainViewModel", "Settings button pressed");
    }
}