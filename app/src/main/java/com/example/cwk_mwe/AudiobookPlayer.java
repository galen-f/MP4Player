package com.example.cwk_mwe;

public class AudiobookPlayer {

    public enum AudiobookPlayerState {
        ERROR,
        PLAYING,
        PAUSED,
        STOPPED
    }

    private AudiobookPlayerState state;

    public AudiobookPlayer() {
        this.state = AudiobookPlayerState.STOPPED;
    }

    public AudiobookPlayerState getState() {
        return this.state;
    }

    public void setState(AudiobookPlayerState newState) {
        this.state = newState;
    }

    public void play() {
        this.state = AudiobookPlayerState.PLAYING;
    }

    public void pause() {
        this.state = AudiobookPlayerState.PAUSED;
    }

    public void stop() {
        this.state = AudiobookPlayerState.STOPPED;
    }
}
