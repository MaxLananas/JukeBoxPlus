package com.example.jukeboxplus.gui;

import com.example.jukeboxplus.music.MusicDatabase;
import com.example.jukeboxplus.music.MusicInfo;
import com.example.jukeboxplus.music.MusicPlayer;
import com.example.jukeboxplus.music.MusicTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

public class MusicPlayerScreen extends Screen {

    private final MusicPlayer player;
    private final MusicTracker tracker;

    private static final String[] CATEGORIES = {"All", "Discs", "Ambient", "Nether", "End"};
    private int selectedCategory = 0;

    private List<MusicInfo> displayedMusic = new ArrayList<>();
    private int scrollOffset = 0;
    private static final int MAX_VISIBLE = 8;

    private int hoveredIndex = -1;
    private long openTime;

    private static final int BG_COLOR        = 0xFF121212;
    private static final int PANEL_COLOR     = 0xFF1E1E1E;
    private static final int CARD_COLOR      = 0xFF2A2A2A;
    private static final int CARD_HOVER      = 0xFF333333;
    private static final int CARD_PLAYING    = 0xFF1A3A1A;
    private static final int ACCENT_COLOR    = 0xFF1DB954;
    private static final int TEXT_PRIMARY    = 0xFFFFFFFF;
    private static final int TEXT_SECONDARY  = 0xFFB3B3B3;
    private static final int TEXT_MUTED      = 0xFF535353;
    private static final int DANGER_COLOR    = 0xFFE74C3C;

    private int panelX, panelY, panelW, panelH;

    private int prevBtnX, prevBtnY;
    private int playBtnX, playBtnY;
    private int nextBtnX, nextBtnY;
    private int closeBtnX, closeBtnY;
    private int shuffleBtnX, shuffleBtnY;
    private int repeatBtnX, repeatBtnY;

    private static final int BTN_SIZE = 20;

    public MusicPlayerScreen(MusicPlayer player, MusicTracker tracker) {
        super(Text.literal("Music Player"));
        this.player  = player;
        this.tracker = tracker;
        this.openTime = System.currentTimeMillis();
        loadMusicList();
    }

    private void loadMusicList() {
        List<MusicInfo> all = MusicDatabase.getAllMusic();
        displayedMusic = new ArrayList<>();
        for (MusicInfo m : all) {
            boolean include = switch (selectedCategory) {
                case 1  -> m.isDisc();
                case 2  -> m.isAmbient();
                case 3  -> m.isNether();
                case 4  -> m.isEnd();
                default -> true;
            };
            if (include) displayedMusic.add(m);
        }
        scrollOffset = 0;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        float elapsed = (System.currentTimeMillis() - openTime) / 1000f;
        float ease    = easeOutBack(Math.min(elapsed * 3f, 1f));

        panelW = Math.min(480, width  - 40);
        panelH = Math.min(580, height - 40);
        panelX = (width  - panelW) / 2;
        panelY = (int) ((height - panelH) / 2 + (1f - ease) * 30);

        context.fill(0, 0, width, height, 0xCC000000);

        drawRoundedRect(context, panelX, panelY, panelW, panelH, PANEL_COLOR);
        drawRoundedRect(context, panelX - 2, panelY - 2, panelW + 4, panelH + 4,
                withAlpha(0xFF000000, 80));

        hoveredIndex = -1;

        renderHeader(context, mouseX, mouseY);
        renderNowPlaying(context, mouseX, mouseY, delta);
        renderCategories(context, mouseX, mouseY);
        renderMusicList(context, mouseX, mouseY);
        renderFooter(context, mouseX, mouseY);

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderHeader(DrawContext context, int mouseX, int mouseY) {
        int headerH = 36;
        drawRoundedRect(context, panelX, panelY, panelW, headerH, darkenColor(PANEL_COLOR, 0.5f));

        float bounce = (float) Math.sin(System.currentTimeMillis() / 800.0) * 1.5f;
        String title = "# MUSIC PLAYER #";
        int tw = textRenderer.getWidth(title);
        context.drawTextWithShadow(textRenderer, Text.literal(title),
                panelX + panelW / 2 - tw / 2,
                (int) (panelY + 10 + bounce),
                ACCENT_COLOR);

        closeBtnX = panelX + panelW - BTN_SIZE - 6;
        closeBtnY = panelY + 8;
        boolean hoverClose = isInBounds(mouseX, mouseY, closeBtnX, closeBtnY, BTN_SIZE, BTN_SIZE);
        drawRoundedRect(context, closeBtnX, closeBtnY, BTN_SIZE, BTN_SIZE,
                hoverClose ? DANGER_COLOR : CARD_COLOR);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("X"),
                closeBtnX + BTN_SIZE / 2, closeBtnY + 6, TEXT_PRIMARY);
    }

    private void renderNowPlaying(DrawContext context, int mouseX, int mouseY, float delta) {
        int sectionY = panelY + 40;
        int sectionH = 130;
        drawRoundedRect(context, panelX + 6, sectionY, panelW - 12, sectionH, CARD_COLOR);

        MusicInfo current = player.getCurrentMusic();

        if (current == null) {
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("No music playing"),
                    panelX + panelW / 2, sectionY + sectionH / 2 - 4, TEXT_MUTED);
            renderPlaybackControls(context, mouseX, mouseY, sectionY + sectionH - 28);
            return;
        }

        int discX = panelX + 18;
        int discY = sectionY + 10;
        int discR  = 28;
        long now   = System.currentTimeMillis();
        int color  = current.getDiscColor();

        for (int r = discR; r > 0; r--) {
            float ratio = (float) r / discR;
            int c = withAlpha(darkenColor(color, ratio * 0.8f), (int)(ratio * 200));
            fillCircle(context, discX + discR, discY + discR, r, c);
        }

        if (player.isPlaying()) {
            double angle = (now / 800.0) % (Math.PI * 2);
            int holeR = 5;
            int holeX = (int)(discX + discR + Math.cos(angle) * 8);
            int holeY = (int)(discY + discR + Math.sin(angle) * 8);
            fillCircle(context, holeX, holeY, holeR, 0xFF000000);
        } else {
            fillCircle(context, discX + discR, discY + discR, 5, 0xFF000000);
        }

        int infoX = discX + discR * 2 + 12;
        int infoY = sectionY + 10;

        String statusSymbol = player.isPlaying() ? "~" : "II";
        context.drawTextWithShadow(textRenderer, Text.literal(statusSymbol),
                infoX, infoY, ACCENT_COLOR);

        String titleStr = current.getTitle();
        if (titleStr.length() > 22) titleStr = titleStr.substring(0, 19) + "...";
        context.drawTextWithShadow(textRenderer, Text.literal(titleStr),
                infoX + 14, infoY, TEXT_PRIMARY);

        String artist = current.getArtist();
        if (artist.length() > 26) artist = artist.substring(0, 23) + "...";
        context.drawTextWithShadow(textRenderer, Text.literal(artist),
                infoX, infoY + 12, TEXT_SECONDARY);

        String typeTag = "[" + current.getTypeLabel() + "]";
        context.drawTextWithShadow(textRenderer, Text.literal(typeTag),
                infoX, infoY + 24, TEXT_MUTED);

        int barY  = sectionY + sectionH - 42;
        int barX  = panelX + 10;
        int barW  = panelW - 20;
        int barH  = 4;
        float progress = current.getProgress();

        context.fill(barX, barY, barX + barW, barY + barH, TEXT_MUTED);
        context.fill(barX, barY, barX + (int)(barW * progress), barY + barH, ACCENT_COLOR);
        fillCircle(context, barX + (int)(barW * progress), barY + barH / 2, 4, ACCENT_COLOR);

        String elapsed = formatTime(current.getElapsedSeconds());
        String total   = formatTime(current.getTotalSeconds());
        context.drawTextWithShadow(textRenderer, Text.literal(elapsed),
                barX, barY + 8, TEXT_MUTED);
        int totalW = textRenderer.getWidth(total);
        context.drawTextWithShadow(textRenderer, Text.literal(total),
                barX + barW - totalW, barY + 8, TEXT_MUTED);

        renderPlaybackControls(context, mouseX, mouseY, sectionY + sectionH - 22);
        renderVolumeControl(context, mouseX, mouseY, sectionY + sectionH - 10);
    }

    private void renderPlaybackControls(DrawContext context, int mouseX, int mouseY, int y) {
        int centerX = panelX + panelW / 2;
        int spacing = 28;

        prevBtnX = centerX - spacing - BTN_SIZE / 2;
        prevBtnY = y - BTN_SIZE / 2;
        playBtnX = centerX - BTN_SIZE / 2;
        playBtnY = y - BTN_SIZE / 2;
        nextBtnX = centerX + spacing - BTN_SIZE / 2;
        nextBtnY = y - BTN_SIZE / 2;

        boolean hPrev = isInBounds(mouseX, mouseY, prevBtnX, prevBtnY, BTN_SIZE, BTN_SIZE);
        boolean hPlay = isInBounds(mouseX, mouseY, playBtnX, playBtnY, BTN_SIZE, BTN_SIZE);
        boolean hNext = isInBounds(mouseX, mouseY, nextBtnX, nextBtnY, BTN_SIZE, BTN_SIZE);

        drawRoundedRect(context, prevBtnX, prevBtnY, BTN_SIZE, BTN_SIZE,
                hPrev ? CARD_HOVER : CARD_COLOR);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("|<"),
                prevBtnX + BTN_SIZE / 2, prevBtnY + 6, TEXT_PRIMARY);

        drawRoundedRect(context, playBtnX, playBtnY, BTN_SIZE, BTN_SIZE,
                hPlay ? darkenColor(ACCENT_COLOR, 0.7f) : ACCENT_COLOR);
        String playLabel = player.isPlaying() ? "II" : ">";
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(playLabel),
                playBtnX + BTN_SIZE / 2, playBtnY + 6, TEXT_PRIMARY);

        drawRoundedRect(context, nextBtnX, nextBtnY, BTN_SIZE, BTN_SIZE,
                hNext ? CARD_HOVER : CARD_COLOR);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(">|"),
                nextBtnX + BTN_SIZE / 2, nextBtnY + 6, TEXT_PRIMARY);
    }

    private void renderVolumeControl(DrawContext context, int mouseX, int mouseY, int y) {
        int barX = panelX + 10;
        int barW = panelW - 20;
        int barH = 3;
        float vol = player.getVolume();

        context.fill(barX, y, barX + barW, y + barH, TEXT_MUTED);
        context.fill(barX, y, barX + (int)(barW * vol), y + barH, ACCENT_COLOR);

        String volStr = (int)(vol * 100) + "%";
        context.drawTextWithShadow(textRenderer, Text.literal("Vol: " + volStr),
                barX, y + 6, TEXT_MUTED);
    }

    private void renderCategories(DrawContext context, int mouseX, int mouseY) {
        int catY  = panelY + 178;
        int catH  = 22;
        int tabW  = (panelW - 12) / CATEGORIES.length;

        for (int i = 0; i < CATEGORIES.length; i++) {
            int tabX   = panelX + 6 + i * tabW;
            boolean sel   = (i == selectedCategory);
            boolean hover = isInBounds(mouseX, mouseY, tabX, catY, tabW - 2, catH);

            int bgColor = sel ? ACCENT_COLOR : (hover ? CARD_HOVER : CARD_COLOR);
            drawRoundedRect(context, tabX, catY, tabW - 2, catH, bgColor);

            int tw = textRenderer.getWidth(CATEGORIES[i]);
            context.drawTextWithShadow(textRenderer, Text.literal(CATEGORIES[i]),
                    tabX + (tabW - 2) / 2 - tw / 2, catY + 7,
                    sel ? TEXT_PRIMARY : TEXT_SECONDARY);
        }
    }

    private void renderMusicList(DrawContext context, int mouseX, int mouseY) {
        int listY  = panelY + 204;
        int listH  = panelH - 204 - 36;
        int itemH  = 24;

        drawRoundedRect(context, panelX + 6, listY, panelW - 12, listH, darkenColor(PANEL_COLOR, 0.6f));

        int visible = Math.min(MAX_VISIBLE, displayedMusic.size() - scrollOffset);

        for (int i = 0; i < visible; i++) {
            int idx   = i + scrollOffset;
            MusicInfo m = displayedMusic.get(idx);
            int itemY = listY + i * itemH;

            boolean hover   = isInBounds(mouseX, mouseY, panelX + 8, itemY, panelW - 16, itemH);
            boolean playing = player.getCurrentMusic() == m;

            if (hover) hoveredIndex = idx;

            int bg = playing ? CARD_PLAYING : (hover ? CARD_HOVER : 0x00000000);
            if (bg != 0x00000000)
                context.fill(panelX + 8, itemY, panelX + panelW - 8, itemY + itemH, bg);

            int discColor = m.getDiscColor();
            fillCircle(context, panelX + 18, itemY + itemH / 2, 6, discColor);

            if (playing) {
                context.drawTextWithShadow(textRenderer, Text.literal(">"),
                        panelX + 10, itemY + 8, ACCENT_COLOR);
            }

            String t = m.getTitle();
            if (t.length() > 20) t = t.substring(0, 17) + "...";
            context.drawTextWithShadow(textRenderer, Text.literal(t),
                    panelX + 28, itemY + 4, playing ? ACCENT_COLOR : TEXT_PRIMARY);

            String a = m.getArtist();
            if (a.length() > 18) a = a.substring(0, 15) + "...";
            context.drawTextWithShadow(textRenderer, Text.literal(a),
                    panelX + 28, itemY + 14, TEXT_SECONDARY);

            String dur = formatTime(m.getTotalSeconds());
            int durW = textRenderer.getWidth(dur);
            context.drawTextWithShadow(textRenderer, Text.literal(dur),
                    panelX + panelW - durW - 12, itemY + 8, TEXT_MUTED);
        }

        if (displayedMusic.size() > MAX_VISIBLE) {
            int scrollBarH  = listH;
            int thumbH      = Math.max(20, scrollBarH * MAX_VISIBLE / displayedMusic.size());
            int thumbY      = listY + scrollOffset * (scrollBarH - thumbH) / (displayedMusic.size() - MAX_VISIBLE);
            int scrollBarX  = panelX + panelW - 10;

            context.fill(scrollBarX, listY, scrollBarX + 4, listY + scrollBarH, CARD_COLOR);
            context.fill(scrollBarX, thumbY, scrollBarX + 4, thumbY + thumbH, TEXT_SECONDARY);
        }
    }

    private void renderFooter(DrawContext context, int mouseX, int mouseY) {
        int footerY = panelY + panelH - 32;
        drawRoundedRect(context, panelX, footerY, panelW, 32, darkenColor(PANEL_COLOR, 0.5f));

        shuffleBtnX = panelX + 10;
        shuffleBtnY = footerY + 6;
        boolean hShuffle = isInBounds(mouseX, mouseY, shuffleBtnX, shuffleBtnY, 40, 18);
        boolean shuffleOn = player.isShuffleEnabled();
        drawRoundedRect(context, shuffleBtnX, shuffleBtnY, 40, 18,
                shuffleOn ? ACCENT_COLOR : (hShuffle ? CARD_HOVER : CARD_COLOR));
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("[S]"),
                shuffleBtnX + 20, shuffleBtnY + 5,
                shuffleOn ? TEXT_PRIMARY : TEXT_SECONDARY);

        repeatBtnX = panelX + 58;
        repeatBtnY = footerY + 6;
        boolean hRepeat = isInBounds(mouseX, mouseY, repeatBtnX, repeatBtnY, 40, 18);
        boolean repeatOn = player.isRepeatEnabled();
        drawRoundedRect(context, repeatBtnX, repeatBtnY, 40, 18,
                repeatOn ? ACCENT_COLOR : (hRepeat ? CARD_HOVER : CARD_COLOR));
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("[R]"),
                repeatBtnX + 20, repeatBtnY + 5,
                repeatOn ? TEXT_PRIMARY : TEXT_SECONDARY);

        String credit = "Made by maxlananas";
        int creditW = textRenderer.getWidth(credit);
        context.drawTextWithShadow(textRenderer, Text.literal(credit),
                panelX + panelW - creditW - 8, footerY + 10, TEXT_MUTED);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int mx = (int) mouseX;
        int my = (int) mouseY;

        if (isInBounds(mx, my, closeBtnX, closeBtnY, BTN_SIZE, BTN_SIZE)) {
            playClickSound();
            close();
            return true;
        }

        if (isInBounds(mx, my, prevBtnX, prevBtnY, BTN_SIZE, BTN_SIZE)) {
            playClickSound();
            player.previous();
            return true;
        }

        if (isInBounds(mx, my, playBtnX, playBtnY, BTN_SIZE, BTN_SIZE)) {
            playClickSound();
            if (player.getCurrentMusic() == null && !displayedMusic.isEmpty()) {
                player.play(displayedMusic.get(0));
            } else {
                player.togglePlayPause();
            }
            return true;
        }

        if (isInBounds(mx, my, nextBtnX, nextBtnY, BTN_SIZE, BTN_SIZE)) {
            playClickSound();
            player.next();
            return true;
        }

        if (isInBounds(mx, my, shuffleBtnX, shuffleBtnY, 40, 18)) {
            playClickSound();
            player.toggleShuffle();
            return true;
        }

        if (isInBounds(mx, my, repeatBtnX, repeatBtnY, 40, 18)) {
            playClickSound();
            player.toggleRepeat();
            return true;
        }

        int tabW  = (panelW - 12) / CATEGORIES.length;
        int catY  = panelY + 178;
        for (int i = 0; i < CATEGORIES.length; i++) {
            int tabX = panelX + 6 + i * tabW;
            if (isInBounds(mx, my, tabX, catY, tabW - 2, 22)) {
                playClickSound();
                selectedCategory = i;
                loadMusicList();
                return true;
            }
        }

        int listY = panelY + 204;
        int itemH = 24;
        for (int i = 0; i < Math.min(MAX_VISIBLE, displayedMusic.size() - scrollOffset); i++) {
            int idx   = i + scrollOffset;
            int itemY = listY + i * itemH;
            if (isInBounds(mx, my, panelX + 8, itemY, panelW - 16, itemH)) {
                playClickSound();
                player.play(displayedMusic.get(idx));
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int maxScroll = Math.max(0, displayedMusic.size() - MAX_VISIBLE);
        scrollOffset  = MathHelper.clamp(scrollOffset - (int) verticalAmount, 0, maxScroll);
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private void playClickSound() {
        MinecraftClient.getInstance().getSoundManager().play(
                PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
    }

    private boolean isInBounds(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private void drawRoundedRect(DrawContext context, int x, int y, int w, int h, int color) {
        context.fill(x + 2, y,     x + w - 2, y + h,     color);
        context.fill(x,     y + 2, x + w,     y + h - 2, color);
    }

    private void fillCircle(DrawContext context, int cx, int cy, int r, int color) {
        for (int dy = -r; dy <= r; dy++) {
            for (int dx = -r; dx <= r; dx++) {
                if (dx * dx + dy * dy <= r * r) {
                    context.fill(cx + dx, cy + dy, cx + dx + 1, cy + dy + 1, color);
                }
            }
        }
    }

    private int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | (MathHelper.clamp(alpha, 0, 255) << 24);
    }

    private int darkenColor(int color, float factor) {
        int r = (int)(((color >> 16) & 0xFF) * factor);
        int g = (int)(((color >> 8)  & 0xFF) * factor);
        int b = (int)(( color        & 0xFF) * factor);
        return (color & 0xFF000000) | (r << 16) | (g << 8) | b;
    }

    private float easeOutBack(float t) {
        float c1 = 1.70158f;
        float c3 = c1 + 1f;
        return 1f + c3 * (float) Math.pow(t - 1, 3) + c1 * (float) Math.pow(t - 1, 2);
    }

    private String formatTime(int totalSeconds) {
        int m = totalSeconds / 60;
        int s = totalSeconds % 60;
        return String.format("%02d:%02d", m, s);
    }
}
