package com.example.cwk_mwe;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
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

    // Action Variables
    public static final String ACTION_PLAY = "com.example.cwk_mwe.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.example.cwk_mwe.ACTION_PAUSE";
    public static final String ACTION_STOP = "com.example.cwk_mwe.ACTION_STOP";
    public static final String ACTION_SEEK = "com.example.cwk_mwe.ACTION_SEEK";
    public static final String ACTION_SKIP = "com.example.cwk_mwe.ACTION_SKIP";

    // Service Variables
    public static final String CHANNEL_ID = "AudioServiceChannel";
    private AudiobookPlayer audiobookPlayer;
    private Handler handler = new Handler();

    // Playlist variables
    private List<String> playlist = new ArrayList<>();
    private int currentTrackIndex = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        audiobookPlayer = new AudiobookPlayer();
        Log.d("AudioService.onCreate", "AudiobookPlayer created");

        startForeground(1, buildNotification("Ready to play!"));
        checkPlayerState("AudioService.onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        String filePath = intent.getStringExtra("FILE_PATH");

        if (playlist == null || playlist.isEmpty()) {
            playlist = FileUtils.getAudioFiles(); // Load the "playlist"
        }

        handler.post(updateSeekBarRunnable); // Start runnable for the progress bar
        updateNotification("Playing Audio"); // Update notification

        if (filePath != null && (audiobookPlayer.getFilePath() == null || !filePath.equals(audiobookPlayer.getFilePath()))) {
            // Case if a new file path is provided
            playFile(filePath);
        } else if (filePath == null && audiobookPlayer.getFilePath() == null) {
            // Load the first track if no file is playing
            if (!playlist.isEmpty()) {
                playFile(playlist.get(0));
            } else {
                Log.e("AudioService", "Playlist is empty. Stopping service.");
                stopSelf();
                return START_NOT_STICKY;
            }
        } else if (audiobookPlayer == null && filePath != null) {
            // Case if no audiobookPlayer is loaded and a file path is provided
            // This happens if the player is stopped and then started again, not sure why
            playFile(filePath);
        }

        if (action != null && audiobookPlayer != null) {
            switch (action) {
                case ACTION_PLAY:
                    if (audiobookPlayer.getState() == AudiobookPlayer.AudiobookPlayerState.PAUSED || audiobookPlayer.getState() == AudiobookPlayer.AudiobookPlayerState.STOPPED) {
                        audiobookPlayer.play();
                        updateNotification("Playing Audio");
                        checkPlayerState("ACTION_PLAY");
                    } else {
                        Log.d("AudioService", "(play command ignored)");
                        checkPlayerState("ACTION_PLAY");
                    }
                    break;

                case ACTION_PAUSE:
                    if (audiobookPlayer.getState() == AudiobookPlayer.AudiobookPlayerState.PLAYING) {
                        audiobookPlayer.pause();
                        stopForeground(false);
                        updateNotification("Paused Audio");
                        checkPlayerState("ACTION_PAUSE");
                    } else {
                        Log.d("AudioService", "(pause command ignored)");
                        checkPlayerState("ACTION_PAUSE");
                    }
                    break;

                case ACTION_STOP:
                    audiobookPlayer.stop();
                    stopForeground(true);
                    handler.removeCallbacks(updateSeekBarRunnable);
                    checkPlayerState("ACTION_STOP");
                    break;

                case ACTION_SEEK:
                    int seekPosition = intent.getIntExtra("seek_position", 0);
                    Log.d("AudioService", "Seek Command Received" + seekPosition);
                    audiobookPlayer.skipTo(seekPosition);
                    break;

                case ACTION_SKIP:
                    skipTrack();
                    break;
            }
        } else {
            Log.e("AudioService.onStartCommand", "Action or audiobookPlayer is null");
            checkPlayerState("ACTION_ERROR");
        }

        return START_NOT_STICKY;
    }

    private Notification buildNotification(String contentText) {
        Intent playIntent = new Intent(this, AudioService.class);
        playIntent.setAction(ACTION_PLAY);
        PendingIntent playPendingIntent = PendingIntent.getService(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent pauseIntent = new Intent(this, AudioService.class);
        pauseIntent.setAction(ACTION_PAUSE);
        PendingIntent pausePendingIntent = PendingIntent.getService(this, 1, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent stopIntent = new Intent(this, AudioService.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 2, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Galen's MP3 Player")
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_music_notif)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .addAction(R.drawable.ic_play, "Play", playPendingIntent)
                .addAction(R.drawable.ic_pause, "Pause", pausePendingIntent)
                .addAction(R.drawable.ic_stop, "Stop", stopPendingIntent)
                .build();
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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void playFile(String filePath) {
        audiobookPlayer.stop();
        audiobookPlayer.load(filePath, 1.0f);
        audiobookPlayer.play();
        Log.d("AudioService", "Playing file: " + filePath);
    }

    private void skipTrack() {
        if (playlist != null && !playlist.isEmpty()) {
            currentTrackIndex = (currentTrackIndex + 1) % playlist.size();
            playFile(playlist.get(currentTrackIndex));
        } else {
            Log.e("AudioService", "Cannot skip, playlist is empty.");
        }
    }

    public void checkPlayerState(String location){
        if (audiobookPlayer == null) {
            Log.d(location, "Player state is null");
        } else {
            Log.d(location, "Player state: " + audiobookPlayer.getState());
        }
    }
}
