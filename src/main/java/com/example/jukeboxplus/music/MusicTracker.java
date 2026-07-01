package com.example.jukeboxplus.music;

import com.example.jukeboxplus.JukeboxPlus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;

import java.util.ArrayList;
import java.util.List;

public class MusicTracker {

    private MusicInfo currentMusic = null;
    private final List<MusicInfo> history = new ArrayList<>();
    private static final int MAX_HISTORY = 20;
    private long lastMusicTime = 0;
    private boolean isMusicPlaying = false;

    public void tick() {
        if (currentMusic != null && currentMusic.isFinished()) stopMusic();
        if (isMusicPlaying && System.currentTimeMillis() - lastMusicTime > 5000) {
            if (currentMusic != null && currentMusic.isFinished()) stopMusic();
        }
    }

    public void onSoundPlayed(SoundInstance sound) {
        if (sound == null) return;
        String soundId = sound.getLocation().toString();
        SoundSource category = sound.getSource();

        if (soundId.contains("music_disc") || category == SoundSource.RECORDS) { handleMusicStart(soundId); return; }
        if (category == SoundSource.MUSIC || soundId.contains("music.")) { handleMusicStart(soundId); return; }
        if (soundId.contains("calm") || soundId.contains("hal") || soundId.contains("creative") ||
                soundId.contains("nether") || soundId.contains("end") || soundId.contains("credits") ||
                soundId.contains("menu")) { handleMusicStart(soundId); }
    }

    public void onSoundStopped(SoundInstance sound) {
        if (sound == null || currentMusic == null) return;
        if (sound.getLocation().toString().toLowerCase().contains(currentMusic.getId().toLowerCase())) stopMusic();
    }

    private void handleMusicStart(String soundId) {
        MusicInfo info = MusicDatabase.getByIdentifier(soundId);
        if (info == null) info = MusicDatabase.createUnknown(soundId);
        if (currentMusic != null && currentMusic.getId().equals(info.getId()) &&
                System.currentTimeMillis() - currentMusic.getStartTime() < 2000) return;
        if (currentMusic != null) addToHistory(currentMusic);
        currentMusic = info;
        isMusicPlaying = true;
        lastMusicTime = System.currentTimeMillis();
        JukeboxPlus.LOGGER.info("Now playing: {} - {}", info.getTitle(), info.getArtist());
    }

    private void stopMusic() {
        if (currentMusic != null) addToHistory(currentMusic);
        currentMusic = null;
        isMusicPlaying = false;
    }

    private void addToHistory(MusicInfo music) {
        if (!history.isEmpty() && history.get(0).getId().equals(music.getId())) return;
        history.add(0, music);
        while (history.size() > MAX_HISTORY) history.remove(history.size() - 1);
    }

    public void onMusicStarted(MusicInfo music) {
        if (currentMusic != null) addToHistory(currentMusic);
        this.currentMusic = music;
        this.isMusicPlaying = true;
        this.lastMusicTime = System.currentTimeMillis();
    }

    public void onMusicStopped(MusicInfo music) {
        if (music != null) addToHistory(music);
        if (currentMusic != null && music != null && currentMusic.getId().equals(music.getId())) {
            currentMusic = null;
            isMusicPlaying = false;
        }
    }

    public MusicInfo getCurrentMusic()      { return currentMusic; }
    public boolean isMusicPlaying()          { return isMusicPlaying && currentMusic != null; }
    public List<MusicInfo> getHistory()      { return new ArrayList<>(history); }
    public void clearHistory()               { history.clear(); }

    public float getMusicVolume() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options != null) return mc.options.getSoundSourceVolume(SoundSource.MUSIC);
        return 1.0f;
    }

    public void setMusicVolume(float volume) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options != null) {
            mc.options.getSoundSourceOptionInstance(SoundSource.MUSIC).set((double) Math.max(0, Math.min(1, volume)));
        }
    }
}
