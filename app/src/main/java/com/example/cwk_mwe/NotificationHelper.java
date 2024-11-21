package com.example.cwk_mwe;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

/**
 * Helper class for managing notifications related to audiobook playback. Creates and updates
 * notifications, and provides playback control actions such as play, pause, stop, and skip.
 * This class was created through auto refactoring in the IDE. Comments might look a little funny.
 */

public class NotificationHelper {
    private final AudioService audioService;

    public NotificationHelper(AudioService audioService) {
        this.audioService = audioService;
    }

    Notification buildNotification(String contentText) {
        // Intent to open the main activity when notification is clicked
        Intent notificationIntent = new Intent(audioService, PlayerActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                audioService, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(audioService, AudioService.CHANNEL_ID)
                .setContentTitle("Galen's MP3 Player") // Its me!! I hope this doesn't break any university guidelines but oh well.
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_music_notif)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Check audiobookPlayer state and add appropriate Play/Pause action
        if (audioService.getAudiobookPlayer() != null && audioService.getAudiobookPlayer().getState() == AudiobookPlayer.AudiobookPlayerState.PLAYING) {
            Intent pauseIntent = new Intent(audioService, AudioService.class);
            pauseIntent.setAction(AudioService.ACTION_PAUSE);
            PendingIntent pausePendingIntent = PendingIntent.getService(audioService, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            builder.addAction(R.drawable.ic_pause, "Pause", pausePendingIntent);
        } else if (audioService.getAudiobookPlayer() != null && audioService.getAudiobookPlayer().getState() == AudiobookPlayer.AudiobookPlayerState.PAUSED) {
            Intent playIntent = new Intent(audioService, AudioService.class);
            playIntent.setAction(AudioService.ACTION_PLAY);
            PendingIntent playPendingIntent = PendingIntent.getService(audioService, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            // These drawables don't show up, I am not sure why, I don't really care to figure out because it works without them and they're not in the specs.
            builder.addAction(R.drawable.ic_play, "Play", playPendingIntent);
        } else {
            audioService.checkPlayerState("buildNotification");
        }

        Intent stopIntent = new Intent(audioService, AudioService.class);
        stopIntent.setAction(AudioService.ACTION_STOP);
        PendingIntent stopPendingIntent = PendingIntent.getService(audioService, 1, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        builder.addAction(R.drawable.ic_stop, "Stop", stopPendingIntent);

        Intent skipIntent = new Intent(audioService, AudioService.class);
        skipIntent.setAction(AudioService.ACTION_SKIP);
        PendingIntent skipPendingIntent = PendingIntent.getService(audioService, 2, skipIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        builder.addAction(R.drawable.ic_skip, "Skip", skipPendingIntent);

        return builder.build();
    }

    void updateNotification(String contentText) { // Allows system to change the text in the notification
        Notification notification = buildNotification(contentText);
        NotificationManager notificationManager = (NotificationManager) audioService.getSystemService(AudioService.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(1, notification);
        }
    }

    void createNotificationChannel() { // Necessary but wonky
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    AudioService.CHANNEL_ID,
                    "Audio Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = audioService.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            } else {
                Log.e("AudioService.createNotificationChannel", "Notification Manager is null");
            }
        }
    }
}