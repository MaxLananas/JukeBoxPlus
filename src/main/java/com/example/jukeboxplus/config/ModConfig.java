package com.example.jukeboxplus.config;

import com.example.jukeboxplus.JukeboxPlus;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Persistent settings, stored in {@code config/jukeboxplus.json}. */
public final class ModConfig {

    public enum Corner { TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT }
    public enum RepeatMode { OFF, ALL, ONE }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int VERSION = 2;

    public int configVersion = VERSION;

    /** Show the "now playing" card on the HUD. */
    public boolean overlayEnabled = true;
    /** Keep the card visible while music plays instead of fading it out after a few seconds. */
    public boolean overlayAlwaysVisible = false;
    /** How long (seconds) the card stays visible after a track starts when not always visible. */
    public int overlayDurationSeconds = 8;
    public Corner overlayCorner = Corner.TOP_RIGHT;
    /** Also show the card for music started by the game itself (biome music, jukeboxes...). */
    public boolean overlayForVanillaMusic = true;

    /** Playback gain for tracks started from the player, 0..1. Multiplied by the "Jukebox/Note Blocks" slider. */
    public float volume = 0.8f;
    public RepeatMode repeatMode = RepeatMode.OFF;
    public boolean shuffle = false;
    /** Stop the background music of the game when a track is started from the player. */
    public boolean stopVanillaMusic = true;

    public List<String> history = new ArrayList<>();

    private static ModConfig instance;

    public static ModConfig get() {
        if (instance == null) instance = load();
        return instance;
    }

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("jukeboxplus.json");
    }

    private static ModConfig load() {
        Path path = file();
        if (Files.exists(path)) {
            try (Reader r = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                ModConfig cfg = GSON.fromJson(r, ModConfig.class);
                if (cfg != null) {
                    cfg.sanitize();
                    return cfg;
                }
            } catch (Exception e) {
                JukeboxPlus.LOGGER.warn("Could not read {}, using defaults", path, e);
            }
        }
        ModConfig cfg = new ModConfig();
        cfg.save();
        return cfg;
    }

    private void sanitize() {
        if (volume < 0f || Float.isNaN(volume)) volume = 0f;
        if (volume > 1f) volume = 1f;
        if (overlayCorner == null) overlayCorner = Corner.TOP_RIGHT;
        if (repeatMode == null) repeatMode = RepeatMode.OFF;
        if (history == null) history = new ArrayList<>();
        if (overlayDurationSeconds < 1) overlayDurationSeconds = 1;
        if (overlayDurationSeconds > 120) overlayDurationSeconds = 120;
        configVersion = VERSION;
    }

    public void save() {
        Path path = file();
        try {
            Files.createDirectories(path.getParent());
            try (Writer w = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                GSON.toJson(this, w);
            }
        } catch (Exception e) {
            JukeboxPlus.LOGGER.warn("Could not save {}", path, e);
        }
    }
}
