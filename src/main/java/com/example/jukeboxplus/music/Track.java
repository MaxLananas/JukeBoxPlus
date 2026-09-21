package com.example.jukeboxplus.music;

import java.util.Locale;
import java.util.Objects;

/**
 * Immutable description of a vanilla music track.
 * <p>
 * {@code path} is the sound file path relative to {@code assets/minecraft/sounds/}
 * without extension (for example {@code music/game/sweden} or {@code records/cat}).
 * It is the stable key used everywhere in the mod.
 */
public final class Track {

    public enum Category {
        DISCS("discs"), OVERWORLD("overworld"), CREATIVE("creative"), MENU("menu"),
        NETHER("nether"), END("end"), WATER("water"), UNKNOWN("unknown");

        public final String key;

        Category(String key) { this.key = key; }

        public static Category fromKey(String key) {
            if (key == null) return UNKNOWN;
            for (Category c : values()) if (c.key.equalsIgnoreCase(key)) return c;
            return UNKNOWN;
        }
    }

    private final String path;
    private final String title;
    private final String artist;
    private final Category category;

    public Track(String path, String title, String artist, Category category) {
        this.path = Objects.requireNonNull(path);
        this.title = title == null || title.isBlank() ? prettify(path) : title;
        this.artist = artist == null ? "" : artist;
        this.category = category == null ? Category.UNKNOWN : category;
    }

    public String getPath()         { return path; }
    public String getTitle()        { return title; }
    public String getArtist()       { return artist; }
    public Category getCategory()   { return category; }
    public boolean isDisc()         { return category == Category.DISCS; }

    /** Sound event id registered by this mod for the track, e.g. {@code jukeboxplus:music/game/sweden}. */
    public String getSoundEventId() { return "jukeboxplus:" + path; }

    /** Turns {@code music/game/creative/aria_math} into {@code Aria Math}. */
    public static String prettify(String path) {
        String name = path;
        int slash = name.lastIndexOf('/');
        if (slash >= 0) name = name.substring(slash + 1);
        int dot = name.lastIndexOf('.');
        if (dot > 0) name = name.substring(0, dot);
        StringBuilder sb = new StringBuilder();
        for (String word : name.split("[_\\-]+")) {
            if (word.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1).toLowerCase(Locale.ROOT));
        }
        return sb.length() == 0 ? path : sb.toString();
    }

    @Override public boolean equals(Object o) { return o instanceof Track t && t.path.equals(path); }
    @Override public int hashCode()          { return path.hashCode(); }
    @Override public String toString()       { return title + " (" + path + ")"; }
}
