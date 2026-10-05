package com.example.jukeboxplus.gui;

import com.example.jukeboxplus.config.ModConfig;
import com.example.jukeboxplus.music.MusicPlayer;
import com.example.jukeboxplus.music.MusicTracker;
import com.example.jukeboxplus.music.Track;

/**
 * The small "now playing" card on the HUD. Slides in when a track starts and fades
 * out after a few seconds (or stays, depending on the settings).
 */
public final class OverlayUi {

    private static final int W = 150;
    private static final int H = 30;
    private static final int MARGIN = 6;
    private static final long SLIDE_MS = 250;
    private static final long FADE_MS = 400;

    private final MusicPlayer player;
    private final MusicTracker tracker;

    private Track shown;
    private long shownSince;
    private boolean forcedVisible;   // toggled by the key binding
    private long lastToggleAt;

    public OverlayUi(MusicPlayer player, MusicTracker tracker) {
        this.player = player;
        this.tracker = tracker;
    }

    /** Key binding: show the card for a moment even if it already faded. */
    public void toggle() {
        forcedVisible = !forcedVisible;
        lastToggleAt = System.currentTimeMillis();
        if (forcedVisible) shownSince = lastToggleAt;
    }

    public void draw(Gfx g) {
        ModConfig cfg = ModConfig.get();
        if (!cfg.overlayEnabled) return;

        Track current = tracker.getCurrent();
        if (current != null && tracker.getSource() == MusicTracker.Source.GAME && !cfg.overlayForVanillaMusic) current = null;

        long now = System.currentTimeMillis();
        if (current != null && !current.equals(shown)) {
            shown = current;
            shownSince = now;
        }
        if (current == null) {
            if (shown == null) return;
            // Track ended: fade out quickly then forget it.
            long since = now - shownSince;
            if (!forcedVisible && since > FADE_MS) { shown = null; return; }
        }

        long age = now - shownSince;
        float alpha;
        if (current == null) {
            alpha = 1f - Math.min(1f, age / (float) FADE_MS);
        } else if (forcedVisible || cfg.overlayAlwaysVisible) {
            alpha = 1f;
        } else {
            long visibleMs = cfg.overlayDurationSeconds * 1000L;
            if (age > visibleMs + FADE_MS) return;
            alpha = age <= visibleMs ? 1f : 1f - (age - visibleMs) / (float) FADE_MS;
        }
        if (alpha <= 0.02f) return;

        float slide = Ui.ease(Math.min(1f, age / (float) SLIDE_MS));
        int sw = g.screenWidth(), sh = g.screenHeight();
        boolean right = cfg.overlayCorner == ModConfig.Corner.TOP_RIGHT || cfg.overlayCorner == ModConfig.Corner.BOTTOM_RIGHT;
        boolean bottom = cfg.overlayCorner == ModConfig.Corner.BOTTOM_LEFT || cfg.overlayCorner == ModConfig.Corner.BOTTOM_RIGHT;
        int offset = Math.round((1f - slide) * (W + MARGIN));
        int x = right ? sw - MARGIN - W + offset : MARGIN - offset;
        int y = bottom ? sh - MARGIN - H : MARGIN;
        // Avoid the chat area on the bottom-left corner.
        if (bottom && !right) y -= 40;

        draw(g, x, y, shown, alpha);
    }

    private void draw(Gfx g, int x, int y, Track track, float a) {
        boolean own = player.getCurrentTrack() != null && player.getCurrentTrack().equals(track);
        boolean paused = own && player.isPaused();

        g.rect(x, y, W, H, Colors.withAlpha(Colors.BG, a));
        g.rect(x, y, 2, H, Colors.withAlpha(paused ? Colors.WARN : Colors.ACCENT, a));

        // Animated bars
        int bx = x + 8, by = y + 8;
        long t = System.currentTimeMillis();
        for (int i = 0; i < 4; i++) {
            int h;
            if (paused || !tracker.isPlaying()) h = 3;
            else h = 3 + (int) (7 * (0.5 + 0.5 * Math.sin((t / 120.0) + i * 1.3)));
            g.rect(bx + i * 3, by + 14 - h, 2, h, Colors.withAlpha(paused ? Colors.TEXT_MUTED : Colors.ACCENT, a));
        }

        int tx = x + 24;
        int maxW = W - 30;
        String label = paused ? Ui.tr("jukeboxplus.overlay.paused")
                : own ? Ui.tr("jukeboxplus.overlay.title") : Ui.tr("jukeboxplus.overlay.game");
        g.text(label.toUpperCase(java.util.Locale.ROOT), tx, y + 5, Colors.withAlpha(Colors.TEXT_MUTED, a), false);
        String time = Ui.formatTime(own ? player.elapsedMs() : tracker.elapsedMs());
        g.textRight(time, x + W - 6, y + 5, Colors.withAlpha(Colors.TEXT_MUTED, a), false);
        String line = track.getArtist().isEmpty() ? track.getTitle() : track.getTitle() + " \u2014 " + track.getArtist();
        g.textClipped(line, tx, y + 16, maxW, Colors.withAlpha(Colors.TEXT, a), true);
    }
}
