package com.example.jukeboxplus.music;

import net.minecraft.util.Identifier;

public class MusicInfo {
    
    private final String id;
    private final String title;
    private final String artist;
    private final MusicType type;
    private final int durationSeconds;
    private final int discColor;
    private long startTime;
    
    public enum MusicType {
        DISC("Disc"),
        AMBIENT("Ambient"),
        CREATIVE("Creative"),
        CREDITS("Credits"),
        MENU("Menu"),
        NETHER("Nether"),
        END("End"),
        UNKNOWN("Unknown");
        
        public final String displayName;
        
        MusicType(String displayName) {
            this.displayName = displayName;
        }
    }
    
    public MusicInfo(String id, String title, String artist, MusicType type, int durationSeconds, int discColor) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.type = type;
        this.durationSeconds = durationSeconds;
        this.discColor = discColor;
        this.startTime = System.currentTimeMillis();
    }
    
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public MusicType getType() { return type; }
    public int getDurationSeconds() { return durationSeconds; }
    public int getDiscColor() { return discColor; }
    public long getStartTime() { return startTime; }
    
    public void setStartTime(long time) {
        this.startTime = time;
    }
    
    public int getElapsedSeconds() {
        return (int) ((System.currentTimeMillis() - startTime) / 1000);
    }
    
    public float getProgress() {
        if (durationSeconds <= 0) return 0;
        return Math.min(1.0f, (float) getElapsedSeconds() / durationSeconds);
    }
    
    public String getFormattedTime() {
        int elapsed = getElapsedSeconds();
        int minutes = elapsed / 60;
        int seconds = elapsed % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
    
    public String getFormattedDuration() {
        int minutes = durationSeconds / 60;
        int seconds = durationSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
    
    public boolean isFinished() {
        return durationSeconds > 0 && getElapsedSeconds() >= durationSeconds;
    }
}