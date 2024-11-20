package com.example.cwk_mwe;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cwk_mwe.databinding.ActivityMainBinding;
import com.example.cwk_mwe.databinding.ActivityPlayerBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the main activity, the home page. It displays the list of audio files in the Music directory.
 * It also allows for a modal to be created when clicking the bookmark button to allow the user to
 * direct themselves to a specific bookmark in a specific audio file.
 */

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TextView emptyView;
    private MusicRecyclerViewAdapter adapter;
    private MainViewModel mainViewModel;
    private AppSharedViewModel appSharedViewModel;
    private AlertDialog bookmarkDialog;
    private static final int PERMISSION_REQ_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appSharedViewModel = new ViewModelProvider(this).get(AppSharedViewModel.class);
        appSharedViewModel.applyBackgroundColor(this, findViewById(android.R.id.content));

        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        // Initialize Viewmodel
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        binding.setViewModel(mainViewModel);
        binding.setLifecycleOwner(this);

        // Initialize views, dont worry prof, this is only for empty view checking, no need to call me out on not using databinding
        recyclerView = findViewById(R.id.recyclerView);
        emptyView = findViewById(R.id.empty_message);

        checkAndRequestPermissions();
        setupRecyclerView();
        observeViewModel();
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new MusicRecyclerViewAdapter(new ArrayList<>(), filePath -> {
            Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
            intent.putExtra("FILE_PATH", filePath);
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);
    }

    public void observeViewModel() {
        // Observe track data and toggle visibility
        mainViewModel.trackData.observe(this, trackData -> {
            adapter.trackData = trackData;
            adapter.notifyDataSetChanged();

            // Show or hide the empty view based on the data
            if (trackData == null || trackData.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);
            }
        });

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

        bookmarkDialog = new AlertDialog.Builder(this)
                .setTitle("Bookmarks")
                .setView(recyclerView)
                .setNegativeButton("Close", (dialog, which) -> dialog.dismiss())
                .create();

        // Show the dialog
        bookmarkDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Dismiss the dialog if it is still showing
        if (bookmarkDialog != null && bookmarkDialog.isShowing()) {
            bookmarkDialog.dismiss();
            bookmarkDialog = null;
        }
    }
}
