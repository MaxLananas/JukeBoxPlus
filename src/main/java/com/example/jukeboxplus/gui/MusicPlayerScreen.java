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
    
    // Catégories
    private static final String[] CATEGORIES = {"All", "Discs", "Ambient", "Nether", "End"};
    private int selectedCategory = 0;
    
    // Liste de musiques
    private List<MusicInfo> displayedMusic = new ArrayList<>();
    private int scrollOffset = 0;
    private int maxVisible = 8;
    
    // Hovered
    private int hoveredIndex = -1;
    
    // Animation
    private long openTime;
    
    // Couleurs
    private static final int BG_COLOR = 0xF0101018;
    private static final int PANEL_COLOR = 0xF0181820;
    private static final int ACCENT_COLOR = 0xFF1DB954;
    private static final int TEXT_PRIMARY = 0xFFFFFFFF;
    private static final int TEXT_SECONDARY = 0xFFB3B3B3;
    private static final int HOVER_COLOR = 0xFF2A2A35;
    private static final int BUTTON_COLOR = 0xFF252530;
    
    // Positions des boutons
    private int prevBtnX, prevBtnY;
    private int playBtnX, playBtnY;
    private int nextBtnX, nextBtnY;
    private int shuffleBtnX, shuffleBtnY;
    private int repeatBtnX, repeatBtnY;
    private int closeBtnX, closeBtnY;
    private static final int BTN_SIZE = 24;
    private static final int FOOTER_BTN_WIDTH = 70;
    private static final int FOOTER_BTN_HEIGHT = 22;
    
    public MusicPlayerScreen(MusicPlayer player, MusicTracker tracker) {
        super(Text.literal("Music Player"));
        this.player = player;
        this.tracker = tracker;
        this.openTime = System.currentTimeMillis();
        
        loadMusicList();
    }
    
    private void loadMusicList() {
        displayedMusic.clear();
        
        List<MusicInfo> allMusic = MusicDatabase.getAllMusic();
        
        for (MusicInfo music : allMusic) {
            boolean include = switch (selectedCategory) {
                case 0 -> true;
                case 1 -> music.getType() == MusicInfo.MusicType.DISC;
                case 2 -> music.getType() == MusicInfo.MusicType.AMBIENT || 
                          music.getType() == MusicInfo.MusicType.CREATIVE;
                case 3 -> music.getType() == MusicInfo.MusicType.NETHER;
                case 4 -> music.getType() == MusicInfo.MusicType.END;
                default -> true;
            };
            
            if (include) {
                displayedMusic.add(music);
            }
        }
        
        scrollOffset = 0;
    }
    
    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Pas de blur
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        float timeSinceOpen = (System.currentTimeMillis() - openTime) / 1000f;
        float ease = easeOutBack(Math.min(1f, timeSinceOpen * 3f));
        
        // Fond
        context.fill(0, 0, this.width, this.height, 0xCC000000);
        
        // Panneau principal
        int panelWidth = Math.min(480, this.width - 40);
        int panelHeight = this.height - 60;
        int panelX = (this.width - panelWidth) / 2;
        int panelY = 30;
        
        int animatedY = (int) (panelY + (1 - ease) * 40);
        int alpha = (int) (ease * 255);
        
        // Ombre
        drawRoundedRect(context, panelX + 4, animatedY + 4, panelWidth, panelHeight, 16, 0x60000000);
        
        // Panneau
        drawRoundedRect(context, panelX, animatedY, panelWidth, panelHeight, 16, withAlpha(PANEL_COLOR, alpha));
        drawRoundedBorder(context, panelX, animatedY, panelWidth, panelHeight, 16, withAlpha(ACCENT_COLOR, alpha / 3));
        
        // Header
        renderHeader(context, panelX, animatedY, panelWidth, mouseX, mouseY);
        
        // Now Playing
        renderNowPlaying(context, panelX, animatedY + 45, panelWidth, mouseX, mouseY);
        
        // Catégories
        renderCategories(context, panelX, animatedY + 145, panelWidth, mouseX, mouseY);
        
        // Liste des musiques
        renderMusicList(context, panelX, animatedY + 180, panelWidth, panelHeight - 240, mouseX, mouseY);
        
        // Footer
        renderFooter(context, panelX, animatedY + panelHeight - 50, panelWidth, mouseX, mouseY);
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    private void renderHeader(DrawContext context, int x, int y, int width, int mouseX, int mouseY) {
        int centerX = x + width / 2;
        
        float bounce = (float) Math.sin(System.currentTimeMillis() / 400.0) * 2;
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("# MUSIC PLAYER #"), centerX, y + 15 + (int) bounce, ACCENT_COLOR);
        
        // Bouton fermer
        closeBtnX = x + width - 30;
        closeBtnY = y + 10;
        boolean closeHovered = isInBounds(mouseX, mouseY, closeBtnX, closeBtnY, 20, 20);
        
        drawRoundedRect(context, closeBtnX, closeBtnY, 20, 20, 6, closeHovered ? 0xFFFF4444 : 0xFF444444);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("X"), closeBtnX + 10, closeBtnY + 6, TEXT_PRIMARY);
    }
    
    private void renderNowPlaying(DrawContext context, int x, int y, int width, int mouseX, int mouseY) {
        MusicInfo current = player.getCurrentMusic();
        
        // Fond
        drawRoundedRect(context, x + 15, y, width - 30, 90, 10, 0xFF1A1A24);
        
        if (current == null) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("No music playing"), x + width / 2 - 40, y + 40, TEXT_SECONDARY);
            renderPlaybackControls(context, x + width - 115, y + 30, mouseX, mouseY);
            return;
        }
        
        // Disque animé
        int discX = x + 30;
        int discY = y + 15;
        int discSize = 55;
        fillCircle(context, discX + discSize/2, discY + discSize/2, discSize/2, current.getDiscColor() | 0xFF000000);
        fillCircle(context, discX + discSize/2, discY + discSize/2, discSize/3, darkenColor(current.getDiscColor() | 0xFF000000, 0.3f));
        fillCircle(context, discX + discSize/2, discY + discSize/2, 8, 0xFF151515);
        fillCircle(context, discX + discSize/2, discY + discSize/2, 3, current.getDiscColor() | 0xFF000000);
        
        if (player.isPlaying()) {
            int noteOffset = (int) (Math.sin(System.currentTimeMillis() / 200.0) * 2);
            context.drawText(textRenderer, Text.literal("~"), discX + discSize/2 - 3, discY + discSize/2 - 4 + noteOffset, TEXT_PRIMARY, true);
        } else if (player.isPaused()) {
            context.drawText(textRenderer, Text.literal("II"), discX + discSize/2 - 4, discY + discSize/2 - 4, TEXT_PRIMARY, false);
        }
        
        // Infos
        int infoX = x + 100;
        int infoWidth = width - 240;
        
        // Titre
        String title = current.getTitle();
        if (textRenderer.getWidth(title) > infoWidth) {
            title = title.substring(0, Math.min(title.length(), 14)) + "...";
        }
        context.drawText(textRenderer, Text.literal(title), infoX, y + 10, TEXT_PRIMARY, true);
        
        // Artiste
        context.drawText(textRenderer, Text.literal(current.getArtist()), infoX, y + 24, TEXT_SECONDARY, true);
        
        // Type
        context.drawText(textRenderer, Text.literal("[" + current.getType().displayName + "]"), infoX, y + 38, 0xFF888888, false);
        
        // Barre de progression
        int progressX = infoX;
        int progressY = y + 55;
        int progressWidth = infoWidth - 10;
        
        drawRoundedRect(context, progressX, progressY, progressWidth, 5, 2, 0xFF404040);
        int filled = (int) (progressWidth * current.getProgress());
        if (filled > 0) {
            drawRoundedRect(context, progressX, progressY, filled, 5, 2, current.getDiscColor() | 0xFF000000);
        }
        
        // Temps
        String timeText = current.getFormattedTime() + " / " + current.getFormattedDuration();
        context.drawText(textRenderer, Text.literal(timeText), progressX, progressY + 10, TEXT_SECONDARY, false);
        
        // Contrôles
        renderPlaybackControls(context, x + width - 115, y + 10, mouseX, mouseY);
        
        // Volume
        renderVolumeControl(context, x + width - 115, y + 60);
    }
    
    private void renderPlaybackControls(DrawContext context, int x, int y, int mouseX, int mouseY) {
        int btnSpacing = 28;
        
        // Previous
        prevBtnX = x;
        prevBtnY = y;
        boolean prevHovered = isInBounds(mouseX, mouseY, prevBtnX, prevBtnY, BTN_SIZE, BTN_SIZE);
        drawRoundedRect(context, prevBtnX, prevBtnY, BTN_SIZE, BTN_SIZE, 6, prevHovered ? HOVER_COLOR : BUTTON_COLOR);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("|<"), prevBtnX + BTN_SIZE/2, prevBtnY + 8, TEXT_PRIMARY);
        
        // Play/Pause
        playBtnX = x + btnSpacing;
        playBtnY = y;
        boolean playHovered = isInBounds(mouseX, mouseY, playBtnX, playBtnY, BTN_SIZE, BTN_SIZE);
        drawRoundedRect(context, playBtnX, playBtnY, BTN_SIZE, BTN_SIZE, 6, playHovered ? ACCENT_COLOR : BUTTON_COLOR);
        String playIcon = player.isPlaying() ? "II" : ">";
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(playIcon), playBtnX + BTN_SIZE/2, playBtnY + 8, TEXT_PRIMARY);
        
        // Next
        nextBtnX = x + btnSpacing * 2;
        nextBtnY = y;
        boolean nextHovered = isInBounds(mouseX, mouseY, nextBtnX, nextBtnY, BTN_SIZE, BTN_SIZE);
        drawRoundedRect(context, nextBtnX, nextBtnY, BTN_SIZE, BTN_SIZE, 6, nextHovered ? HOVER_COLOR : BUTTON_COLOR);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(">|"), nextBtnX + BTN_SIZE/2, nextBtnY + 8, TEXT_PRIMARY);
    }
    
    private void renderVolumeControl(DrawContext context, int x, int y) {
        context.drawText(textRenderer, Text.literal("Vol:"), x, y, TEXT_SECONDARY, false);
        
        int barX = x + 25;
        int barWidth = 45;
        int barHeight = 5;
        
        drawRoundedRect(context, barX, y + 3, barWidth, barHeight, 2, 0xFF404040);
        
        int filledWidth = (int) (barWidth * player.getVolume());
        if (filledWidth > 0) {
            drawRoundedRect(context, barX, y + 3, filledWidth, barHeight, 2, ACCENT_COLOR);
        }
        
        int vol = (int) (player.getVolume() * 100);
        context.drawText(textRenderer, Text.literal(vol + "%"), barX + barWidth + 4, y, TEXT_PRIMARY, false);
    }
    
    private void renderCategories(DrawContext context, int x, int y, int width, int mouseX, int mouseY) {
        int tabWidth = (width - 30) / CATEGORIES.length;
        int tabX = x + 15;
        
        for (int i = 0; i < CATEGORIES.length; i++) {
            boolean selected = i == selectedCategory;
            boolean hovered = isInBounds(mouseX, mouseY, tabX, y, tabWidth - 4, 22);
            
            int bgColor = selected ? ACCENT_COLOR : (hovered ? HOVER_COLOR : 0xFF1A1A24);
            drawRoundedRect(context, tabX, y, tabWidth - 4, 22, 6, bgColor);
            
            int textColor = selected ? TEXT_PRIMARY : TEXT_SECONDARY;
            String text = CATEGORIES[i];
            int textWidth = textRenderer.getWidth(text);
            context.drawText(textRenderer, Text.literal(text), tabX + (tabWidth - 4) / 2 - textWidth / 2, y + 7, textColor, false);
            
            tabX += tabWidth;
        }
    }
    
    private void renderMusicList(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY) {
        drawRoundedRect(context, x + 15, y, width - 30, height, 10, 0xFF0D0D12);
        
        if (displayedMusic.isEmpty()) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("No music in this category"), x + width / 2, y + height / 2, TEXT_SECONDARY);
            return;
        }
        
        int itemHeight = 36;
        maxVisible = height / itemHeight;
        int listX = x + 20;
        int listWidth = width - 50;
        
        // Scrollbar
        if (displayedMusic.size() > maxVisible) {
            int scrollbarHeight = height - 10;
            int thumbHeight = Math.max(30, scrollbarHeight * maxVisible / displayedMusic.size());
            int maxScroll = displayedMusic.size() - maxVisible;
            int thumbY = y + 5 + (int) ((scrollbarHeight - thumbHeight) * ((float) scrollOffset / Math.max(1, maxScroll)));
            
            drawRoundedRect(context, x + width - 22, y + 5, 6, scrollbarHeight, 3, 0xFF252530);
            drawRoundedRect(context, x + width - 22, thumbY, 6, thumbHeight, 3, ACCENT_COLOR);
        }
        
        hoveredIndex = -1;
        int itemY = y + 5;
        
        for (int i = scrollOffset; i < Math.min(scrollOffset + maxVisible, displayedMusic.size()); i++) {
            MusicInfo music = displayedMusic.get(i);
            
            boolean isPlaying = player.getCurrentMusic() != null && 
                               player.getCurrentMusic().getId().equals(music.getId());
            boolean hovered = isInBounds(mouseX, mouseY, listX, itemY, listWidth, itemHeight - 4);
            
            if (hovered) hoveredIndex = i;
            
            int itemBg = isPlaying ? withAlpha(ACCENT_COLOR, 60) : (hovered ? HOVER_COLOR : 0xFF151520);
            drawRoundedRect(context, listX, itemY, listWidth, itemHeight - 4, 8, itemBg);
            
            fillCircle(context, listX + 15, itemY + itemHeight / 2 - 2, 6, music.getDiscColor() | 0xFF000000);
            
            if (isPlaying) {
                context.drawText(textRenderer, Text.literal(">"), listX + 12, itemY + itemHeight / 2 - 6, ACCENT_COLOR, false);
            }
            
            String title = music.getTitle();
            if (textRenderer.getWidth(title) > listWidth - 120) {
                title = title.substring(0, 18) + "...";
            }
            int titleColor = isPlaying ? ACCENT_COLOR : TEXT_PRIMARY;
            context.drawText(textRenderer, Text.literal(title), listX + 30, itemY + 6, titleColor, false);
            
            context.drawText(textRenderer, Text.literal(music.getArtist()), listX + 30, itemY + 18, TEXT_SECONDARY, false);
            
            String duration = music.getFormattedDuration();
            int durationWidth = textRenderer.getWidth(duration);
            context.drawText(textRenderer, Text.literal(duration), listX + listWidth - durationWidth - 10, itemY + 12, TEXT_SECONDARY, false);
            
            itemY += itemHeight;
        }
    }
    
    private void renderFooter(DrawContext context, int x, int y, int width, int mouseX, int mouseY) {
        int centerX = x + width / 2;
        int btnY = y + 10;
        int btnSpacing = 10;
        
        // Shuffle
        shuffleBtnX = centerX - FOOTER_BTN_WIDTH - btnSpacing;
        shuffleBtnY = btnY;
        boolean shuffleHovered = isInBounds(mouseX, mouseY, shuffleBtnX, shuffleBtnY, FOOTER_BTN_WIDTH, FOOTER_BTN_HEIGHT);
        int shuffleBg = player.isShuffle() ? ACCENT_COLOR : (shuffleHovered ? HOVER_COLOR : BUTTON_COLOR);
        drawRoundedRect(context, shuffleBtnX, shuffleBtnY, FOOTER_BTN_WIDTH, FOOTER_BTN_HEIGHT, 6, shuffleBg);
        String shuffleText = player.isShuffle() ? "[S] ON" : "[S] OFF";
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(shuffleText), shuffleBtnX + FOOTER_BTN_WIDTH/2, shuffleBtnY + 7, TEXT_PRIMARY);
        
        // Repeat
        repeatBtnX = centerX + btnSpacing;
        repeatBtnY = btnY;
        boolean repeatHovered = isInBounds(mouseX, mouseY, repeatBtnX, repeatBtnY, FOOTER_BTN_WIDTH, FOOTER_BTN_HEIGHT);
        int repeatBg = player.isRepeat() ? ACCENT_COLOR : (repeatHovered ? HOVER_COLOR : BUTTON_COLOR);
        drawRoundedRect(context, repeatBtnX, repeatBtnY, FOOTER_BTN_WIDTH, FOOTER_BTN_HEIGHT, 6, repeatBg);
        String repeatText = player.isRepeat() ? "[R] ON" : "[R] OFF";
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(repeatText), repeatBtnX + FOOTER_BTN_WIDTH/2, repeatBtnY + 7, TEXT_PRIMARY);
        
        // Crédit
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Made by maxlananas"), centerX, y + 38, 0xFF555555);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int mx = (int) mouseX;
        int my = (int) mouseY;
        
        // Bouton fermer
        if (isInBounds(mx, my, closeBtnX, closeBtnY, 20, 20)) {
            playClick();
            this.close();
            return true;
        }
        
        // Previous
        if (isInBounds(mx, my, prevBtnX, prevBtnY, BTN_SIZE, BTN_SIZE)) {
            playClick();
            player.previous();
            return true;
        }
        
        // Play/Pause
        if (isInBounds(mx, my, playBtnX, playBtnY, BTN_SIZE, BTN_SIZE)) {
            playClick();
            if (player.getCurrentMusic() != null) {
                player.togglePlayPause();
            } else if (hoveredIndex >= 0 && hoveredIndex < displayedMusic.size()) {
                player.play(displayedMusic.get(hoveredIndex));
            } else if (!displayedMusic.isEmpty()) {
                player.play(displayedMusic.get(0));
            }
            return true;
        }
        
        // Next
        if (isInBounds(mx, my, nextBtnX, nextBtnY, BTN_SIZE, BTN_SIZE)) {
            playClick();
            player.next();
            return true;
        }
        
        // Shuffle
        if (isInBounds(mx, my, shuffleBtnX, shuffleBtnY, FOOTER_BTN_WIDTH, FOOTER_BTN_HEIGHT)) {
            playClick();
            player.toggleShuffle();
            return true;
        }
        
        // Repeat
        if (isInBounds(mx, my, repeatBtnX, repeatBtnY, FOOTER_BTN_WIDTH, FOOTER_BTN_HEIGHT)) {
            playClick();
            player.toggleRepeat();
            return true;
        }
        
        // Catégories
        int panelWidth = Math.min(480, this.width - 40);
        int panelX = (this.width - panelWidth) / 2;
        int panelY = 30;
        int tabWidth = (panelWidth - 30) / CATEGORIES.length;
        int tabX = panelX + 15;
        int tabY = panelY + 145;
        
        for (int i = 0; i < CATEGORIES.length; i++) {
            if (isInBounds(mx, my, tabX, tabY, tabWidth - 4, 22)) {
                playClick();
                selectedCategory = i;
                loadMusicList();
                return true;
            }
            tabX += tabWidth;
        }
        
        // Clic sur une musique
        if (hoveredIndex >= 0 && hoveredIndex < displayedMusic.size()) {
            playClick();
            player.play(displayedMusic.get(hoveredIndex));
            return true;
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int maxScroll = Math.max(0, displayedMusic.size() - maxVisible);
        scrollOffset = MathHelper.clamp(scrollOffset - (int) verticalAmount, 0, maxScroll);
        return true;
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            this.close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    private void playClick() {
        MinecraftClient.getInstance().getSoundManager().play(
            PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f)
        );
    }
    
    @Override
    public boolean shouldPause() {
        return false;
    }
    
    // ═══════════════════════════════════════════
    // UTILITAIRES
    // ═══════════════════════════════════════════
    
    private boolean isInBounds(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }
    
    private void drawRoundedRect(DrawContext context, int x, int y, int width, int height, int radius, int color) {
        context.fill(x + radius, y, x + width - radius, y + height, color);
        context.fill(x, y + radius, x + width, y + height - radius, color);
        fillCircle(context, x + radius, y + radius, radius, color);
        fillCircle(context, x + width - radius, y + radius, radius, color);
        fillCircle(context, x + radius, y + height - radius, radius, color);
        fillCircle(context, x + width - radius, y + height - radius, radius, color);
    }
    
    private void drawRoundedBorder(DrawContext context, int x, int y, int width, int height, int radius, int color) {
        context.fill(x + radius, y, x + width - radius, y + 1, color);
        context.fill(x + radius, y + height - 1, x + width - radius, y + height, color);
        context.fill(x, y + radius, x + 1, y + height - radius, color);
        context.fill(x + width - 1, y + radius, x + width, y + height - radius, color);
    }
    
    private void fillCircle(DrawContext context, int cx, int cy, int radius, int color) {
        for (int dy = -radius; dy <= radius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                if (dx * dx + dy * dy <= radius * radius) {
                    context.fill(cx + dx, cy + dy, cx + dx + 1, cy + dy + 1, color);
                }
            }
        }
    }
    
    private int withAlpha(int color, int alpha) {
        return (MathHelper.clamp(alpha, 0, 255) << 24) | (color & 0x00FFFFFF);
    }
    
    private int darkenColor(int color, float factor) {
        int a = (color >> 24) & 0xFF;
        int r = (int) (((color >> 16) & 0xFF) * (1 - factor));
        int g = (int) (((color >> 8) & 0xFF) * (1 - factor));
        int b = (int) ((color & 0xFF) * (1 - factor));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
    
    private float easeOutBack(float x) {
        float c1 = 1.70158f;
        float c3 = c1 + 1;
        return (float) (1 + c3 * Math.pow(x - 1, 3) + c1 * Math.pow(x - 1, 2));
    }
}