package com.example.cwk_mwe;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.os.Environment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

//TODO:
// - Add Color Changer
// - Add Playback speed settings
// - Bookmarks feature
// - Auto create "queue"
// - Skip Track feature

public class MainActivity extends AppCompatActivity {
    private Button settingsBtn;
    private RecyclerView recyclerView;
    private MusicRecyclerViewAdapter adapter;
    private List<TrackData> trackData = new ArrayList<>();
    private static final int MY_PERMISSIONS_REQUEST_READ_MEDIA_AUDIO_AND_NOTIFICATIONS = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settingsBtn = findViewById(R.id.settingsBtn);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        settingsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        // Set adapter with empty list initially
        adapter = new MusicRecyclerViewAdapter(this, trackData, filePath -> {
            // Handle click, pass filePath to PlayerActivity
            Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
            intent.putExtra("FILE_PATH", filePath);
            //Log.d("MainActivity", "Passing file path to PlayerActivity: " + filePath);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        checkAndRequestPermissions();
    }

    private void checkAndRequestPermissions() {
        // List of permissions to request
        List<String> permissionsToRequest = new ArrayList<>();

        // Check READ_MEDIA_AUDIO permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.READ_MEDIA_AUDIO);
        }

        // Check POST_NOTIFICATIONS permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS);
        }

        // Request permissions if any are missing
        if (!permissionsToRequest.isEmpty()) {
            // Show rationale if needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_AUDIO) ||
                    (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS))) {

                new AlertDialog.Builder(this)
                        .setTitle("Permissions Needed")
                        .setMessage("These permissions are needed to access music files and display notifications.")
                        .setPositiveButton("OK", (dialog, which) -> ActivityCompat.requestPermissions(MainActivity.this,
                                permissionsToRequest.toArray(new String[0]),
                                MY_PERMISSIONS_REQUEST_READ_MEDIA_AUDIO_AND_NOTIFICATIONS))
                        .create()
                        .show();
            } else {
                // Request the permissions directly
                ActivityCompat.requestPermissions(this,
                        permissionsToRequest.toArray(new String[0]),
                        MY_PERMISSIONS_REQUEST_READ_MEDIA_AUDIO_AND_NOTIFICATIONS);
            }
        } else {
            // All permissions are granted, load the audiobook
            loadAudiobook();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_PERMISSIONS_REQUEST_READ_MEDIA_AUDIO_AND_NOTIFICATIONS) {
            boolean allPermissionsGranted = true;

            // Check if all permissions were granted
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (allPermissionsGranted) {
                // All required permissions were granted
                loadAudiobook();
            } else {
                // Some permission was denied
                Toast.makeText(this, "Permissions DENIED. Cannot load audiobook.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    protected void loadAudiobook() {
        // Instead of loading one file, we populate the RecyclerView with all audio files
        File musicDir = new File(Environment.getExternalStorageDirectory().getPath() + "/Music");
        if (musicDir.exists() && musicDir.isDirectory()) {
            File[] files = musicDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".mp3")) { // Ignore if not an audio file
                        trackData.add(new TrackData(file.getName(), file.getAbsolutePath()));
                    }
                }
                // Notify adapter about data change
                adapter.notifyDataSetChanged();
            }
        } else {
            Toast.makeText(this, "No Music Directory Found", Toast.LENGTH_SHORT).show();
        }
    }
}
