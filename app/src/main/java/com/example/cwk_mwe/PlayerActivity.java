package com.example.cwk_mwe;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.example.cwk_mwe.databinding.ActivityPlayerBinding;

/**
 * Represents the View for the track player screen. Displays playback progress,
 * allows control actions (play, pause, stop, skip).
 *
 * Side note but if someone is reading this, why are we calling these audiobooks and then linking to
 * a song downloading site in the specs. It really seems like a music playing app.
 */

public class PlayerActivity extends AppCompatActivity {
    private GlobalSharedViewModel globalSharedViewModel;
    private PlayerViewModel playerViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        globalSharedViewModel = new ViewModelProvider(this).get(GlobalSharedViewModel.class);
        globalSharedViewModel.applyBackgroundColor(this, findViewById(android.R.id.content));

        ActivityPlayerBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_player);

        // Initialize ViewModel
        playerViewModel = new ViewModelProvider(this).get(PlayerViewModel.class);
        binding.setViewModel(playerViewModel);
        binding.setLifecycleOwner(this);

        // If playback is stopped, return the user home. This helps to avoid any funky issues with a player with no loaded file.
        playerViewModel.getState().observe(this, state -> {
            if ("stopped".equals(state)) {
                Toast.makeText(this, "Playback stopped: returned to home", Toast.LENGTH_SHORT).show();
                navigateToHome();
            }
        });
        // Play Audio as soon as the activity is created, saves the user having to click the file and THEN the play button
        // Two clicks in this economy is crazyyyyy
        playAudio();
    }

    private void playAudio() {
        String filePath = getIntent().getStringExtra("FILE_PATH");
        if (filePath != null) {
            playerViewModel.play(filePath);
            // If a timestamp is provided, seek to that position (Only happens with bookmarks)
            long timestampLong = getIntent().getLongExtra("TIMESTAMP", 0);
            int timestamp = (int) timestampLong;
            if (timestamp > 0) {
                playerViewModel.seek(timestamp);
            }
        } else {
            Log.e("PlayerActivity", "No file path provided");
        }

        // Request updated playback speed from AudioService
        float playbackSpeed = playerViewModel.getPlaybackSpeed(); // Add getter in ViewModel
        Intent intent = new Intent(this, AudioService.class);
        intent.setAction(AudioService.ACTION_SET_SPEED);
        intent.putExtra("speed", playbackSpeed);
        startService(intent);
    }

    private void navigateToHome() {
        // Navigate to home activity, used to keep the user off playeractivity when there is no file path selected.
        // This caused a lot of issues and its easier to just not allow them on, than deal with those issues.
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
