package com.example.jukeboxplus.music;

import com.example.jukeboxplus.JukeboxPlus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MusicPlayer {

    private final MusicTracker tracker;
    private final Minecraft client;

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
        this.client = Minecraft.getInstance();
    }

    public void tick() {
        if (isPlaying && !isPaused && currentMusic != null && currentMusic.isFinished()) onMusicEnd();
    }

    public void play(MusicInfo music) {
        if (music == null) return;
        stop();
        stopMinecraftMusic();
        try {
            ResourceLocation soundId = parseSoundId(music.getId());
            currentSound = new SimpleSoundInstance(soundId, SoundSource.RECORDS,
                    getVolume(), 1.0f, RandomSource.create(), false, 0,
                    SoundInstance.Attenuation.NONE, 0.0, 0.0, 0.0, true);
            client.getSoundManager().play(currentSound);
            currentMusic = music;
            currentMusic.setStartTime(System.currentTimeMillis());
            isPlaying = true;
            isPaused = false;
            tracker.onMusicStarted(music);
            JukeboxPlus.LOGGER.info("Playing: {} - {}", music.getTitle(), music.getArtist());
        } catch (Exception e) {
            JukeboxPlus.LOGGER.error("Failed to play: {}", e.getMessage());
        }
    }

    private ResourceLocation parseSoundId(String soundPath) {
        if (soundPath.contains(":")) {
            String[] parts = soundPath.split(":", 2);
            return ResourceLocation.fromNamespaceAndPath(parts[0], parts[1]);
        }
        return ResourceLocation.fromNamespaceAndPath("minecraft", soundPath);
    }

    private void stopMinecraftMusic() {
        try {
            client.getSoundManager().stop(null, SoundSource.MUSIC);
            client.getSoundManager().stop(null, SoundSource.RECORDS);
            client.getMusicManager().stopPlaying();
        } catch (Exception ignored) {}
    }

    public void stop() {
        if (currentSound != null) { client.getSoundManager().stop(currentSound); currentSound = null; }
        if (currentMusic != null) { tracker.onMusicStopped(currentMusic); currentMusic = null; }
        isPlaying = false;
        isPaused = false;
    }

    public void togglePlayPause() {
        if (!isPlaying && currentMusic == null) return;
        if (isPaused) resume(); else pause();
    }

    public void pause() {
        if (isPlaying && currentSound != null && !isPaused) {
            pausedAtTime = System.currentTimeMillis();
            client.getSoundManager().stop(currentSound);
            isPaused = true;
        }
    }

    public void resume() {
        if (!isPaused || currentMusic == null) return;
        try {
            ResourceLocation soundId = parseSoundId(currentMusic.getId());
            currentSound = new SimpleSoundInstance(soundId, SoundSource.RECORDS,
                    getVolume(), 1.0f, RandomSource.create(), false, 0,
                    SoundInstance.Attenuation.NONE, 0.0, 0.0, 0.0, true);
            client.getSoundManager().play(currentSound);
            currentMusic.setStartTime(currentMusic.getStartTime() + (System.currentTimeMillis() - pausedAtTime));
            isPaused = false;
        } catch (Exception ignored) {}
    }

    public void next() {
        if (playlist.isEmpty()) { stop(); return; }
        playlistIndex = shuffle ? (int) (Math.random() * playlist.size()) : (playlistIndex + 1) % playlist.size();
        play(playlist.get(playlistIndex));
    }

    public void previous() {
        if (playlist.isEmpty()) return;
        if (currentMusic != null && currentMusic.getElapsedSeconds() > 3) { play(currentMusic); return; }
        playlistIndex = playlistIndex > 0 ? playlistIndex - 1 : playlist.size() - 1;
        play(playlist.get(playlistIndex));
    }

    private void onMusicEnd() {
        if (repeat && currentMusic != null) { MusicInfo m = currentMusic; stop(); play(m); }
        else if (!playlist.isEmpty()) next();
        else stop();
    }

    public void setPlaylist(List<MusicInfo> pl) {
        this.playlist = new ArrayList<>(pl);
        this.playlistIndex = 0;
        if (shuffle) Collections.shuffle(this.playlist);
    }

    public void toggleShuffle() {
        shuffle = !shuffle;
        if (shuffle && !playlist.isEmpty()) {
            MusicInfo cur = playlistIndex < playlist.size() ? playlist.get(playlistIndex) : null;
            Collections.shuffle(playlist);
            if (cur != null) { playlist.remove(cur); playlist.add(0, cur); playlistIndex = 0; }
        }
    }

    public void toggleRepeat() { repeat = !repeat; }

    public void adjustVolume(float delta) {
        setVolume(Math.max(0, Math.min(1, getVolume() + delta)));
    }

    public float getVolume() {
        if (client.options != null) return client.options.getSoundSourceVolume(SoundSource.RECORDS);
        return 1.0f;
    }

    public void setVolume(float volume) {
        if (client.options != null) client.options.getSoundSourceOptionInstance(SoundSource.RECORDS).set((double) Math.max(0, Math.min(1, volume)));
    }

    public boolean isPlaying()              { return isPlaying && !isPaused; }
    public boolean isPaused()               { return isPaused; }
    public boolean isShuffle()              { return shuffle; }
    public boolean isRepeat()               { return repeat; }
    public MusicInfo getCurrentMusic()      { return currentMusic; }
    public List<MusicInfo> getPlaylist()    { return new ArrayList<>(playlist); }
    public int getPlaylistIndex()           { return playlistIndex; }
}
