package com.example.cwk_mwe;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Build;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class AudioService extends Service {

    // TODO:
    // - Not MVVM adherent

    // Action Variables
    public static final String ACTION_PLAY = "com.example.cwk_mwe.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.example.cwk_mwe.ACTION_PAUSE";
    public static final String ACTION_STOP = "com.example.cwk_mwe.ACTION_STOP";
    public static final String ACTION_SEEK = "com.example.cwk_mwe.ACTION_SEEK";
    public static final String ACTION_SKIP = "com.example.cwk_mwe.ACTION_SKIP";
    public static final String ACTION_SET_SPEED = "com.example.cwk_mwe.ACTION_SET_SPEED";

    // Service Variables
    public static final String CHANNEL_ID = "AudioServiceChannel";
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
        createNotificationChannel();

        // Settings shared preferences
        SharedPreferences prefs = getSharedPreferences("PlaybackPrefs", MODE_PRIVATE);
        currentPlaybackSpeed = prefs.getFloat("playbackSpeed", 1.0f);

        audiobookPlayer = new AudiobookPlayer();
        Log.d("AudioService.onCreate", "AudiobookPlayer created");

        startForeground(1, buildNotification("Ready to play!"));
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

        handler.post(updateSeekBarRunnable); // Start runnable for the progress bar
        updateNotification("Playing Audio"); // Update notification

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
                        updateNotification("Playing Audio");
                        checkPlayerState("ACTION_PLAY");
                    } else {
                        Log.w("AudioService", "(play command ignored)");
                        checkPlayerState("ACTION_PLAY");
                    }
                    break;

                case ACTION_PAUSE:
                    if (audiobookPlayer.getState() == AudiobookPlayer.AudiobookPlayerState.PLAYING) {
                        audiobookPlayer.pause();
                        updateNotification("Paused Audio");
                        checkPlayerState("ACTION_PAUSE");
                    } else {
                        Log.w("AudioService", "(pause command ignored)");
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

    private Notification buildNotification(String contentText) {
        // Intent to open the main activity when notification is clicked
        Intent notificationIntent = new Intent(this, PlayerActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Galen's MP3 Player")
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_music_notif)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Check audiobookPlayer state and add appropriate Play/Pause action
        if (audiobookPlayer != null && audiobookPlayer.getState() == AudiobookPlayer.AudiobookPlayerState.PLAYING) {
            Intent pauseIntent = new Intent(this, AudioService.class);
            pauseIntent.setAction(ACTION_PAUSE);
            PendingIntent pausePendingIntent = PendingIntent.getService(this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            builder.addAction(R.drawable.ic_pause, "Pause", pausePendingIntent);
        } else if (audiobookPlayer != null && audiobookPlayer.getState() == AudiobookPlayer.AudiobookPlayerState.PAUSED) {
            Intent playIntent = new Intent(this, AudioService.class);
            playIntent.setAction(ACTION_PLAY);
            PendingIntent playPendingIntent = PendingIntent.getService(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            builder.addAction(R.drawable.ic_play, "Play", playPendingIntent);
        } else {
            checkPlayerState("buildNotification");
        }

        Intent stopIntent = new Intent(this, AudioService.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 1, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        builder.addAction(R.drawable.ic_stop, "Stop", stopPendingIntent);

        Intent skipIntent = new Intent(this, AudioService.class);
        skipIntent.setAction(ACTION_SKIP);
        PendingIntent skipPendingIntent = PendingIntent.getService(this, 2, skipIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        builder.addAction(R.drawable.ic_skip, "Skip", skipPendingIntent);

        return builder.build();
    }



    private void updateNotification(String contentText) {
        Notification notification = buildNotification(contentText);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(1, notification);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Audio Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
                //Log.d("AudioService.createNotificationChannel", "Notification Channel Created");
            } else {
                Log.e("AudioService.createNotificationChannel", "Notification Manager is null");
            }
        }
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
    }

    private void playFile(String filePath) {
        audiobookPlayer.stop();
        audiobookPlayer.load(filePath, currentPlaybackSpeed);
        audiobookPlayer.play();
        updateNotification("Playing Audio");
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
