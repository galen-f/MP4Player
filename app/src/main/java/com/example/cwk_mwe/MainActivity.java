package com.example.cwk_mwe;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

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

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MusicRecyclerViewAdapter adapter;
    private List<TrackData> trackData = new ArrayList<>();
    private AudiobookPlayer audiobookPlayer;
    private static final int MY_PERMISSIONS_REQUEST_READ_MEDIA_AUDIO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Set the selected item (navbar)
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        // Set a listener to handle item selection
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(MainActivity.this, MainActivity.class));
                overridePendingTransition(0, 0); // No animation for smoother switch
                return true;
            } else if (itemId == R.id.nav_player) {
                    startActivity(new Intent(MainActivity.this, PlayerActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
            } else if (itemId == R.id.nav_settings){
                    startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
            }
            return false;
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Set adapter with empty list initially
        adapter = new MusicRecyclerViewAdapter(this, trackData, filePath -> {
            // Handle click, pass filePath to PlayerActivity
            Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
            intent.putExtra("FILE_PATH", filePath);
            Log.d("MainActivity", "Passing file path to PlayerActivity: " + filePath);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        audiobookPlayer = new AudiobookPlayer();
        checkAndRequestPermissions();
    }

    private void checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_AUDIO)) {
                new AlertDialog.Builder(this)
                        .setTitle("Permission needed")
                        .setMessage("This permission is needed to access the music files on your device.")
                        .setPositiveButton("OK", (dialog, which) -> ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.READ_MEDIA_AUDIO},
                                MY_PERMISSIONS_REQUEST_READ_MEDIA_AUDIO))
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_AUDIO},
                        MY_PERMISSIONS_REQUEST_READ_MEDIA_AUDIO);
            }
        } else {
            // Permission already granted, load the audiobook
            loadAudiobook();

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_MEDIA_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted, load the audiobook
                loadAudiobook();
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show(); // Permission denied, show a toast
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
