package com.example.jukeboxplus.music;

import com.example.jukeboxplus.JukeboxPlus;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;

import java.util.ArrayList;
import java.util.List;

public class MusicTracker {
    
    private MusicInfo currentMusic = null;
    private final List<MusicInfo> history = new ArrayList<>();
    private static final int MAX_HISTORY = 20;
    
    private long lastMusicTime = 0;
    private boolean isMusicPlaying = false;
    
    public void tick() {
        // Vérifier si la musique est finie
        if (currentMusic != null && currentMusic.isFinished()) {
            stopMusic();
        }
        
        // Auto-clear après 5 secondes sans mise à jour
        if (isMusicPlaying && System.currentTimeMillis() - lastMusicTime > 5000) {
            if (currentMusic != null && currentMusic.isFinished()) {
                stopMusic();
            }
        }
    }
    
    public void onSoundPlayed(SoundInstance sound) {
        if (sound == null) return;
        
        String soundId = sound.getId().toString();
        SoundCategory category = sound.getCategory();
        
        // Détecter les disques de musique
        if (soundId.contains("music_disc") || category == SoundCategory.RECORDS) {
            handleMusicStart(soundId);
            return;
        }
        
        // Détecter la musique d'ambiance
        if (category == SoundCategory.MUSIC || soundId.contains("music.")) {
            handleMusicStart(soundId);
            return;
        }
        
        // Détecter les musiques spéciales
        if (soundId.contains("calm") || soundId.contains("hal") || 
            soundId.contains("creative") || soundId.contains("nether") ||
            soundId.contains("end") || soundId.contains("credits") ||
            soundId.contains("menu")) {
            handleMusicStart(soundId);
        }
    }
    
    public void onSoundStopped(SoundInstance sound) {
        if (sound == null || currentMusic == null) return;
        
        String soundId = sound.getId().toString();
        
        // Vérifier si c'est la musique en cours
        if (soundId.toLowerCase().contains(currentMusic.getId().toLowerCase())) {
            stopMusic();
        }
    }
    
    private void handleMusicStart(String soundId) {
        MusicInfo info = MusicDatabase.getByIdentifier(soundId);
        
        if (info == null) {
            info = MusicDatabase.createUnknown(soundId);
        }
        
        // Éviter les doublons rapides
        if (currentMusic != null && 
            currentMusic.getId().equals(info.getId()) && 
            System.currentTimeMillis() - currentMusic.getStartTime() < 2000) {
            return;
        }
        
        // Sauvegarder l'ancienne musique dans l'historique
        if (currentMusic != null) {
            addToHistory(currentMusic);
        }
        
        currentMusic = info;
        isMusicPlaying = true;
        lastMusicTime = System.currentTimeMillis();
        
        JukeboxPlus.LOGGER.info("Now playing: {} - {}", info.getTitle(), info.getArtist());
    }
    
    private void stopMusic() {
        if (currentMusic != null) {
            addToHistory(currentMusic);
            currentMusic = null;
        }
        isMusicPlaying = false;
    }
    
    private void addToHistory(MusicInfo music) {
        // Éviter les doublons consécutifs
        if (!history.isEmpty() && history.get(0).getId().equals(music.getId())) {
            return;
        }
        
        history.add(0, music);
        
        // Limiter la taille de l'historique
        while (history.size() > MAX_HISTORY) {
            history.remove(history.size() - 1);
        }
    }
    
    // ═══════════════════════════════════════════
    // MÉTHODES POUR LE PLAYER
    // ═══════════════════════════════════════════
    
    public void onMusicStarted(MusicInfo music) {
        if (currentMusic != null) {
            addToHistory(currentMusic);
        }
        this.currentMusic = music;
        this.isMusicPlaying = true;
        this.lastMusicTime = System.currentTimeMillis();
    }
    
    public void onMusicStopped(MusicInfo music) {
        if (music != null) {
            addToHistory(music);
        }
        if (currentMusic != null && music != null && 
            currentMusic.getId().equals(music.getId())) {
            currentMusic = null;
            isMusicPlaying = false;
        }
    }
    
    // ═══════════════════════════════════════════
    // GETTERS
    // ═══════════════════════════════════════════
    
    public MusicInfo getCurrentMusic() {
        return currentMusic;
    }
    
    public boolean isMusicPlaying() {
        return isMusicPlaying && currentMusic != null;
    }
    
    public List<MusicInfo> getHistory() {
        return new ArrayList<>(history);
    }
    
    public void clearHistory() {
        history.clear();
    }
    
    public float getMusicVolume() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options != null) {
            return client.options.getSoundVolume(SoundCategory.MUSIC);
        }
        return 1.0f;
    }
    
    public void setMusicVolume(float volume) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options != null) {
            volume = Math.max(0, Math.min(1, volume));
            client.options.getSoundVolumeOption(SoundCategory.MUSIC).setValue((double) volume);
        }
    }
}