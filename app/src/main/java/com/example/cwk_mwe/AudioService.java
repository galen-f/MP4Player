package com.example.cwk_mwe;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
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
    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        testNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        String filePath = intent.getStringExtra("FILE_PATH");

        if (filePath != null) {
            Log.d("AudioService", "Audio Service Started with file path: " + filePath);
        } else if (filePath == null) {
            Log.d("AudioService", "No file path provided");
        }

        if (filePath != null && mediaPlayer == null) {
            initializeMediaPlayer(filePath);
        }

        if (action != null) {
            switch (action) {
                case ACTION_PLAY:
                    Log.d("AudioService", "Play Command Received");
                    play();
                    break;
                case ACTION_PAUSE:
                    Log.d("AudioService", "Pause Command Received");
                    pause();
                    break;
                case ACTION_STOP:
                    Log.d("AudioService", "Stop Command Received");
                    stopSelf();
                    break;
                case ACTION_SEEK:
                    Log.d("AudioService", "Seek Command Received");
                    int seekPosition = intent.getIntExtra("seek_position", 0);
                    if (mediaPlayer != null) {
                        mediaPlayer.seekTo(seekPosition);
                    }
                    break;
            }
        }
        return START_NOT_STICKY;
    }

    private void initializeMediaPlayer(String filePath) {
        Log.d("AudioService", "Audio Player Initialized");
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build());
        try {
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
            mediaPlayer.setOnPreparedListener(mp -> {
                // Start sending updates
                handler.post(updateSeekBarRunnable);
            });
        } catch (Exception e) {
            Log.e("AudioService", "Error initializing media player: " + e.getMessage());
            stopSelf();
        }
    }

    private void play() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();

            Notification notification = buildNotification("Playing Audio");

            if (notification != null) {
                startForeground(1, notification);
                Log.d("AudioService.play", "Audio Play, notification built successfully");
            } else {
                Log.e("AudioService.play", "Audio error: notification is null");
            }

        } else if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            Log.e("AudioService.play", "Audio error: mediaPlayer is already playing");
        } else if (mediaPlayer == null) {
            Log.e("AudioService.play", "Audio error: mediaPlayer is null");

        }
    }

    private void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            stopForeground(false);
            Log.d("AudioService.pause", "Audio Pause");
        } else {
            Log.e("AudioService.play", "Audio error");
        }
    }

    private Notification buildNotification(String contentText) {
        Log.d("AudioService.buildNotification", "Notification Built");
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Audio Service")
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
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                int duration = mediaPlayer.getDuration();

                Intent intent = new Intent("position_update");
                intent.putExtra("current_position", currentPosition);
                intent.putExtra("duration", duration);
                LocalBroadcastManager.getInstance(AudioService.this).sendBroadcast(intent);
                handler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
            Log.d("AudioService.onDestroy", "Audio service destroyed");
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void testNotification() {
        Notification notification = buildNotification("Test Notification");
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.notify(1, notification); // Test showing the notification
            Log.d("AudioService", "Test notification sent");
        }
    }
}
