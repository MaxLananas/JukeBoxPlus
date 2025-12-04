package com.example.jukeboxplus.music;

import com.example.jukeboxplus.JukeboxPlus;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MusicPlayer {
    
    private final MusicTracker tracker;
    private final MinecraftClient client;
    
    private SoundInstance currentSound = null;
    private MusicInfo currentMusic = null;
    private boolean isPlaying = false;
    private boolean isPaused = false;
    private boolean shuffle = false;
    private boolean repeat = false;
    
    private long pausedAtTime = 0;
    
    private List<MusicInfo> playlist = new ArrayList<>();
    private int playlistIndex = 0;
    
    public MusicPlayer(MusicTracker tracker) {
        this.tracker = tracker;
        this.client = MinecraftClient.getInstance();
    }
    
    public void tick() {
        if (isPlaying && !isPaused && currentMusic != null && currentMusic.isFinished()) {
            onMusicEnd();
        }
    }
    
    public void play(MusicInfo music) {
        if (music == null) return;
        
        // Arrêter la musique en cours
        stop();
        
        // Arrêter la musique Minecraft
        stopMinecraftMusic();
        
        try {
            String soundPath = music.getId();
            
            Identifier soundId;
            if (soundPath.startsWith("music_disc.")) {
                soundId = Identifier.of("minecraft", "music_disc." + soundPath.replace("music_disc.", ""));
            } else if (soundPath.contains(":")) {
                String[] parts = soundPath.split(":");
                soundId = Identifier.of(parts[0], parts[1]);
            } else {
                soundId = Identifier.of("minecraft", soundPath);
            }
            
            float volume = getVolume();
            
            currentSound = new PositionedSoundInstance(
                soundId,
                SoundCategory.RECORDS,
                volume,
                1.0f,
                SoundInstance.createRandom(),
                false,
                0,
                SoundInstance.AttenuationType.NONE,
                0.0,
                0.0,
                0.0,
                true
            );
            
            client.getSoundManager().play(currentSound);
            
            currentMusic = music;
            currentMusic.setStartTime(System.currentTimeMillis());
            isPlaying = true;
            isPaused = false;
            
            tracker.onMusicStarted(music);
            
            JukeboxPlus.LOGGER.info("Playing: {} - {}", music.getTitle(), music.getArtist());
            
        } catch (Exception e) {
            JukeboxPlus.LOGGER.error("Failed to play music: {}", e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void stopMinecraftMusic() {
        try {
            // Arrêter toute la musique ambient
            client.getSoundManager().stopSounds(null, SoundCategory.MUSIC);
            
            // Arrêter les disques
            client.getSoundManager().stopSounds(null, SoundCategory.RECORDS);
            
            // Arrêter le tracker de musique de Minecraft
            client.getMusicTracker().stop();
            
        } catch (Exception e) {
            JukeboxPlus.LOGGER.error("Failed to stop Minecraft music: {}", e.getMessage());
        }
    }
    
    public void playFromPlaylist(int index) {
        if (index >= 0 && index < playlist.size()) {
            playlistIndex = index;
            play(playlist.get(index));
        }
    }
    
    public void stop() {
        if (currentSound != null) {
            client.getSoundManager().stop(currentSound);
            currentSound = null;
        }
        
        if (currentMusic != null) {
            tracker.onMusicStopped(currentMusic);
            currentMusic = null;
        }
        
        isPlaying = false;
        isPaused = false;
    }
    
    public void togglePlayPause() {
        if (!isPlaying && currentMusic == null) {
            return;
        }
        
        if (isPaused) {
            resume();
        } else {
            pause();
        }
    }
    
    public void pause() {
        if (isPlaying && currentSound != null && !isPaused) {
            pausedAtTime = System.currentTimeMillis();
            client.getSoundManager().stop(currentSound);
            isPaused = true;
            JukeboxPlus.LOGGER.info("Paused: {}", currentMusic != null ? currentMusic.getTitle() : "Unknown");
        }
    }
    
    public void resume() {
        if (isPaused && currentMusic != null) {
            try {
                String soundPath = currentMusic.getId();
                
                Identifier soundId;
                if (soundPath.startsWith("music_disc.")) {
                    soundId = Identifier.of("minecraft", "music_disc." + soundPath.replace("music_disc.", ""));
                } else if (soundPath.contains(":")) {
                    String[] parts = soundPath.split(":");
                    soundId = Identifier.of(parts[0], parts[1]);
                } else {
                    soundId = Identifier.of("minecraft", soundPath);
                }
                
                float volume = getVolume();
                
                currentSound = new PositionedSoundInstance(
                    soundId,
                    SoundCategory.RECORDS,
                    volume,
                    1.0f,
                    SoundInstance.createRandom(),
                    false,
                    0,
                    SoundInstance.AttenuationType.NONE,
                    0.0, 0.0, 0.0,
                    true
                );
                
                client.getSoundManager().play(currentSound);
                
                long pauseDuration = System.currentTimeMillis() - pausedAtTime;
                currentMusic.setStartTime(currentMusic.getStartTime() + pauseDuration);
                
                isPaused = false;
                JukeboxPlus.LOGGER.info("Resumed: {}", currentMusic.getTitle());
                
            } catch (Exception e) {
                JukeboxPlus.LOGGER.error("Failed to resume: {}", e.getMessage());
            }
        }
    }
    
    public void next() {
        if (playlist.isEmpty()) {
            stop();
            return;
        }
        
        if (shuffle) {
            playlistIndex = (int) (Math.random() * playlist.size());
        } else {
            playlistIndex = (playlistIndex + 1) % playlist.size();
        }
        
        play(playlist.get(playlistIndex));
    }
    
    public void previous() {
        if (playlist.isEmpty()) return;
        
        if (currentMusic != null && currentMusic.getElapsedSeconds() > 3) {
            play(currentMusic);
        } else {
            if (playlistIndex > 0) {
                playlistIndex--;
            } else {
                playlistIndex = playlist.size() - 1;
            }
            play(playlist.get(playlistIndex));
        }
    }
    
    private void onMusicEnd() {
        if (repeat && currentMusic != null) {
            MusicInfo toRepeat = currentMusic;
            stop();
            play(toRepeat);
        } else if (!playlist.isEmpty()) {
            next();
        } else {
            stop();
        }
    }
    
    public void setPlaylist(List<MusicInfo> newPlaylist) {
        this.playlist = new ArrayList<>(newPlaylist);
        this.playlistIndex = 0;
        
        if (shuffle) {
            Collections.shuffle(this.playlist);
        }
    }
    
    public void addToPlaylist(MusicInfo music) {
        playlist.add(music);
    }
    
    public void clearPlaylist() {
        playlist.clear();
        playlistIndex = 0;
    }
    
    public void toggleShuffle() {
        shuffle = !shuffle;
        if (shuffle && !playlist.isEmpty()) {
            MusicInfo current = playlistIndex < playlist.size() ? playlist.get(playlistIndex) : null;
            Collections.shuffle(playlist);
            if (current != null) {
                playlist.remove(current);
                playlist.add(0, current);
                playlistIndex = 0;
            }
        }
    }
    
    public void toggleRepeat() {
        repeat = !repeat;
    }
    
    public void adjustVolume(float delta) {
        if (client.options != null) {
            float current = getVolume();
            float newVolume = Math.max(0, Math.min(1, current + delta));
            setVolume(newVolume);
        }
    }
    
    public float getVolume() {
        if (client.options != null) {
            return client.options.getSoundVolume(SoundCategory.RECORDS);
        }
        return 1.0f;
    }
    
    public void setVolume(float volume) {
        if (client.options != null) {
            volume = Math.max(0, Math.min(1, volume));
            client.options.getSoundVolumeOption(SoundCategory.RECORDS).setValue((double) volume);
        }
    }
    
    // Getters
    public boolean isPlaying() { 
        return isPlaying && !isPaused; 
    }
    
    public boolean isPaused() { 
        return isPaused; 
    }
    
    public boolean isShuffle() { 
        return shuffle; 
    }
    
    public boolean isRepeat() { 
        return repeat; 
    }
    
    public MusicInfo getCurrentMusic() { 
        return currentMusic; 
    }
    
    public List<MusicInfo> getPlaylist() { 
        return new ArrayList<>(playlist); 
    }
    
    public int getPlaylistIndex() { 
        return playlistIndex; 
    }
}