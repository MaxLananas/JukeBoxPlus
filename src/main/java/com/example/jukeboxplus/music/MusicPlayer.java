package com.example.jukeboxplus.music;

import com.example.jukeboxplus.JukeboxPlus;
import com.example.jukeboxplus.compat.Compat;
import com.example.jukeboxplus.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Plays vanilla tracks on demand. Playback goes through the normal sound engine, so
 * the game's own volume sliders still apply ("Jukebox/Note Blocks" category).
 */
public final class MusicPlayer {

    private static final Random RANDOM = new Random();

    private final Minecraft minecraft;
    private final MusicTracker tracker;

    private TrackSoundInstance current;
    private Track currentTrack;
    private boolean paused;
    private long startedAtMs;
    private long pausedAtMs;

    /** Play queue: the list displayed when the track was started, used for next/previous. */
    private List<Track> queue = new ArrayList<>();
    private int queueIndex = -1;

    public MusicPlayer(Minecraft minecraft, MusicTracker tracker) {
        this.minecraft = minecraft;
        this.tracker = tracker;
    }

    // ----------------------------------------------------------------- playback

    public void play(Track track) {
        play(track, queue, queue.indexOf(track));
    }

    public void play(Track track, List<Track> newQueue, int index) {
        if (track == null) return;
        stopInternal(false);
        if (newQueue != null) {
            queue = new ArrayList<>(newQueue);
            queueIndex = index >= 0 ? index : queue.indexOf(track);
        }
        if (ModConfig.get().stopVanillaMusic) Compat.stopVanillaMusic(minecraft);
        TrackSoundInstance inst = new TrackSoundInstance(track, Compat.soundEvent(track.getSoundEventId()), ModConfig.get().volume);
        try {
            minecraft.getSoundManager().play(inst);
        } catch (Exception e) {
            JukeboxPlus.LOGGER.error("Could not start {}", track, e);
            return;
        }
        current = inst;
        currentTrack = track;
        paused = false;
        startedAtMs = System.currentTimeMillis();
        tracker.onPlayerStarted(track);
        JukeboxPlus.LOGGER.info("Playing {} - {}", track.getTitle(), track.getArtist());
    }

    public void stop() {
        stopInternal(true);
    }

    private void stopInternal(boolean notify) {
        if (current != null) {
            try {
                if (paused) Compat.resumeSound(minecraft.getSoundManager(), current);
                minecraft.getSoundManager().stop(current);
                current.end();
            } catch (Exception ignored) {}
        }
        boolean had = currentTrack != null;
        current = null;
        currentTrack = null;
        paused = false;
        if (notify && had) tracker.onPlayerStopped();
    }

    public void togglePause() {
        if (current == null) return;
        SoundManager sm = minecraft.getSoundManager();
        if (paused) {
            Compat.resumeSound(sm, current);
            startedAtMs += System.currentTimeMillis() - pausedAtMs;
            paused = false;
        } else {
            if (!Compat.pauseSound(sm, current)) return; // could not pause: keep playing
            pausedAtMs = System.currentTimeMillis();
            paused = true;
        }
    }

    public void next() {
        Track t = pickNext(true);
        if (t != null) play(t, null, queue.indexOf(t));
    }

    public void previous() {
        if (queue.isEmpty()) return;
        // Restart the track if we're more than a few seconds in, like every music player.
        if (currentTrack != null && elapsedMs() > 3000) {
            play(currentTrack, null, queueIndex);
            return;
        }
        int idx = queueIndex <= 0 ? queue.size() - 1 : queueIndex - 1;
        play(queue.get(idx), null, idx);
    }

    private Track pickNext(boolean manual) {
        if (queue.isEmpty()) return null;
        ModConfig cfg = ModConfig.get();
        if (!manual && cfg.repeatMode == ModConfig.RepeatMode.ONE && currentTrack != null) return currentTrack;
        if (cfg.shuffle && queue.size() > 1) {
            int idx;
            do idx = RANDOM.nextInt(queue.size()); while (idx == queueIndex);
            return queue.get(idx);
        }
        int idx = queueIndex + 1;
        if (idx >= queue.size()) {
            if (!manual && cfg.repeatMode == ModConfig.RepeatMode.OFF) return null;
            idx = 0;
        }
        return queue.get(idx);
    }

    // --------------------------------------------------------------------- tick

    /** Called every client tick. Detects the natural end of a track and chains the next one. */
    public void tick() {
        if (current == null || paused) return;
        if (!minecraft.getSoundManager().isActive(current)) {
            // Give the engine one second of grace: isActive is false until the stream really starts.
            if (elapsedMs() < 1000) return;
            Track finished = currentTrack;
            current = null;
            currentTrack = null;
            tracker.onPlayerStopped();
            Track nextTrack = pickNext(false);
            if (nextTrack != null) play(nextTrack, null, queue.indexOf(nextTrack));
            else JukeboxPlus.LOGGER.debug("Finished {}", finished);
        }
    }

    /** Called when the volume setting changed. */
    public void applyVolume() {
        if (current != null) current.setVolume(ModConfig.get().volume);
    }

    public void adjustVolume(float delta) {
        ModConfig cfg = ModConfig.get();
        cfg.volume = Math.max(0f, Math.min(1f, Math.round((cfg.volume + delta) * 20f) / 20f));
        cfg.save();
        applyVolume();
    }

    /** Called when leaving a world: the sound engine drops every sound, so forget ours. */
    public void onDisconnect() {
        stopInternal(true);
    }

    // ------------------------------------------------------------------ getters

    public boolean isPlaying()      { return current != null && !paused; }
    public boolean isPaused()       { return current != null && paused; }
    public boolean hasTrack()       { return current != null; }
    public Track getCurrentTrack()  { return currentTrack; }
    public List<Track> getQueue()   { return queue; }

    public long elapsedMs() {
        if (current == null) return 0;
        return (paused ? pausedAtMs : System.currentTimeMillis()) - startedAtMs;
    }
}
