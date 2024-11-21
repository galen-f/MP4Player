package com.example.cwk_mwe;

import android.os.Environment;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for file-related operations, including retrieving a list of MP3 files from
 * the device's Music directory.
 */

public class FileUtils {

    // Method to get a list of MP3 files from the Music directory
    public static List<String> getAudioFiles() {
        List<String> audioFiles = new ArrayList<>();

        // Access the Music directory
        File musicDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getPath());

        if (musicDirectory.exists() && musicDirectory.isDirectory()) {
            File[] files = musicDirectory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && isMP3(file.getName())) {
                        audioFiles.add(file.getAbsolutePath());
                    }
                }
            }
        }
        return audioFiles;
    }

    // Helper method to check if the file is an MP3
    private static boolean isMP3(String fileName) {
        String lowerCaseName = fileName.toLowerCase();
        return lowerCaseName.endsWith(".mp3");
    }
}
