package com.example.cwk_mwe;


/**
 * Data class representing a bookmark.
 */
public class BookmarkData {
    private String audiobookPath;
    private long timestamp;
    private String title;

    public BookmarkData(String audiobookPath, long timestamp, String title) {
        this.audiobookPath = audiobookPath;
        this.timestamp = timestamp;
        this.title = title;
    }

    public String getAudiobookPath() {
        return audiobookPath;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getTitle() {
        return title;
    }
}
