package com.example.cwk_mwe;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * A foreground service that manages audiobook playback in the background. Handles all service related features in one spot
 */

public class AudioService extends Service {
    // Action Variables
    public static final String ACTION_PLAY = "com.example.cwk_mwe.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.example.cwk_mwe.ACTION_PAUSE";
    public static final String ACTION_STOP = "com.example.cwk_mwe.ACTION_STOP";
    public static final String ACTION_SEEK = "com.example.cwk_mwe.ACTION_SEEK";
    public static final String ACTION_SKIP = "com.example.cwk_mwe.ACTION_SKIP";
    public static final String ACTION_SET_SPEED = "com.example.cwk_mwe.ACTION_SET_SPEED";

    // Service Variables
    public static final String CHANNEL_ID = "AudioServiceChannel";
    private final NotificationHelper notificationHelper = new NotificationHelper(this);
    private AudiobookPlayer audiobookPlayer;
    private Handler handler = new Handler();

    // Playlist variables
    private List<String> playlist = new ArrayList<>();
    private int currentTrackIndex = 0;

    // Settings Variabels
    private float currentPlaybackSpeed = 1.0f;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationHelper.createNotificationChannel();

        // Settings shared preferences
        SharedPreferences prefs = getSharedPreferences("PlaybackPrefs", MODE_PRIVATE);
        currentPlaybackSpeed = prefs.getFloat("playbackSpeed", 1.0f);

        audiobookPlayer = new AudiobookPlayer();
        Log.d("AudioService.onCreate", "AudiobookPlayer created");

        startForeground(1, notificationHelper.buildNotification("Ready to play!"));
        checkPlayerState("AudioService.onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        String filePath = intent.getStringExtra("FILE_PATH");

        if (ACTION_SET_SPEED.equals(action)) {
            float speed = intent.getFloatExtra("speed", 1.0f);
            setPlaybackSpeed(speed);
        }

        if (playlist == null || playlist.isEmpty()) {
            playlist = FileUtils.getAudioFiles(); // Load the "playlist"
        }

        if (filePath == null && (audiobookPlayer.getFilePath() == null || audiobookPlayer.getState() == AudiobookPlayer.AudiobookPlayerState.STOPPED)) {
            if (playlist != null && !playlist.isEmpty()) {
                currentTrackIndex = 0; // Ensure it starts with the first track
                filePath = playlist.get(currentTrackIndex);
                Log.d("AudioService", "No file path provided and no track loaded. Playing first song in the playlist: " + filePath);
            } else {
                Log.w("AudioService", "Playlist is empty. Cannot play a track.");
            }
        }

        handler.post(updateSeekBarRunnable); // Start runnable for the progress bar
        notificationHelper.updateNotification("Playing Audio"); // Update notification

        if (audiobookPlayer == null) {
            audiobookPlayer = new AudiobookPlayer();
            Log.d("AudioService", "AudiobookPlayer was null, reinitialized.");
        }
        if (filePath != null) {
            // If a file path is provided, play it if it's new or the player was stopped
            if (!filePath.equals(audiobookPlayer.getFilePath())) {
                playFile(filePath);
                // No log because playFile has a log
            }
        }

        if (action != null && audiobookPlayer != null) {
            switch (action) {
                case ACTION_PLAY:
                    if (audiobookPlayer.getState() == AudiobookPlayer.AudiobookPlayerState.PAUSED || audiobookPlayer.getState() == AudiobookPlayer.AudiobookPlayerState.STOPPED) {
                        audiobookPlayer.play();
                        notificationHelper.updateNotification("Playing Audio");
                        checkPlayerState("ACTION_PLAY");
                    }
                    break;

                case ACTION_PAUSE:
                    if (audiobookPlayer.getState() == AudiobookPlayer.AudiobookPlayerState.PLAYING) {
                        audiobookPlayer.pause();
                        notificationHelper.updateNotification("Paused Audio");
                        checkPlayerState("ACTION_PAUSE");
                    }
                    break;

                case ACTION_STOP:
                    audiobookPlayer.stop();
                    checkPlayerState("ACTION_STOP");
                    stopForeground(true);
                    broadcastStoppedState(); // here in case the service is stopped from notification - notify the activity
                    stopSelf();
                    break;

                case ACTION_SEEK:
                    int seekPosition = intent.getIntExtra("seek_position", 0);
                    Log.d("AudioService", "Seek Command Received, seeking to: " + seekPosition);
                    audiobookPlayer.skipTo(seekPosition);
                    break;

                case ACTION_SKIP:
                    skipTrack();
                    break;
            }
        } else if (audiobookPlayer == null) {
            Log.e("AudioService.onStartCommand", "AudiobookPlayer is null");
            checkPlayerState("AudioService.ACTION_ERROR");
        } else {
            Log.d("AudioService.onStartCommand", "Action is null"); // This gets triggered on startup, don't worry, theres just no command on startup lol
            checkPlayerState("AudioService.ACTION_ERROR");
        }

        return START_NOT_STICKY;
    }

    public AudiobookPlayer getAudiobookPlayer() { // For notificationBuilder
        return audiobookPlayer;
    }

    private Runnable updateSeekBarRunnable = new Runnable() {
        @Override
        public void run() {
            if (audiobookPlayer != null && audiobookPlayer.getState() == AudiobookPlayer.AudiobookPlayerState.PLAYING) {
                int currentPosition = audiobookPlayer.getProgress();
                int duration = audiobookPlayer.mediaPlayer != null ? audiobookPlayer.mediaPlayer.getDuration() : 0;  // Any other way of doing this?

                Intent intent = new Intent("position_update");
                intent.putExtra("current_position", currentPosition);
                intent.putExtra("duration", duration);
                LocalBroadcastManager.getInstance(AudioService.this).sendBroadcast(intent);

                // Update position every second
                handler.postDelayed(this, 1000);
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void setPlaybackSpeed(float speed) {
        currentPlaybackSpeed = speed;
        if (audiobookPlayer != null && audiobookPlayer.getState() == AudiobookPlayer.AudiobookPlayerState.PLAYING) {
            audiobookPlayer.setPlaybackSpeed(speed);
        }
        Log.d("AudioService", "Playback speed updated: " + speed);

        /*
        * Because of the AudiobookPlayer class, changing speed immediately plays the mediaPlayer.
        * This is dumb for obvious reasons but the specs say I can't change the AudiobookPlayer
        * I have been trying for a solid 2 hours to fix the issue here, but its always glitchy
        * so if you change the speed while no track is playing, it just plays the first track
        * I really don't like this because I could easily fix it but itd be against the specs
        * We should be allowed to create our own mediaPlayer wrapper class.
         */
    }

    private void playFile(String filePath) {
        audiobookPlayer.stop();
        audiobookPlayer.load(filePath, currentPlaybackSpeed);
        audiobookPlayer.play();
        notificationHelper.updateNotification("Playing Audio");
        Log.d("AudioService", "Playing file: " + filePath);
    }

    private void skipTrack() {
        if (playlist != null && !playlist.isEmpty()) {
            // Variables to find the location in the "playlist"
            String currentFilePath = audiobookPlayer.getFilePath();
            int currentIndex = playlist.indexOf(currentFilePath);

            if (currentIndex != -1) {
                // Play the next track in the playlist, wrap around playlist if at end
                currentTrackIndex = (currentIndex + 1) % playlist.size();
                playFile(playlist.get(currentTrackIndex));
            } else {
                Log.w("AudioService", "Current file not found in playlist, playing first track...");
                currentTrackIndex = 0;
                playFile(playlist.get(currentTrackIndex));
            }
        } else {
            Log.e("AudioService", "Cannot skip, playlist is empty.");
        }
    }

    private void broadcastStoppedState() {
        Intent intent = new Intent("player_state_update");
        intent.putExtra("state", "stopped");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public void checkPlayerState(String location){
        // This method exists for bug fixing
        if (audiobookPlayer == null) {
            Log.d(location, "Player state is null");
        } else {
            Log.d(location, "Player state: " + audiobookPlayer.getState());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Save playback speed to preferences before destruction
        SharedPreferences prefs = getSharedPreferences("PlaybackPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat("playback_speed", currentPlaybackSpeed);
        editor.apply();

        // Remove callbacks to prevent memory leaks
        handler.removeCallbacks(updateSeekBarRunnable);
        audiobookPlayer.stop(); // Stop and release resources in AudiobookPlayer
        Log.d("AudioService", "Service destroyed");
    }
}
