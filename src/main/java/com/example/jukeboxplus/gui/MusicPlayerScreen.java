package com.example.jukeboxplus.gui;

import com.example.jukeboxplus.music.MusicDatabase;
import com.example.jukeboxplus.music.MusicInfo;
import com.example.jukeboxplus.music.MusicPlayer;
import com.example.jukeboxplus.music.MusicTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;

import java.util.ArrayList;
import java.util.List;

public class MusicPlayerScreen extends Screen {

    private static final String[] CATEGORIES = {"All", "Discs", "Ambient", "Nether", "End"};
    private static final int MAX_VISIBLE = 10;
    private static final int PANEL_W = 480;
    private static final int PANEL_H = 540;

    private final MusicPlayer player;
    private final MusicTracker tracker;
    private final List<MusicInfo> displayedMusic = new ArrayList<>();

    private int selectedCategory = 0;
    private int scrollOffset = 0;
    private boolean needsRebuild = true;
    private int panelX, panelY;

    public MusicPlayerScreen(MusicPlayer player, MusicTracker tracker) {
        super(Component.literal("Music Player"));
        this.player = player;
        this.tracker = tracker;
    }

    @Override
    protected void init() {
        panelX = (width - PANEL_W) / 2;
        panelY = (height - PANEL_H) / 2;
        needsRebuild = true;
    }

    @Override
    public void tick() {
        if (needsRebuild) { needsRebuild = false; rebuildUI(); }
    }

    private void loadMusicList() {
        displayedMusic.clear();
        for (MusicInfo m : MusicDatabase.getAllMusic()) {
            boolean include = switch (selectedCategory) {
                case 1 -> m.getType() == MusicInfo.MusicType.DISC;
                case 2 -> m.getType() == MusicInfo.MusicType.AMBIENT || m.getType() == MusicInfo.MusicType.CREATIVE;
                case 3 -> m.getType() == MusicInfo.MusicType.NETHER;
                case 4 -> m.getType() == MusicInfo.MusicType.END;
                default -> true;
            };
            if (include) displayedMusic.add(m);
        }
        scrollOffset = 0;
    }

    private void rebuildUI() {
        clearWidgets();
        loadMusicList();

        int pad = 12;
        int closeX = panelX + PANEL_W - 28;
        int closeY = panelY + 6;
        addRenderableWidget(new MusicButton(closeX, closeY, 22, 22, "X",
                () -> { if (minecraft != null) minecraft.setScreen(null); },
                0xFF3A1A1A, 0xFF5A2020, 0xFFE74C3C, 0xFFFFFFFF));

        int controlY = panelY + 120;
        int btnSize = 28;
        int gap = 8;
        int totalW = btnSize * 3 + gap * 2;
        int startX = panelX + (PANEL_W - totalW) / 2;

        addRenderableWidget(new MusicButton(startX, controlY, btnSize, btnSize, "|<",
                () -> { player.previous(); playClick(); }));
        addRenderableWidget(new MusicButton(startX + btnSize + gap, controlY, btnSize, btnSize,
                player.isPlaying() ? "||" : ">",
                () -> {
                    if (player.getCurrentMusic() == null && !displayedMusic.isEmpty()) player.play(displayedMusic.get(0));
                    else player.togglePlayPause();
                    playClick();
                    needsRebuild = true;
                },
                0xFF1DB954, 0xFF1ED760, 0xFFFFFFFF, 0xFFFFFFFF));
        addRenderableWidget(new MusicButton(startX + (btnSize + gap) * 2, controlY, btnSize, btnSize, ">|",
                () -> { player.next(); playClick(); }));

        int volY = controlY + btnSize + 8;
        int volBtnW = 40;
        int volStartX = panelX + PANEL_W / 2 - volBtnW - 4;
        addRenderableWidget(new MusicButton(volStartX, volY, volBtnW, 16, "Vol-",
                () -> { player.adjustVolume(-0.1f); playClick(); }));
        addRenderableWidget(new MusicButton(volStartX + volBtnW + 8, volY, volBtnW, 16, "Vol+",
                () -> { player.adjustVolume(0.1f); playClick(); }));

        int catY = panelY + 180;
        int catW = (PANEL_W - pad * 2 - 4 * 4) / 5;
        for (int i = 0; i < CATEGORIES.length; i++) {
            final int cat = i;
            boolean sel = (i == selectedCategory);
            addRenderableWidget(new MusicButton(
                    panelX + pad + i * (catW + 4), catY, catW, 20, CATEGORIES[i],
                    () -> { selectedCategory = cat; needsRebuild = true; playClick(); },
                    sel ? 0xFF1DB954 : 0xFF282828,
                    sel ? 0xFF1ED760 : 0xFF333333,
                    sel ? 0xFFFFFFFF : 0xFFB3B3B3,
                    0xFFFFFFFF));
        }

        int listY = panelY + 208;
        int itemH = 26;
        int listW = PANEL_W - pad * 2 - 8;
        int start = scrollOffset;
        int end = Math.min(displayedMusic.size(), scrollOffset + MAX_VISIBLE);

        for (int i = start; i < end; i++) {
            final MusicInfo music = displayedMusic.get(i);
            int itemY = listY + (i - scrollOffset) * itemH;
            boolean playing = player.getCurrentMusic() != null && player.getCurrentMusic().getId().equals(music.getId());
            String label = music.getTitle() + " - " + music.getArtist();

            addRenderableWidget(new MusicButton(panelX + pad, itemY, listW, itemH - 2, label,
                    () -> { player.play(music); playClick(); },
                    playing ? 0xFF1A3A1A : 0xFF1E1E1E,
                    playing ? 0xFF1A4A1A : 0xFF333333,
                    playing ? 0xFF1DB954 : 0xFFFFFFFF,
                    0xFFFFFFFF));
        }

        int footerY = panelY + PANEL_H - 36;
        addRenderableWidget(new MusicButton(panelX + pad, footerY, 50, 18,
                player.isShuffle() ? "[S] ON" : "[S]",
                () -> { player.toggleShuffle(); playClick(); needsRebuild = true; },
                player.isShuffle() ? 0xFF1DB954 : 0xFF282828,
                player.isShuffle() ? 0xFF1ED760 : 0xFF333333,
                player.isShuffle() ? 0xFFFFFFFF : 0xFFB3B3B3,
                0xFFFFFFFF));

        addRenderableWidget(new MusicButton(panelX + pad + 56, footerY, 50, 18,
                player.isRepeat() ? "[R] ON" : "[R]",
                () -> { player.toggleRepeat(); playClick(); needsRebuild = true; },
                player.isRepeat() ? 0xFF1DB954 : 0xFF282828,
                player.isRepeat() ? 0xFF1ED760 : 0xFF333333,
                player.isRepeat() ? 0xFFFFFFFF : 0xFFB3B3B3,
                0xFFFFFFFF));
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        g.fill(0, 0, width, height, 0xCC000000);

        g.fill(panelX - 2, panelY - 2, panelX + PANEL_W + 2, panelY + PANEL_H + 2, 0xFF1DB954);
        g.fill(panelX, panelY, panelX + PANEL_W, panelY + PANEL_H, 0xFF121212);
        drawBorder(g, panelX, panelY, PANEL_W, PANEL_H, 0xFF3E3E3E);
        g.fill(panelX + 1, panelY, panelX + PANEL_W - 1, panelY + 2, 0xFF1DB954);

        Font f = font;
        int pad = 12;

        float bounce = (float) Math.sin(System.currentTimeMillis() / 800.0) * 1.5f;
        String title = "♫ MUSIC PLAYER";
        int tw = f.width(title);
        g.drawCenteredString(f, Component.literal(title), panelX + PANEL_W / 2, (int)(panelY + 10 + bounce), 0xFF1DB954);

        int npY = panelY + 36;
        int npH = 80;
        g.fill(panelX + pad, npY, panelX + PANEL_W - pad, npY + npH, 0xFF1E1E1E);
        drawBorder(g, panelX + pad, npY, PANEL_W - pad * 2, npH, 0xFF3E3E3E);

        MusicInfo current = player.getCurrentMusic();

        if (current == null) {
            g.drawCenteredString(f, Component.literal("§7No music playing"),
                    panelX + PANEL_W / 2, npY + npH / 2 - 4, 0xFF535353);
        } else {
            int discX = panelX + pad + 20;
            int discY = npY + 20;
            int discR = 24;
            int color = current.getDiscColor() | 0xFF000000;
            fillCircle(g, discX, discY, discR, color);
            fillCircle(g, discX, discY, discR * 2 / 3, darken(color, 0.3f));
            fillCircle(g, discX, discY, 5, 0xFF151515);
            fillCircle(g, discX, discY, 2, color);

            if (player.isPlaying()) {
                double angle = (System.currentTimeMillis() / 800.0) % (Math.PI * 2);
                fillCircle(g, (int)(discX + Math.cos(angle) * 8), (int)(discY + Math.sin(angle) * 8), 4, 0xFF000000);
            }

            int infoX = discX + discR + 16;
            g.drawString(f, Component.literal(player.isPlaying() ? "~" : "||"), infoX, npY + 8, 0xFF1DB954, false);
            String songTitle = current.getTitle();
            if (f.width(songTitle) > PANEL_W - 120) songTitle = songTitle.substring(0, 20) + "...";
            g.drawString(f, Component.literal(songTitle), infoX + 14, npY + 8, 0xFFFFFFFF, true);
            g.drawString(f, Component.literal(current.getArtist()), infoX, npY + 20, 0xFFB3B3B3, true);
            g.drawString(f, Component.literal("[" + current.getType().displayName + "]"), infoX, npY + 32, 0xFF535353, false);

            int barY = npY + npH - 20;
            int barX = panelX + pad + 4;
            int barW = PANEL_W - pad * 2 - 8;
            float progress = current.getProgress();
            g.fill(barX, barY, barX + barW, barY + 3, 0xFF404040);
            g.fill(barX, barY, barX + (int)(barW * progress), barY + 3, 0xFF1DB954);
            fillCircle(g, barX + (int)(barW * progress), barY + 1, 3, 0xFF1DB954);
            String time = current.getFormattedTime() + " / " + current.getFormattedDuration();
            g.drawCenteredString(f, Component.literal("§8" + time), panelX + PANEL_W / 2, barY + 6, 0xFF535353);
        }

        g.fill(panelX + pad, npY + npH + 4, panelX + PANEL_W - pad, npY + npH + 5, 0xFF3E3E3E);

        int volY = panelY + 156;
        float vol = player.getVolume();
        int volBarX = panelX + PANEL_W / 2 - 60;
        int volBarW = 120;
        g.fill(volBarX, volY, volBarX + volBarW, volY + 3, 0xFF404040);
        g.fill(volBarX, volY, volBarX + (int)(volBarW * vol), volY + 3, 0xFF1DB954);
        String volStr = "Vol: " + (int)(vol * 100) + "%";
        g.drawCenteredString(f, Component.literal("§8" + volStr), panelX + PANEL_W / 2, volY + 6, 0xFF535353);

        int catY = panelY + 180;
        g.fill(panelX + pad, catY + 22, panelX + PANEL_W - pad, catY + 23, 0xFF3E3E3E);

        int listY = panelY + 208;
        int listH = MAX_VISIBLE * 26;
        g.fill(panelX + pad, listY, panelX + PANEL_W - pad, listY + listH, 0xFF1E1E1E);
        drawBorder(g, panelX + pad, listY, PANEL_W - pad * 2, listH, 0xFF2E2E2E);

        super.render(g, mouseX, mouseY, delta);

        int listW = PANEL_W - pad * 2 - 8;
        int start = scrollOffset;
        int end = Math.min(displayedMusic.size(), scrollOffset + MAX_VISIBLE);
        for (int i = start; i < end; i++) {
            MusicInfo m = displayedMusic.get(i);
            int itemY = listY + (i - start) * 26;
            fillCircle(g, panelX + pad + 10, itemY + 11, 5, m.getDiscColor() | 0xFF000000);
            String dur = m.getFormattedDuration();
            g.drawString(f, Component.literal("§8" + dur), panelX + pad + listW - f.width(dur) - 4, itemY + 5, 0xFF535353, false);
        }

        if (displayedMusic.size() > MAX_VISIBLE) {
            int sbX = panelX + PANEL_W - pad - 6;
            int sbH = listH;
            int thumbH = Math.max(20, sbH * MAX_VISIBLE / displayedMusic.size());
            int thumbY = listY + scrollOffset * (sbH - thumbH) / Math.max(1, displayedMusic.size() - MAX_VISIBLE);
            g.fill(sbX, listY, sbX + 4, listY + sbH, 0xFF282828);
            g.fill(sbX, thumbY, sbX + 4, thumbY + thumbH, 0xFFB3B3B3);
        }

        int footerY = panelY + PANEL_H - 36;
        g.fill(panelX, footerY, panelX + PANEL_W, footerY + 1, 0xFF3E3E3E);
        String credit = "Made by maxlananas";
        g.drawString(f, Component.literal("§8" + credit), panelX + PANEL_W - f.width(credit) - pad, footerY + 6, 0xFF535353, false);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (displayedMusic.size() <= MAX_VISIBLE) return false;
        int maxScroll = displayedMusic.size() - MAX_VISIBLE;
        int newOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) Math.signum(verticalAmount)));
        if (newOffset != scrollOffset) { scrollOffset = newOffset; needsRebuild = true; }
        return true;
    }

    private void drawBorder(GuiGraphics g, int x, int y, int w, int h, int c) {
        g.fill(x, y, x + w, y + 1, c);
        g.fill(x, y + h - 1, x + w, y + h, c);
        g.fill(x, y, x + 1, y + h, c);
        g.fill(x + w - 1, y, x + w, y + h, c);
    }

    private void fillCircle(GuiGraphics g, int cx, int cy, int r, int color) {
        for (int dy = -r; dy <= r; dy++)
            for (int dx = -r; dx <= r; dx++)
                if (dx * dx + dy * dy <= r * r) g.fill(cx + dx, cy + dy, cx + dx + 1, cy + dy + 1, color);
    }

    private int darken(int color, float factor) {
        int r = (int)(((color >> 16) & 0xFF) * factor);
        int gr = (int)(((color >> 8) & 0xFF) * factor);
        int b = (int)((color & 0xFF) * factor);
        return (color & 0xFF000000) | (r << 16) | (gr << 8) | b;
    }

    private void playClick() {
        Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
    }

    @Override
    public void onClose() { if (minecraft != null) minecraft.setScreen(null); }

    @Override
    public boolean isPauseScreen() { return false; }
}
