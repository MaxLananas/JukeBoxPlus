package com.example.jukeboxplus.music;

import com.example.jukeboxplus.compat.Compat;
import com.example.jukeboxplus.JukeboxPlus;
import com.example.jukeboxplus.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;

import java.util.ArrayList;
import java.util.List;

/**
 * Knows what music is currently audible, whether it was started by the player screen
 * or by the game itself (biome music, jukeboxes, menu music), and keeps a history.
 */
public final class MusicTracker {

    public enum Source { PLAYER, GAME }

    private static final int MAX_HISTORY = 50;

    private final Minecraft minecraft;

    private Track current;
    private Source source;
    private long startedAtMs;
    /** The vanilla sound instance we are following, when {@link #source} is {@link Source#GAME}. */
    private SoundInstance gameInstance;

    public MusicTracker(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    // ------------------------------------------------------------ player events

    void onPlayerStarted(Track track) {
        setCurrent(track, Source.PLAYER, null);
    }

    void onPlayerStopped() {
        if (source == Source.PLAYER) clear();
    }

    // -------------------------------------------------------------- game events

    /**
     * Called from {@code SoundEngineMixin} after the sound engine accepted a sound.
     * Runs on the render thread.
     */
    public void onSoundStarted(SoundInstance instance) {
        if (instance == null || instance instanceof TrackSoundInstance) return;
        SoundSource cat;
        try {
            cat = instance.getSource();
        } catch (Throwable t) {
            return;
        }
        if (cat != SoundSource.MUSIC && cat != SoundSource.RECORDS) return;
        if (instance.getSound() == null) return;
        String file = Compat.soundLocation(instance);
        Track track = TrackDatabase.resolveOrUnknown(file);
        // Our own playback wins over background music that might sneak in.
        if (source == Source.PLAYER && current != null) return;
        setCurrent(track, Source.GAME, instance);
    }

    private void setCurrent(Track track, Source src, SoundInstance instance) {
        current = track;
        source = src;
        gameInstance = instance;
        startedAtMs = System.currentTimeMillis();
        addToHistory(track);
        JukeboxPlus.LOGGER.debug("Now playing ({}): {}", src, track);
    }

    private void clear() {
        current = null;
        source = null;
        gameInstance = null;
    }

    public void tick() {
        if (source == Source.GAME && gameInstance != null) {
            long elapsed = System.currentTimeMillis() - startedAtMs;
            if (elapsed > 1500 && !minecraft.getSoundManager().isActive(gameInstance)) clear();
        }
    }

    public void onDisconnect() {
        if (source == Source.GAME) clear();
    }

    // ------------------------------------------------------------------ history

    private void addToHistory(Track track) {
        if (track == null) return;
        ModConfig cfg = ModConfig.get();
        List<String> h = cfg.history;
        if (!h.isEmpty() && h.get(0).equals(track.getPath())) return;
        h.remove(track.getPath());
        h.add(0, track.getPath());
        while (h.size() > MAX_HISTORY) h.remove(h.size() - 1);
        cfg.save();
    }

    /** Most recent first. Unknown paths (from another Minecraft version) are skipped. */
    public List<Track> getHistory() {
        List<Track> out = new ArrayList<>();
        for (String path : ModConfig.get().history) {
            Track t = TrackDatabase.byPath(path);
            if (t != null) out.add(t);
        }
        return out;
    }

    public void clearHistory() {
        ModConfig cfg = ModConfig.get();
        cfg.history.clear();
        cfg.save();
    }

    // ------------------------------------------------------------------ getters

    public Track getCurrent()   { return current; }
    public Source getSource()   { return source; }
    public boolean isPlaying()  { return current != null; }
    public long elapsedMs()     { return current == null ? 0 : System.currentTimeMillis() - startedAtMs; }
}
