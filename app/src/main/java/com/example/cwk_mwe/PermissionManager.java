package com.example.cwk_mwe;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionManager {
    private final MainActivity mainActivity;

    public PermissionManager(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    void checkAndRequestPermissions() {
        List<String> permissionsToRequest = new ArrayList<String>();

        // Check READ_MEDIA_AUDIO permission
        if (ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.READ_MEDIA_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.READ_MEDIA_AUDIO);
        }

        // Check POST_NOTIFICATIONS permission (Android 13+) Yes I know it'll be tested on 14, but just seems like good practice lol
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS);
        }

        // Request permissions if necessary
        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(
                    mainActivity,
                    permissionsToRequest.toArray(new String[0]),
                    MainActivity.PERMISSION_REQ_CODE
            );
        } else {
            // All permissions are granted
            mainActivity.getMainViewModel().checkAndSetPermissions(true);
        }
    }

    public boolean handlePermissionResult(int requestCode, int[] grantResults) {
        if (requestCode == MainActivity.PERMISSION_REQ_CODE) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    return false; // Permission denied
                }
            }
            return true; // All permissions granted
        }
        return false; // Not the relevant request code
    }
}