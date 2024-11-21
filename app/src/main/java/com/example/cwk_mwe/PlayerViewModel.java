package com.example.cwk_mwe;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for PlayerActivity, manages playback state, current position, duration,
 * and bookmark creation. Handles communication with the AudioService to control those states.
 */

public class PlayerViewModel extends AndroidViewModel {

    private final MutableLiveData<Integer> currentPosition = new MutableLiveData<>();
    private final MutableLiveData<Integer> duration = new MutableLiveData<>();
    private final MutableLiveData<String> state = new MutableLiveData<>();
    private final MutableLiveData<String> currentFilePath = new MutableLiveData<>();
    private final MutableLiveData<List<BookmarkData>> bookmarks = new MutableLiveData<>(new ArrayList<>());
    private float playbackSpeed = 1.0f;

    private final Context context;

    public PlayerViewModel(Application application) {
        super(application);
        context = application.getApplicationContext();

        // Register for position updates
        LocalBroadcastManager.getInstance(context).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        currentPosition.postValue(intent.getIntExtra("current_position", 0));
                        duration.postValue(intent.getIntExtra("duration", 0));
                    }
                }, new IntentFilter("position_update")
        );

        // Register for state updates
        LocalBroadcastManager.getInstance(context).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        state.postValue(intent.getStringExtra("state"));
                    }
                }, new IntentFilter("player_state_update")
        );
    }

    public LiveData<Integer> getCurrentPosition() {
        return currentPosition;
    }

    public LiveData<Integer> getDuration() {
        return duration;
    }

    public LiveData<String> getState() {
        return state;
    }

    public LiveData<String> getCurrentFilePath() {
        return currentFilePath;
    }

    public float getPlaybackSpeed() {
        return playbackSpeed;
    }

    public void play(String filePath) {
        Intent intent = new Intent(context, AudioService.class);
        intent.setAction(AudioService.ACTION_PLAY);
        intent.putExtra("FILE_PATH", filePath);
        context.startService(intent); // I miss my girlfriend
        currentFilePath.postValue(filePath);
    }

    public void pause() {
        Intent intent = new Intent(context, AudioService.class);
        intent.setAction(AudioService.ACTION_PAUSE);
        context.startService(intent);
    }

    public void stop() {
        Intent intent = new Intent(context, AudioService.class);
        intent.setAction(AudioService.ACTION_STOP);
        context.startService(intent);
    }

    public void skip() {
        Intent intent = new Intent(context, AudioService.class);
        intent.setAction(AudioService.ACTION_SKIP);
        context.startService(intent);
    }
    public void seek(int position) {
        // Used for bookmarks (broken)
        Intent intent = new Intent(context, AudioService.class);
        intent.setAction(AudioService.ACTION_SEEK);
        intent.putExtra("seek_position", position);
        context.startService(intent);
    }
    public void navigateToHome(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    public void addBookmark() {
        String filePath = currentFilePath.getValue();
        String title = filePath.substring(filePath.lastIndexOf("/") + 1);
        int timeStamp = currentPosition.getValue();

        BookmarkData bookmark = new BookmarkData(filePath, timeStamp, title);
        boolean success = BookmarkManager.addBookmark(context, bookmark);
        // Notify user of outcome. User will not see the bookmark be created until they navigate back to Main, so this is important
        if (success) {
            Toast.makeText(context, "Bookmark added successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Failed to add bookmark", Toast.LENGTH_SHORT).show();
        }
    }
}
