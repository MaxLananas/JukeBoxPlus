package com.example.jukeboxplus.music;

import com.example.jukeboxplus.JukeboxPlus;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Catalogue of vanilla music tracks. Loaded once from {@code assets/jukeboxplus/tracks.json}
 * which is generated per Minecraft version at build time so it only lists tracks that
 * really exist in the targeted version.
 */
public final class TrackDatabase {

    private static final Map<String, Track> BY_PATH = new LinkedHashMap<>();
    private static final List<Track> ALL = new ArrayList<>();
    private static boolean loaded;

    private TrackDatabase() {}

    public static synchronized void load() {
        if (loaded) return;
        loaded = true;
        try (InputStream in = TrackDatabase.class.getResourceAsStream("/assets/jukeboxplus/tracks.json")) {
            if (in == null) {
                JukeboxPlus.LOGGER.error("tracks.json is missing from the jar, the music list will be empty");
                return;
            }
            JsonArray arr = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonArray();
            for (JsonElement el : arr) {
                JsonObject o = el.getAsJsonObject();
                String path = o.get("path").getAsString();
                Track t = new Track(path,
                        str(o, "title"), str(o, "artist"),
                        Track.Category.fromKey(str(o, "category")));
                if (BY_PATH.put(path, t) == null) ALL.add(t);
            }
            JukeboxPlus.LOGGER.info("Loaded {} music tracks", ALL.size());
        } catch (Exception e) {
            JukeboxPlus.LOGGER.error("Failed to read tracks.json", e);
        }
    }

    private static String str(JsonObject o, String key) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : null;
    }

    public static List<Track> all() {
        load();
        return Collections.unmodifiableList(ALL);
    }

    public static Track byPath(String path) {
        load();
        return path == null ? null : BY_PATH.get(path);
    }

    /**
     * Resolve any sound-ish identifier to a known track. Accepts a sound file path
     * ({@code minecraft:music/game/sweden}), a mod sound event ({@code jukeboxplus:records/cat})
     * or a vanilla music disc event ({@code minecraft:music_disc.cat}).
     */
    public static Track resolve(String id) {
        load();
        if (id == null || id.isEmpty()) return null;
        String path = id;
        int colon = path.indexOf(':');
        if (colon >= 0) path = path.substring(colon + 1);
        Track t = BY_PATH.get(path);
        if (t != null) return t;
        if (path.startsWith("music_disc.")) {
            t = BY_PATH.get("records/" + path.substring("music_disc.".length()));
            if (t != null) return t;
        }
        // "sounds/music/game/sweden.ogg" style paths
        if (path.startsWith("sounds/")) path = path.substring("sounds/".length());
        if (path.endsWith(".ogg")) path = path.substring(0, path.length() - 4);
        return BY_PATH.get(path);
    }

    /** Resolve or create a placeholder so unknown music still shows up in the overlay. */
    public static Track resolveOrUnknown(String id) {
        Track t = resolve(id);
        if (t != null) return t;
        String path = id;
        int colon = path.indexOf(':');
        if (colon >= 0) path = path.substring(colon + 1);
        Track.Category cat = path.contains("records") || path.contains("music_disc") ? Track.Category.DISCS : Track.Category.UNKNOWN;
        return new Track(path, Track.prettify(path), "", cat);
    }

    public static List<Track> search(String query, Track.Category category) {
        load();
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        List<Track> out = new ArrayList<>();
        for (Track t : ALL) {
            if (category != null && t.getCategory() != category) continue;
            if (!q.isEmpty()
                    && !t.getTitle().toLowerCase(Locale.ROOT).contains(q)
                    && !t.getArtist().toLowerCase(Locale.ROOT).contains(q)
                    && !t.getPath().toLowerCase(Locale.ROOT).contains(q)) continue;
            out.add(t);
        }
        return out;
    }
}
