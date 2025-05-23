package com.example.cwk_mwe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cwk_mwe.databinding.ActivityMainBinding;

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
    private PermissionManager permissionManager;
    private MainViewModel mainViewModel;
    private GlobalSharedViewModel globalSharedViewModel;
    private AlertDialog bookmarkDialog;
    public static final int PERMISSION_REQ_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize permission manager
        permissionManager = new PermissionManager(this);

        globalSharedViewModel = new ViewModelProvider(this).get(GlobalSharedViewModel.class);
        globalSharedViewModel.applyBackgroundColor(this, findViewById(android.R.id.content));

        // DataBinding makes things harder
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        // Initialize Viewmodel
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        binding.setViewModel(mainViewModel);
        binding.setLifecycleOwner(this);

        // Initialize views, don't worry code checker, this is only for empty view checking, no need to call me out on not using databinding
        recyclerView = findViewById(R.id.recyclerView);
        emptyView = findViewById(R.id.empty_message);

        permissionManager.checkAndRequestPermissions();

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

        // Observe bookmarks
        mainViewModel.bookmarks.observe(this, this::displayBookmarks);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Delegate permission handling to PermissionManager
        boolean allPermissionsGranted = permissionManager.handlePermissionResult(requestCode, grantResults);
        mainViewModel.checkAndSetPermissions(allPermissionsGranted);

        if (!allPermissionsGranted) {
            Toast.makeText(this, "Permissions denied. Functionality limited", Toast.LENGTH_SHORT).show();
        }
    }

    public MainViewModel getMainViewModel() {
        return mainViewModel;
    }

    private void displayBookmarks(List<BookmarkData> bookmarks) {
        // Handle no bookmark case
        if (bookmarks == null || bookmarks.isEmpty()) {
            Toast.makeText(this, "No bookmarks available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a RecyclerView to display bookmarks
        RecyclerView recyclerView = new RecyclerView(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        BookmarkAdapter adapter = new BookmarkAdapter(bookmarks, bookmark -> {
            Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
            intent.putExtra("FILE_PATH", bookmark.getAudiobookPath());
            intent.putExtra("TIMESTAMP", bookmark.getTimestamp());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        // Render bookmarks
        bookmarkDialog = new AlertDialog.Builder(this)
                .setTitle("Bookmarks")
                .setView(recyclerView)
                .setNegativeButton("Close", (dialog, which) -> dialog.dismiss())
                .create();

        // Show the modal
        bookmarkDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Dismiss the bookmark dialog if it is still showing (this broke things so much omg)
        if (bookmarkDialog != null && bookmarkDialog.isShowing()) {
            bookmarkDialog.dismiss();
            bookmarkDialog = null;
        }
    }
}
