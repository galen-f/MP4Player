package com.example.cwk_mwe;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class PlayerViewModel extends AndroidViewModel {

    private final MutableLiveData<Integer> currentPosition = new MutableLiveData<>();
    private final MutableLiveData<Integer> duration = new MutableLiveData<>();
    private final MutableLiveData<String> state = new MutableLiveData<>();
    private final MutableLiveData<String> currentFilePath = new MutableLiveData<>();

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

    public void play(String filePath) {
        Intent intent = new Intent(context, AudioService.class);
        intent.setAction(AudioService.ACTION_PLAY);
        intent.putExtra("FILE_PATH", filePath);
        context.startService(intent);
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
}
