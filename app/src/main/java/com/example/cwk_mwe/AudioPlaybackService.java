package com.example.cwk_mwe;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import androidx.core.app.NotificationCompat;

// This class uses the provided AudiobookPlayer class as a helper to control the playback.
// I specifically used this extra class as a way of not touching the code inside of AudiobookPlayer because I wasn't sure if I was allowed to
// So it might seem slightly odd how I've done this but yeah that's why.

public class AudioPlaybackService extends Service {
    private static final String CHANNEL_ID = "AudioPlaybackChannel";
    private AudiobookPlayer audiobookPlayer;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        audiobookPlayer = new AudiobookPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        String filePath = intent.getStringExtra("FILE_PATH");

        if (action != null) {
            switch (action) {
                case "PLAY":
                    startPlayback(filePath);
                    break;
                case "PAUSE":
                    pausePlayback();
                    break;
                case "STOP":
                    stopSelf();
                    break;
            }
        }

        return START_NOT_STICKY;
    }

    private void startPlayback(String filePath) {
        if (filePath != null) {
            audiobookPlayer.load(filePath, 1.0f);
            startForeground(1, createNotification("Playing audio"));
        } else {
            Log.e("AudioPlaybackService", "File path is null");
        }
    }

    private void pausePlayback() {
        audiobookPlayer.pause();
        stopForeground(false); // Notification stays visible, but service is no longer foreground
        updateNotification("Audio paused");
    }

    private Notification createNotification(String contentText) {
        // Intent
        Intent notificationIntent = new Intent(this, PlayerActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Audiobook Player")
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_music_notif)
                .setContentIntent(pendingIntent)
                .build();
    }

    private void updateNotification(String contentText) {
        Notification notification = createNotification(contentText);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Audio Playback Channel",
                NotificationManager.IMPORTANCE_LOW
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        audiobookPlayer.stop();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // Not using bound service
    }
}
