package com.example.cwk_mwe;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Build;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.util.Log;

public class AudioService extends Service {
    public static final String ACTION_PLAY = "com.example.cwk_mwe.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.example.cwk_mwe.ACTION_PAUSE";
    public static final String ACTION_STOP = "com.example.cwk_mwe.ACTION_STOP";
    public static final String ACTION_SEEK = "com.example.cwk_mwe.ACTION_SEEK";
    public static final String CHANNEL_ID = "AudioServiceChannel";
    private AudiobookPlayer audiobookPlayer;
    private Handler handler = new Handler();

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        String filePath = intent.getStringExtra("FILE_PATH");

        if (filePath != null) {
            // Case if file path is provided and all is well
            if (audiobookPlayer == null) {
                // Case if AudiobookPlayer is not initialized, initialize it
                audiobookPlayer = new AudiobookPlayer();
                audiobookPlayer.load(filePath, 1.0f); // Load with normal playback speed
                Log.d("AudioService", "Audio Service Started with file path: " + filePath);
            } else if (!filePath.equals(audiobookPlayer.getFilePath())) {
                // Case if a different file is provided, stop current playback and load the new file
                audiobookPlayer.stop();
                audiobookPlayer.load(filePath, 1.0f); // Load new track with normal playback speed
                Log.d("AudioService", "Audio Service loaded new file path: " + filePath);
            }
        } else if (audiobookPlayer == null) {
            // Case if no file path is provided and AudiobookPlayer is not initialized
            Log.e("AudioService", "No file path provided, and AudiobookPlayer is not initialized.");
            stopSelf();
            return START_NOT_STICKY;
        }

        if (action != null && audiobookPlayer != null) {
            switch (action) {
                case ACTION_PLAY:
                    if (audiobookPlayer.getState() == AudiobookPlayer.AudiobookPlayerState.PAUSED || audiobookPlayer.getState() == AudiobookPlayer.AudiobookPlayerState.STOPPED) {
                        Log.d("AudioService", "Play Command Received");
                        audiobookPlayer.play();
                        startForeground(1, buildNotification("Playing Audio"));
                    } else {
                        Log.d("AudioService", "Audio is already playing (play command ignored)");
                    }
                    break;

                case ACTION_PAUSE:
                    if (audiobookPlayer.getState() == AudiobookPlayer.AudiobookPlayerState.PLAYING) {
                        Log.d("AudioService", "Pause Command Received");
                        audiobookPlayer.pause();
                        stopForeground(false);
                    } else {
                        Log.d("AudioService", "Audio is already paused (pause command ignored)");
                    }
                    break;

                case ACTION_STOP:
                    Log.d("AudioService", "Stop Command Received");
                    audiobookPlayer.stop();
                    stopForeground(true);
                    audiobookPlayer = null; // Release Player instance
                    break;

                case ACTION_SEEK:
                    int seekPosition = intent.getIntExtra("seek_position", 0);
                    Log.d("AudioService", "Seek Command Received" + seekPosition);
                    audiobookPlayer.skipTo(seekPosition);
                    break;

            }
        } else {
            Log.e("AudioService.onStartCommand", "Action or audiobookPlayer is null");
        }

        return START_NOT_STICKY;
    }

    private Notification buildNotification(String contentText) {
        Log.d("AudioService.buildNotification", "Notification Built");
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Galen's MP3 Player")
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_music_notif)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();
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
                Log.d("AudioService.createNotificationChannel", "Notification Channel Created");
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
                int duration = audiobookPlayer.mediaPlayer != null ? audiobookPlayer.mediaPlayer.getDuration() : 0;

                Intent intent = new Intent("position_update");
                intent.putExtra("current_position", currentPosition);
                intent.putExtra("duration", duration);
                LocalBroadcastManager.getInstance(AudioService.this).sendBroadcast(intent);

                // Update position every second
                handler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (audiobookPlayer != null) {
            audiobookPlayer.stop();
            audiobookPlayer = null;
            Log.d("AudioService.onDestroy", "Audio service destroyed");
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
