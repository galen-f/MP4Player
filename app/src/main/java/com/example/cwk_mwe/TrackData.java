package com.example.cwk_mwe;

/**
 * Represents an audio track, storing its file name and path. Used as a data model for displaying
 * tracks and managing playback.
 */

public class TrackData {
    public String fileName;
    public String filePath;

    public TrackData (String fileName, String filePath) {
        this.fileName = fileName;
        this.filePath = filePath;
    }

}
