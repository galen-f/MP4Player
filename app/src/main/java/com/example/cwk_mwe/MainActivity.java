package com.example.cwk_mwe;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
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

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//TODO:
// - Add Color Changer *
// - Add Playback speed settings *
// - Test with "don't keep activities on" *
// - reverse stack navigation ends service lifecycle

//TODO:
// - Create a new AudioManager class to handle audio playback and leave AudioService only in control of lifecycle and notifications *
// - Empty playlist edge case in AudioService (Test)
// - denied permissions in ungraceful
// - Data Binding library? *
// - What happens after a song plays?

/**
 * This is the main activity, the home page. It displays the list of audio files in the Music directory.
 * It also allows for a modal to be created when clicking the bookmark button to allow the user to
 * direct themselves to a specific bookmark in a specific audio file.
 */

public class MainActivity extends AppCompatActivity {
    private Button settingsBtn, bookmarksBtn;
    private RecyclerView recyclerView;
    private MusicRecyclerViewAdapter adapter;
    private MainViewModel mainViewModel;
    private static final int PERMISSION_REQ_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        setupUI();
        observeViewModel();

        checkAndRequestPermissions();
    }

    private void setupUI() {
        // Buttons and Elements
        settingsBtn = findViewById(R.id.settingsBtn);
        bookmarksBtn = findViewById(R.id.bookmarksBtn);
        recyclerView = findViewById(R.id.recyclerView);

        // Recycler view and adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new MusicRecyclerViewAdapter(this, new ArrayList<>(), filePath -> {
            // on click, pass filePath to PlayerActivity and send user to PlayerActivity
            Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
            intent.putExtra("FILE_PATH", filePath);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        //Button click listeners
        settingsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });
        bookmarksBtn.setOnClickListener(v -> mainViewModel.loadBookmarks());
    }

    public void observeViewModel() {
        // Observer of permission status
        mainViewModel.permissionsGranted.observe(this, granted -> {
            if (granted) {
                mainViewModel.loadTracks();
            } else {
                Toast.makeText(this, "Permissions DENIED. Cannot load audiobook.", Toast.LENGTH_SHORT).show();
            }
        });

        // Observe track data
        mainViewModel.trackData.observe(this, trackData -> {
            adapter.trackData = trackData;
            adapter.notifyDataSetChanged();
        });

        // Observe bookmarks
        mainViewModel.bookmarks.observe(this, this::displayBookmarks);
    }

    private void checkAndRequestPermissions() {
        // Simplified permission handling, invoke ViewModel on success
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            mainViewModel.checkAndSetPermissions(true);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_AUDIO}, PERMISSION_REQ_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQ_CODE) {
            boolean allPermissionsGranted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            mainViewModel.checkAndSetPermissions(allPermissionsGranted);
        }
    }

    private void displayBookmarks(List<BookmarkData> bookmarks) {
        if (bookmarks == null || bookmarks.isEmpty()) {
            Toast.makeText(this, "No bookmarks available", Toast.LENGTH_SHORT).show();
            return;
        }

        RecyclerView recyclerView = new RecyclerView(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        BookmarkAdapter adapter = new BookmarkAdapter(bookmarks, bookmark -> {
            Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
            intent.putExtra("FILE_PATH", bookmark.getAudiobookPath());
            intent.putExtra("TIMESTAMP", bookmark.getTimestamp());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        new AlertDialog.Builder(this)
                .setTitle("Bookmarks")
                .setView(recyclerView)
                .setNegativeButton("Close", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }
}
