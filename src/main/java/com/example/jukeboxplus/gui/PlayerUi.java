package com.example.jukeboxplus.gui;

import com.example.jukeboxplus.JukeboxPlus;
import com.example.jukeboxplus.config.ModConfig;
import com.example.jukeboxplus.music.MusicPlayer;
import com.example.jukeboxplus.music.MusicTracker;
import com.example.jukeboxplus.music.Track;
import com.example.jukeboxplus.music.TrackDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * All the logic and drawing of the music player window. Version-agnostic: the
 * per-version {@code PlayerScreen} only forwards input and provides a {@link Gfx}.
 * <p>
 * Layout (all coordinates relative to the panel origin):
 * <pre>
 *  ┌ title bar ───────────────────────────── [⚙] [x] ┐
 *  │ tabs: All Discs Overworld ...                     │
 *  │ [search box]                       n tracks        │
 *  │ ┌ list ──────────────────────────────────┐▌      │
 *  │ │ ♪ Title                     Artist  3:12 │▌      │
 *  │ └──────────────────────────────────────────┘      │
 *  │ now playing: Title - Artist         0:42 ▬▬▬▬     │
 *  │ [⏮] [⏯] [⏹] [⏭]   [🔀] [🔁]      vol ▬▬▬▬▬ 80%   │
 *  └───────────────────────────────────────────────────┘
 * </pre>
 */
public final class PlayerUi {

    // ----------------------------------------------------------------- layout
    public static final int MAX_W = 360;
    public static final int MAX_H = 260;
    private static final int PAD = 8;
    private static final int TITLE_H = 20;
    private static final int TAB_H = 14;
    private static final int SEARCH_H = 16;
    private static final int ROW_H = 18;
    private static final int FOOTER_H = 58;
    private static final int SCROLLBAR_W = 4;

    private enum Tab {
        ALL("jukeboxplus.tab.all", null),
        DISCS("jukeboxplus.tab.discs", Track.Category.DISCS),
        OVERWORLD("jukeboxplus.tab.overworld", Track.Category.OVERWORLD),
        CREATIVE("jukeboxplus.tab.creative", Track.Category.CREATIVE),
        NETHER("jukeboxplus.tab.nether", Track.Category.NETHER),
        END("jukeboxplus.tab.end", Track.Category.END),
        WATER("jukeboxplus.tab.water", Track.Category.WATER),
        MENU("jukeboxplus.tab.menu", Track.Category.MENU),
        HISTORY("jukeboxplus.tab.history", null);

        final String key;
        final Track.Category category;

        Tab(String key, Track.Category category) {
            this.key = key;
            this.category = category;
        }
    }

    /** A clickable rectangle registered during drawing, hit-tested on click. */
    private static final class Hit {
        final int x, y, w, h;
        final Runnable action;
        final String tooltip;

        Hit(int x, int y, int w, int h, String tooltip, Runnable action) {
            this.x = x; this.y = y; this.w = w; this.h = h; this.tooltip = tooltip; this.action = action;
        }

        boolean contains(double mx, double my) { return Ui.inside(mx, my, x, y, w, h); }
    }

    private final MusicPlayer player;
    private final MusicTracker tracker;

    private Tab tab = Tab.ALL;
    private String query = "";
    private List<Track> visible = new ArrayList<>();
    private boolean listDirty = true;
    private double scroll;          // pixels
    private boolean draggingScroll;
    private boolean draggingVolume;
    private boolean settingsOpen;
    private final List<Hit> hits = new ArrayList<>();
    private long openedAt = System.currentTimeMillis();

    // Panel geometry, recomputed on every draw so it follows window resizes.
    private int px, py, pw, ph;
    private int listX, listY, listW, listH;
    private int volX, volY, volW;

    public PlayerUi(MusicPlayer player, MusicTracker tracker) {
        this.player = player;
        this.tracker = tracker;
    }

    // ------------------------------------------------------------------ state

    public String getQuery() { return query; }

    public void setQuery(String q) {
        String nq = q == null ? "" : q;
        if (!nq.equals(query)) {
            query = nq;
            listDirty = true;
            scroll = 0;
        }
    }

    public boolean isSettingsOpen() { return settingsOpen; }

    private List<Track> visibleTracks() {
        if (listDirty) {
            visible = tab == Tab.HISTORY ? filterHistory() : TrackDatabase.search(query, tab.category);
            listDirty = false;
        }
        return visible;
    }

    private List<Track> filterHistory() {
        List<Track> hist = tracker.getHistory();
        if (query.isEmpty()) return hist;
        List<Track> out = new ArrayList<>();
        String q = query.toLowerCase(java.util.Locale.ROOT);
        for (Track t : hist) {
            if (t.getTitle().toLowerCase(java.util.Locale.ROOT).contains(q) || t.getArtist().toLowerCase(java.util.Locale.ROOT).contains(q)) out.add(t);
        }
        return out;
    }

    /** Rectangle where the version-specific search box must be placed: {x, y, w, h}. */
    public int[] searchBox(int screenW, int screenH) {
        computePanel(screenW, screenH);
        int y = py + TITLE_H + TAB_H + PAD;
        return new int[]{px + PAD, y, pw - PAD * 2 - 70, SEARCH_H};
    }

    private void computePanel(int screenW, int screenH) {
        pw = Math.min(MAX_W, screenW - 16);
        ph = Math.min(MAX_H, screenH - 16);
        px = (screenW - pw) / 2;
        py = (screenH - ph) / 2;
        listX = px + PAD;
        listY = py + TITLE_H + TAB_H + PAD + SEARCH_H + 4;
        listW = pw - PAD * 2;
        listH = py + ph - FOOTER_H - listY;
        if (listH < ROW_H) listH = ROW_H;
    }

    // ------------------------------------------------------------------- draw

    public void draw(Gfx g, int mouseX, int mouseY, boolean searchFocused) {
        hits.clear();
        computePanel(g.screenWidth(), g.screenHeight());

        // Dim the game behind, with a short fade-in.
        float t = Ui.ease((System.currentTimeMillis() - openedAt) / 150f);
        g.fill(0, 0, g.screenWidth(), g.screenHeight(), Colors.withAlpha(0xA0000000, t));

        // Panel
        g.rect(px, py, pw, ph, Colors.BG);
        g.border(px, py, pw, ph, Colors.BORDER);

        drawTitleBar(g, mouseX, mouseY);
        if (settingsOpen) {
            drawSettings(g, mouseX, mouseY);
        } else {
            drawTabs(g, mouseX, mouseY);
            drawSearchHint(g, searchFocused);
            drawList(g, mouseX, mouseY);
        }
        drawFooter(g, mouseX, mouseY);
        drawTooltip(g, mouseX, mouseY);
    }

    private void drawTitleBar(Gfx g, int mx, int my) {
        g.rect(px, py, pw, TITLE_H, Colors.BG_LIGHT);
        g.rect(px, py + TITLE_H - 1, pw, 1, Colors.BORDER);
        g.text("\u266B", px + PAD, py + 6, Colors.ACCENT, false);
        g.text(Ui.tr("jukeboxplus.screen.title"), px + PAD + 12, py + 6, Colors.TEXT, true);
        String ver = "v" + JukeboxPlus.VERSION;
        g.text(ver, px + PAD + 12 + g.textWidth(Ui.tr("jukeboxplus.screen.title")) + 6, py + 6, Colors.TEXT_MUTED, false);

        int bx = px + pw - PAD - 12;
        iconButton(g, mx, my, bx, py + 4, 12, 12, "\u00D7", Ui.tr("jukeboxplus.screen.close"), false, this::requestClose);
        bx -= 16;
        iconButton(g, mx, my, bx, py + 4, 12, 12, "\u2699", Ui.tr("jukeboxplus.screen.settings"), settingsOpen, () -> settingsOpen = !settingsOpen);
    }

    private void drawTabs(Gfx g, int mx, int my) {
        int x = px + PAD;
        int y = py + TITLE_H + 2;
        for (Tab tb : Tab.values()) {
            String label = Ui.tr(tb.key);
            int w = g.textWidth(label) + 8;
            if (x + w > px + pw - PAD) break; // narrow screens: drop trailing tabs
            boolean sel = tb == tab;
            boolean hover = Ui.inside(mx, my, x, y, w, TAB_H - 2);
            if (sel) {
                g.rect(x, y + TAB_H - 3, w, 2, Colors.ACCENT);
            } else if (hover) {
                g.rect(x, y + TAB_H - 3, w, 2, Colors.BORDER);
            }
            g.text(label, x + 4, y + 1, sel ? Colors.TEXT : hover ? Colors.TEXT_DIM : Colors.TEXT_MUTED, false);
            final Tab target = tb;
            hits.add(new Hit(x, y, w, TAB_H, null, () -> selectTab(target)));
            x += w + 2;
        }
    }

    private void selectTab(Tab t) {
        if (tab != t) {
            tab = t;
            listDirty = true;
            scroll = 0;
        }
    }

    private void drawSearchHint(Gfx g, boolean focused) {
        int[] sb = searchBox(g.screenWidth(), g.screenHeight());
        // Count on the right of the search box
        String count = Ui.tr("jukeboxplus.screen.tracks", visibleTracks().size());
        g.textRight(count, px + pw - PAD, sb[1] + 4, Colors.TEXT_MUTED, false);
        if (tab == Tab.HISTORY && !tracker.getHistory().isEmpty()) {
            String clear = Ui.tr("jukeboxplus.screen.history.clear");
            int cw = g.textWidth(clear) + 8;
            int cx = px + pw - PAD - cw;
            // Draw the clear button just below the count, overlapping the first list row's right margin is avoided by list padding
            hits.add(new Hit(cx, sb[1] - 1, cw, SEARCH_H + 2, null, () -> { tracker.clearHistory(); listDirty = true; }));
        }
    }

    private void drawList(Gfx g, int mx, int my) {
        List<Track> tracks = visibleTracks();
        g.rect(listX, listY, listW, listH, Colors.BG_LIGHT);
        g.border(listX, listY, listW, listH, Colors.BORDER);

        if (tracks.isEmpty()) {
            String msg = Ui.tr(tab == Tab.HISTORY ? "jukeboxplus.screen.history.empty" : "jukeboxplus.screen.empty");
            g.centeredText(msg, listX + listW / 2, listY + listH / 2 - 4, Colors.TEXT_MUTED, false);
            return;
        }

        int contentH = tracks.size() * ROW_H;
        int maxScroll = Math.max(0, contentH - (listH - 2));
        if (scroll > maxScroll) scroll = maxScroll;
        if (scroll < 0) scroll = 0;
        boolean hasBar = contentH > listH - 2;
        int rowW = listW - 2 - (hasBar ? SCROLLBAR_W + 2 : 0);

        Track playing = player.getCurrentTrack();
        boolean mouseInList = Ui.inside(mx, my, listX + 1, listY + 1, rowW, listH - 2);

        g.scissorOn(listX + 1, listY + 1, listX + 1 + rowW, listY + listH - 1);
        int first = (int) (scroll / ROW_H);
        int y = listY + 1 - (int) (scroll % ROW_H);
        for (int i = first; i < tracks.size() && y < listY + listH; i++, y += ROW_H) {
            Track tr = tracks.get(i);
            boolean isCurrent = tr.equals(playing);
            boolean hover = mouseInList && my >= y && my < y + ROW_H;
            int bg = isCurrent ? Colors.PANEL_ACTIVE : hover ? Colors.PANEL_HOVER : (i % 2 == 0 ? Colors.BG_LIGHT : Colors.PANEL);
            g.rect(listX + 1, y, rowW, ROW_H, bg);
            if (isCurrent) g.rect(listX + 1, y, 2, ROW_H, Colors.ACCENT);

            String icon = isCurrent ? (player.isPaused() ? "\u2016" : "\u25B6") : (tr.isDisc() ? "\u25C9" : "\u266A");
            g.text(icon, listX + 8, y + 5, isCurrent ? Colors.ACCENT : Colors.TEXT_MUTED, false);

            String artist = tr.getArtist();
            int artistW = artist.isEmpty() ? 0 : g.textWidth(artist);
            int titleMax = rowW - 24 - artistW - 12;
            g.textClipped(tr.getTitle(), listX + 20, y + 5, titleMax, isCurrent ? Colors.TEXT : Colors.TEXT, false);
            if (!artist.isEmpty()) g.textRight(artist, listX + 1 + rowW - 6, y + 5, Colors.TEXT_DIM, false);

            final Track target = tr;
            final int idx = i;
            final List<Track> queue = tracks;
            hits.add(new Hit(listX + 1, Math.max(y, listY + 1), rowW, Math.min(ROW_H, listY + listH - 1 - y), null,
                    () -> onRowClick(target, queue, idx)));
        }
        g.scissorOff();

        if (hasBar) {
            int barX = listX + listW - 1 - SCROLLBAR_W;
            int trackH = listH - 2;
            g.rect(barX, listY + 1, SCROLLBAR_W, trackH, Colors.TRACK_BG);
            int thumbH = Math.max(12, (int) ((long) trackH * trackH / contentH));
            int thumbY = listY + 1 + (int) ((trackH - thumbH) * (scroll / maxScroll));
            boolean hov = Ui.inside(mx, my, barX, listY, SCROLLBAR_W, listH) || draggingScroll;
            g.rect(barX, thumbY, SCROLLBAR_W, thumbH, hov ? Colors.ACCENT : Colors.TEXT_MUTED);
        }
    }

    private void onRowClick(Track track, List<Track> queue, int idx) {
        if (track.equals(player.getCurrentTrack())) player.togglePause();
        else player.play(track, queue, idx);
    }

    private void drawFooter(Gfx g, int mx, int my) {
        int fy = py + ph - FOOTER_H;
        g.rect(px, fy, pw, 1, Colors.BORDER);
        g.rect(px, fy + 1, pw, FOOTER_H - 1, Colors.BG_LIGHT);

        // Now playing line
        Track cur = player.getCurrentTrack();
        Track shown = cur != null ? cur : tracker.getCurrent();
        int ty = fy + 6;
        if (shown == null) {
            g.text(Ui.tr("jukeboxplus.screen.nothing_playing"), px + PAD, ty, Colors.TEXT_MUTED, false);
        } else {
            String label = cur != null
                    ? Ui.tr(player.isPaused() ? "jukeboxplus.screen.paused" : "jukeboxplus.screen.now_playing")
                    : Ui.tr("jukeboxplus.screen.game_music");
            g.text(label.toUpperCase(java.util.Locale.ROOT), px + PAD, ty, cur != null && !player.isPaused() ? Colors.ACCENT : Colors.TEXT_MUTED, false);
            String time = Ui.formatTime(cur != null ? player.elapsedMs() : tracker.elapsedMs());
            g.textRight(time, px + pw - PAD, ty, Colors.TEXT_DIM, false);
            String line = shown.getArtist().isEmpty() ? shown.getTitle() : shown.getTitle() + "  \u2014  " + shown.getArtist();
            g.textClipped(line, px + PAD, ty + 11, pw - PAD * 2, Colors.TEXT, false);
        }

        // Transport buttons
        int by = fy + 32;
        int bx = px + PAD;
        bx = textButton(g, mx, my, bx, by, "\u23EE", Ui.tr("jukeboxplus.button.previous"), false, player::previous);
        boolean playing = player.isPlaying();
        bx = textButton(g, mx, my, bx, by, playing ? "\u2016" : "\u25B6", Ui.tr(playing ? "jukeboxplus.button.pause" : "jukeboxplus.button.play"), playing, this::playPause);
        bx = textButton(g, mx, my, bx, by, "\u25A0", Ui.tr("jukeboxplus.button.stop"), false, player::stop);
        bx = textButton(g, mx, my, bx, by, "\u23ED", Ui.tr("jukeboxplus.button.next"), false, player::next);
        bx += 6;
        ModConfig cfg = ModConfig.get();
        bx = textButton(g, mx, my, bx, by, "\u2928", Ui.tr("jukeboxplus.button.shuffle"), cfg.shuffle, () -> { cfg.shuffle = !cfg.shuffle; cfg.save(); });
        String repeatKey = "jukeboxplus.button.repeat." + cfg.repeatMode.name().toLowerCase(java.util.Locale.ROOT);
        String repeatIcon = cfg.repeatMode == ModConfig.RepeatMode.ONE ? "\u21BB1" : "\u21BB";
        bx = textButton(g, mx, my, bx, by, repeatIcon, Ui.tr(repeatKey), cfg.repeatMode != ModConfig.RepeatMode.OFF, this::cycleRepeat);

        // Volume slider
        volW = Math.min(90, px + pw - PAD - bx - 40);
        volX = px + pw - PAD - 28 - volW;
        volY = by + 7;
        if (volW >= 30) {
            g.text("\uD83D\uDD0A", volX - 12, by + 5, Colors.TEXT_MUTED, false);
            g.rect(volX, volY, volW, 4, Colors.TRACK_BG);
            int filled = Math.round(volW * cfg.volume);
            g.rect(volX, volY, filled, 4, Colors.ACCENT);
            g.rect(volX + filled - 1, volY - 2, 3, 8, Colors.TEXT);
            g.textRight(Math.round(cfg.volume * 100) + "%", px + pw - PAD, by + 5, Colors.TEXT_DIM, false);
            hits.add(new Hit(volX - 4, volY - 6, volW + 8, 16, null, null)); // consumed by mouse handling
        }
    }

    private void playPause() {
        if (player.hasTrack()) {
            player.togglePause();
        } else {
            List<Track> tracks = visibleTracks();
            if (!tracks.isEmpty()) player.play(tracks.get(0), tracks, 0);
        }
    }

    private void cycleRepeat() {
        ModConfig cfg = ModConfig.get();
        ModConfig.RepeatMode[] modes = ModConfig.RepeatMode.values();
        cfg.repeatMode = modes[(cfg.repeatMode.ordinal() + 1) % modes.length];
        cfg.save();
    }

    private void drawSettings(Gfx g, int mx, int my) {
        ModConfig cfg = ModConfig.get();
        int x = px + PAD;
        int y = py + TITLE_H + PAD;
        int w = pw - PAD * 2;
        g.text(Ui.tr("jukeboxplus.screen.settings").toUpperCase(java.util.Locale.ROOT), x, y, Colors.TEXT_MUTED, false);
        y += 14;
        y = toggleRow(g, mx, my, x, y, w, Ui.tr("jukeboxplus.settings.overlay"), cfg.overlayEnabled, () -> cfg.overlayEnabled = !cfg.overlayEnabled);
        y = toggleRow(g, mx, my, x, y, w, Ui.tr("jukeboxplus.settings.overlay_always"), cfg.overlayAlwaysVisible, () -> cfg.overlayAlwaysVisible = !cfg.overlayAlwaysVisible);
        y = toggleRow(g, mx, my, x, y, w, Ui.tr("jukeboxplus.settings.overlay_vanilla"), cfg.overlayForVanillaMusic, () -> cfg.overlayForVanillaMusic = !cfg.overlayForVanillaMusic);
        y = toggleRow(g, mx, my, x, y, w, Ui.tr("jukeboxplus.settings.stop_vanilla"), cfg.stopVanillaMusic, () -> cfg.stopVanillaMusic = !cfg.stopVanillaMusic);
        String corner = Ui.tr("jukeboxplus.corner." + cfg.overlayCorner.name().toLowerCase(java.util.Locale.ROOT));
        y = valueRow(g, mx, my, x, y, w, Ui.tr("jukeboxplus.settings.overlay_corner"), corner, () -> {
            ModConfig.Corner[] c = ModConfig.Corner.values();
            cfg.overlayCorner = c[(cfg.overlayCorner.ordinal() + 1) % c.length];
        });
        y = valueRow(g, mx, my, x, y, w, Ui.tr("jukeboxplus.settings.duration", cfg.overlayDurationSeconds), "\u25C0 \u25B6", () -> {
            int[] steps = {3, 5, 8, 12, 20, 30, 60};
            int next = steps[0];
            for (int s : steps) if (s > cfg.overlayDurationSeconds) { next = s; break; }
            cfg.overlayDurationSeconds = next;
        });
        y += 6;
        g.text("Minecraft " + com.example.jukeboxplus.compat.Compat.targetVersion(), x, y, Colors.TEXT_MUTED, false);
    }

    private int toggleRow(Gfx g, int mx, int my, int x, int y, int w, String label, boolean on, Runnable toggle) {
        int h = 16;
        boolean hover = Ui.inside(mx, my, x, y, w, h);
        g.rect(x, y, w, h, hover ? Colors.PANEL_HOVER : Colors.PANEL);
        g.text(label, x + 6, y + 4, Colors.TEXT, false);
        int sw = 22, sh = 10;
        int sx = x + w - 6 - sw, sy = y + 3;
        g.rect(sx, sy, sw, sh, on ? Colors.ACCENT_DARK : Colors.TRACK_BG);
        g.rect(on ? sx + sw - sh : sx, sy, sh, sh, on ? Colors.ACCENT : Colors.TEXT_MUTED);
        hits.add(new Hit(x, y, w, h, null, () -> { toggle.run(); ModConfig.get().save(); }));
        return y + h + 3;
    }

    private int valueRow(Gfx g, int mx, int my, int x, int y, int w, String label, String value, Runnable next) {
        int h = 16;
        boolean hover = Ui.inside(mx, my, x, y, w, h);
        g.rect(x, y, w, h, hover ? Colors.PANEL_HOVER : Colors.PANEL);
        g.text(label, x + 6, y + 4, Colors.TEXT, false);
        g.textRight(value, x + w - 6, y + 4, hover ? Colors.ACCENT : Colors.TEXT_DIM, false);
        hits.add(new Hit(x, y, w, h, null, () -> { next.run(); ModConfig.get().save(); }));
        return y + h + 3;
    }

    private int textButton(Gfx g, int mx, int my, int x, int y, String icon, String tooltip, boolean active, Runnable action) {
        int w = 20, h = 18;
        boolean hover = Ui.inside(mx, my, x, y, w, h);
        g.rect(x, y, w, h, active ? Colors.ACCENT_DARK : hover ? Colors.PANEL_HOVER : Colors.PANEL);
        if (hover) g.border(x, y, w, h, active ? Colors.ACCENT : Colors.BORDER);
        g.centeredText(icon, x + w / 2, y + 5, active ? Colors.TEXT : hover ? Colors.TEXT : Colors.TEXT_DIM, false);
        hits.add(new Hit(x, y, w, h, tooltip, action));
        return x + w + 3;
    }

    private void iconButton(Gfx g, int mx, int my, int x, int y, int w, int h, String icon, String tooltip, boolean active, Runnable action) {
        boolean hover = Ui.inside(mx, my, x, y, w, h);
        if (hover || active) g.rect(x, y, w, h, active ? Colors.ACCENT_DARK : Colors.PANEL_HOVER);
        g.centeredText(icon, x + w / 2, y + 2, hover || active ? Colors.TEXT : Colors.TEXT_DIM, false);
        hits.add(new Hit(x, y, w, h, tooltip, action));
    }

    private void drawTooltip(Gfx g, int mx, int my) {
        for (Hit h : hits) {
            if (h.tooltip != null && h.contains(mx, my)) {
                int w = g.textWidth(h.tooltip) + 8;
                int x = Math.min(mx + 8, g.screenWidth() - w - 2);
                int y = my - 14;
                g.rect(x, y, w, 12, 0xF0101014);
                g.border(x, y, w, 12, Colors.BORDER);
                g.text(h.tooltip, x + 4, y + 2, Colors.TEXT, false);
                return;
            }
        }
    }

    // ------------------------------------------------------------------ input

    private Runnable closeRequest;

    public void setCloseHandler(Runnable r) { closeRequest = r; }

    private void requestClose() { if (closeRequest != null) closeRequest.run(); }

    /** @return true when the click was consumed. */
    public boolean mouseClicked(double mx, double my, int button) {
        if (button != 0) return false;
        // Volume slider (also starts dragging)
        if (volW >= 30 && Ui.inside(mx, my, volX - 4, volY - 6, volW + 8, 16)) {
            draggingVolume = true;
            setVolumeFromMouse(mx);
            return true;
        }
        // Scrollbar
        int contentH = visibleTracks().size() * ROW_H;
        if (contentH > listH - 2 && Ui.inside(mx, my, listX + listW - 1 - SCROLLBAR_W - 2, listY, SCROLLBAR_W + 3, listH)) {
            draggingScroll = true;
            scrollFromMouse(my);
            return true;
        }
        for (Hit h : hits) {
            if (h.action != null && h.contains(mx, my)) {
                h.action.run();
                return true;
            }
        }
        // Click outside the panel closes it
        if (!Ui.inside(mx, my, px, py, pw, ph)) {
            requestClose();
            return true;
        }
        return false;
    }

    public boolean mouseReleased(double mx, double my, int button) {
        boolean was = draggingScroll || draggingVolume;
        draggingScroll = false;
        if (draggingVolume) {
            draggingVolume = false;
            ModConfig.get().save();
        }
        return was;
    }

    public boolean mouseDragged(double mx, double my) {
        if (draggingVolume) { setVolumeFromMouse(mx); return true; }
        if (draggingScroll) { scrollFromMouse(my); return true; }
        return false;
    }

    public boolean mouseScrolled(double mx, double my, double amount) {
        if (settingsOpen) return false;
        if (Ui.inside(mx, my, listX, listY, listW, listH)) {
            scroll -= amount * ROW_H * 2;
            return true;
        }
        if (volW >= 30 && Ui.inside(mx, my, volX - 16, volY - 8, volW + 50, 20)) {
            player.adjustVolume((float) (amount > 0 ? 0.05 : -0.05));
            return true;
        }
        return false;
    }

    private void setVolumeFromMouse(double mx) {
        float v = (float) ((mx - volX) / volW);
        v = Math.max(0f, Math.min(1f, v));
        ModConfig.get().volume = Math.round(v * 100f) / 100f;
        player.applyVolume();
    }

    private void scrollFromMouse(double my) {
        int contentH = visibleTracks().size() * ROW_H;
        int trackH = listH - 2;
        int maxScroll = Math.max(0, contentH - trackH);
        int thumbH = Math.max(12, (int) ((long) trackH * trackH / Math.max(1, contentH)));
        double rel = (my - listY - 1 - thumbH / 2.0) / Math.max(1, trackH - thumbH);
        scroll = Math.max(0, Math.min(maxScroll, rel * maxScroll));
    }

    /** Keys the UI reacts to; mapped from the version-specific key codes by the screen. */
    public enum Key { SPACE, LEFT, RIGHT, UP, DOWN }

    /**
     * Keyboard shortcuts while the search box is not focused.
     * @return true when handled
     */
    public boolean keyPressed(Key key, boolean searchFocused) {
        if (searchFocused || key == null) return false;
        switch (key) {
            case SPACE: playPause(); return true;
            case RIGHT: player.next(); return true;
            case LEFT:  player.previous(); return true;
            case UP:    player.adjustVolume(0.05f); return true;
            case DOWN:  player.adjustVolume(-0.05f); return true;
            default:    return false;
        }
    }

    /** Called from the screen's tick so the history tab refreshes while open. */
    public void tick() {
        if (tab == Tab.HISTORY) listDirty = true;
    }
}
