package com.example.jukeboxplus.gui;

import com.example.jukeboxplus.config.ModConfig;
import com.example.jukeboxplus.music.MusicInfo;
import com.example.jukeboxplus.music.MusicPlayer;
import com.example.jukeboxplus.music.MusicTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;

public class MusicOverlay {

    private static final int W = 220;
    private static final int H = 75;
    private static final int HISTORY_W = 200;
    private static final int HISTORY_ITEM_H = 30;
    private static final int BG = 0xE8101018;
    private static final int ACCENT = 0xFF1DB954;

    private final MusicTracker tracker;
    private final MusicPlayer player;
    private final Minecraft client;

    private boolean overlayEnabled = true;
    private boolean showHistory = false;
    private float slideProgress = 0f;

    public MusicOverlay(MusicTracker tracker, MusicPlayer player) {
        this.tracker = tracker;
        this.player = player;
        this.client = Minecraft.getInstance();
    }

    public void render(GuiGraphics g) {
        if (client.player == null || !overlayEnabled) return;
        if (!ModConfig.getInstance().isOverlayEnabled()) return;

        MusicInfo current = player.getCurrentMusic();
        if (current == null) current = tracker.getCurrentMusic();

        boolean shouldShow = current != null;
        float target = shouldShow ? 1f : 0f;
        if (slideProgress < target) slideProgress = Math.min(target, slideProgress + 0.12f);
        else if (slideProgress > target) slideProgress = Math.max(target, slideProgress - 0.12f);

        if (slideProgress < 0.01f && !showHistory) return;

        int sw = client.getWindow().getGuiScaledWidth();
        int sh = client.getWindow().getGuiScaledHeight();

        int x, y;
        switch (ModConfig.getInstance().getPosition()) {
            case TOP_LEFT -> { x = 10; y = 10; }
            case BOTTOM_LEFT -> { x = 10; y = sh - H - 10; }
            case BOTTOM_RIGHT -> { x = sw - W - 10; y = sh - H - 10; }
            default -> { x = sw - W - 10; y = 10; }
        }

        ModConfig.Position pos = ModConfig.getInstance().getPosition();
        int slideOffset = (int)((1 - slideProgress) * (W + 20));
        if (pos == ModConfig.Position.TOP_LEFT || pos == ModConfig.Position.BOTTOM_LEFT) x -= slideOffset;
        else x += slideOffset;

        if (current != null) renderMain(g, x, y, current);

        if (showHistory) {
            int hy = y + H + 8;
            if (pos == ModConfig.Position.BOTTOM_LEFT || pos == ModConfig.Position.BOTTOM_RIGHT) {
                hy = y - 10 - (Math.min(5, tracker.getHistory().size()) * HISTORY_ITEM_H + 30);
            }
            renderHistory(g, x, hy);
        }
    }

    private void renderMain(GuiGraphics g, int x, int y, MusicInfo music) {
        Font f = client.font;
        int borderColor = music.getDiscColor() | 0xFF000000;

        fillRoundRect(g, x, y, W, H, 12, BG);
        drawRoundBorder(g, x, y, W, H, 12, borderColor);

        int dcx = x + 30;
        int dcy = y + 26;
        int dr = 18;
        fillCircle(g, dcx, dcy, dr, borderColor);
        fillCircle(g, dcx, dcy, dr * 2 / 3, darken(borderColor, 0.7f));
        fillCircle(g, dcx, dcy, 4, 0xFF151515);

        int tx = x + 55;
        g.drawString(f, Component.literal("§b" + music.getTitle()), tx, y + 6, 0xFFFFFFFF, true);
        g.drawString(f, Component.literal("§7" + music.getArtist()), tx, y + 18, 0xFFB3B3B3, true);
        g.drawString(f, Component.literal("§8[" + music.getType().displayName + "]"), tx, y + 30, 0xFF535353, false);

        String status = player.isPlaying() ? "Playing" : player.isPaused() ? "Paused" : "Stopped";
        int statusColor = player.isPlaying() ? ACCENT : player.isPaused() ? 0xFFFFAA00 : 0xFFB3B3B3;
        g.drawString(f, Component.literal(status), x + 12, y + 46, statusColor, false);

        if (player.isShuffle()) g.drawString(f, Component.literal("§a[S]"), x + 60, y + 46, ACCENT, false);
        if (player.isRepeat()) g.drawString(f, Component.literal("§a[R]"), x + 80, y + 46, ACCENT, false);

        int vol = (int)(player.getVolume() * 100);
        String volText = "Vol:" + vol + "%";
        g.drawString(f, Component.literal("§7" + volText), x + W - f.width(volText) - 14, y + 46, 0xFFB3B3B3, false);

        int pY = y + H - 10;
        int pW = W - 24;
        float progress = music.getProgress();
        fillRoundRect(g, x + 12, pY, pW, 4, 2, 0xFF404040);
        if ((int)(pW * progress) > 2) fillRoundRect(g, x + 12, pY, (int)(pW * progress), 4, 2, borderColor);
    }

    private void renderHistory(GuiGraphics g, int x, int y) {
        List<MusicInfo> history = tracker.getHistory();
        Font f = client.font;

        if (history.isEmpty()) {
            fillRoundRect(g, x, y, HISTORY_W, 35, 10, BG);
            g.drawString(f, Component.literal("No history yet"), x + 12, y + 12, 0xFFB3B3B3, true);
            return;
        }

        int count = Math.min(5, history.size());
        int totalH = count * HISTORY_ITEM_H + 25;
        fillRoundRect(g, x, y, HISTORY_W, totalH, 10, BG);
        drawRoundBorder(g, x, y, HISTORY_W, totalH, 10, ACCENT);
        g.drawString(f, Component.literal("§aRecently Played"), x + 12, y + 8, ACCENT, true);

        int iy = y + 25;
        for (int i = 0; i < count; i++) {
            MusicInfo m = history.get(i);
            fillCircle(g, x + 18, iy + HISTORY_ITEM_H / 2 - 2, 5, m.getDiscColor() | 0xFF000000);
            String t = m.getTitle();
            if (f.width(t) > 110) t = t.substring(0, 14) + "...";
            g.drawString(f, Component.literal(t), x + 30, iy + 4, 0xFFFFFFFF, false);
            g.drawString(f, Component.literal("§7" + m.getArtist()), x + 30, iy + 15, 0xFFB3B3B3, false);
            iy += HISTORY_ITEM_H;
        }
    }

    public void toggleVisibility() { overlayEnabled = !overlayEnabled; }
    public void toggleHistory()    { showHistory = !showHistory; }

    private void fillRoundRect(GuiGraphics g, int x, int y, int w, int h, int r, int color) {
        g.fill(x + r, y, x + w - r, y + h, color);
        g.fill(x, y + r, x + w, y + h - r, color);
        fillCircle(g, x + r, y + r, r, color);
        fillCircle(g, x + w - r, y + r, r, color);
        fillCircle(g, x + r, y + h - r, r, color);
        fillCircle(g, x + w - r, y + h - r, r, color);
    }

    private void drawRoundBorder(GuiGraphics g, int x, int y, int w, int h, int r, int color) {
        g.fill(x + r, y, x + w - r, y + 1, color);
        g.fill(x + r, y + h - 1, x + w - r, y + h, color);
        g.fill(x, y + r, x + 1, y + h - r, color);
        g.fill(x + w - 1, y + r, x + w, y + h - r, color);
    }

    private void fillCircle(GuiGraphics g, int cx, int cy, int r, int color) {
        for (int dy = -r; dy <= r; dy++)
            for (int dx = -r; dx <= r; dx++)
                if (dx * dx + dy * dy <= r * r) g.fill(cx + dx, cy + dy, cx + dx + 1, cy + dy + 1, color);
    }

    private int darken(int color, float factor) {
        int a = (color >> 24) & 0xFF;
        int r = (int)(((color >> 16) & 0xFF) * (1 - factor));
        int gr = (int)(((color >> 8) & 0xFF) * (1 - factor));
        int b = (int)((color & 0xFF) * (1 - factor));
        return (a << 24) | (r << 16) | (gr << 8) | b;
    }
}
