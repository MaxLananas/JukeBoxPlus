package com.example.jukeboxplus.gui;

import com.example.jukeboxplus.config.ModConfig;
import com.example.jukeboxplus.music.MusicInfo;
import com.example.jukeboxplus.music.MusicPlayer;
import com.example.jukeboxplus.music.MusicTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

import java.util.List;

public class MusicOverlay {
    
    private final MusicTracker tracker;
    private final MusicPlayer player;
    private final MinecraftClient client;
    
    // État de visibilité
    private boolean overlayEnabled = true;
    private boolean showHistory = false;
    
    // Animation
    private float slideProgress = 0f;
    private float targetSlide = 0f;
    
    // Dimensions
    private static final int OVERLAY_WIDTH = 220;
    private static final int OVERLAY_HEIGHT = 75;
    private static final int HISTORY_WIDTH = 200;
    private static final int HISTORY_ITEM_HEIGHT = 30;
    
    // Couleurs
    private static final int BG_COLOR = 0xE8101018;
    private static final int ACCENT_COLOR = 0xFF1DB954;
    private static final int TEXT_PRIMARY = 0xFFFFFFFF;
    private static final int TEXT_SECONDARY = 0xFFB3B3B3;
    private static final int PROGRESS_BG = 0xFF404040;
    
    public MusicOverlay(MusicTracker tracker, MusicPlayer player) {
        this.tracker = tracker;
        this.player = player;
        this.client = MinecraftClient.getInstance();
    }
    
    public void render(DrawContext context, float tickDelta) {
        if (client.player == null) return;
        if (!overlayEnabled) return;
        
        ModConfig config = ModConfig.getInstance();
        if (!config.isOverlayEnabled()) return;
        
        // Déterminer si on doit afficher
        MusicInfo currentMusic = player.getCurrentMusic();
        if (currentMusic == null) {
            currentMusic = tracker.getCurrentMusic();
        }
        
        boolean shouldShow = currentMusic != null;
        
        // Animation
        targetSlide = shouldShow ? 1f : 0f;
        float speed = 0.12f;
        if (slideProgress < targetSlide) {
            slideProgress = Math.min(targetSlide, slideProgress + speed);
        } else if (slideProgress > targetSlide) {
            slideProgress = Math.max(targetSlide, slideProgress - speed);
        }
        
        // Ne pas render si caché
        if (slideProgress < 0.01f && !showHistory) return;
        
        // Position
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        
        int x, y;
        switch (config.getPosition()) {
            case TOP_LEFT -> { x = 10; y = 10; }
            case TOP_RIGHT -> { x = screenWidth - OVERLAY_WIDTH - 10; y = 10; }
            case BOTTOM_LEFT -> { x = 10; y = screenHeight - OVERLAY_HEIGHT - 10; }
            case BOTTOM_RIGHT -> { x = screenWidth - OVERLAY_WIDTH - 10; y = screenHeight - OVERLAY_HEIGHT - 10; }
            default -> { x = screenWidth - OVERLAY_WIDTH - 10; y = 10; }
        }
        
        // Animation slide
        int slideOffset = (int) ((1 - slideProgress) * (OVERLAY_WIDTH + 20));
        if (config.getPosition() == ModConfig.Position.TOP_LEFT || 
            config.getPosition() == ModConfig.Position.BOTTOM_LEFT) {
            x -= slideOffset;
        } else {
            x += slideOffset;
        }
        
        // Render overlay
        if (currentMusic != null) {
            renderMainOverlay(context, x, y, currentMusic);
        }
        
        // Render history
        if (showHistory) {
            int historyY = y + OVERLAY_HEIGHT + 8;
            if (config.getPosition() == ModConfig.Position.BOTTOM_LEFT ||
                config.getPosition() == ModConfig.Position.BOTTOM_RIGHT) {
                historyY = y - 10 - (Math.min(5, tracker.getHistory().size()) * HISTORY_ITEM_HEIGHT + 30);
            }
            renderHistory(context, x, historyY);
        }
    }
    
    private void renderMainOverlay(DrawContext context, int x, int y, MusicInfo music) {
        TextRenderer textRenderer = client.textRenderer;
        
        // Fond
        drawRoundedRect(context, x, y, OVERLAY_WIDTH, OVERLAY_HEIGHT, 12, BG_COLOR);
        
        // Bordure colorée
        int borderColor = music.getDiscColor() | 0xFF000000;
        drawRoundedBorder(context, x, y, OVERLAY_WIDTH, OVERLAY_HEIGHT, 12, borderColor);
        
        // Icône disque
        renderDiscIcon(context, x + 12, y + 8, music);
        
        int textX = x + 55;
        
        // Type badge
        String typeText = music.getType().displayName;
        int typeWidth = textRenderer.getWidth(typeText) + 6;
        drawRoundedRect(context, textX, y + 6, typeWidth, 12, 4, borderColor);
        context.drawText(textRenderer, Text.literal(typeText), textX + 3, y + 8, TEXT_PRIMARY, false);
        
        // Titre
        String title = music.getTitle();
        if (textRenderer.getWidth(title) > OVERLAY_WIDTH - 65) {
            title = title.substring(0, Math.min(title.length(), 18)) + "...";
        }
        context.drawText(textRenderer, Text.literal(title), textX, y + 22, TEXT_PRIMARY, true);
        
        // Artiste
        context.drawText(textRenderer, Text.literal(music.getArtist()), textX, y + 34, TEXT_SECONDARY, true);
        
        // Status et contrôles
        renderStatusLine(context, x + 12, y + 48, music);
        
        // Barre de progression
        int progressY = y + OVERLAY_HEIGHT - 8;
        int progressWidth = OVERLAY_WIDTH - 24;
        
        drawRoundedRect(context, x + 12, progressY, progressWidth, 4, 2, PROGRESS_BG);
        
        int filledWidth = (int) (progressWidth * music.getProgress());
        if (filledWidth > 2) {
            drawRoundedRect(context, x + 12, progressY, filledWidth, 4, 2, borderColor);
        }
    }
    
    private void renderDiscIcon(DrawContext context, int x, int y, MusicInfo music) {
        int size = 36;
        int centerX = x + size / 2;
        int centerY = y + size / 2;
        
        int discColor = music.getDiscColor() | 0xFF000000;
        
        // Disque extérieur
        fillCircle(context, centerX, centerY, size / 2, discColor);
        
        // Cercles intérieurs
        fillCircle(context, centerX, centerY, size / 3, darkenColor(discColor, 0.3f));
        fillCircle(context, centerX, centerY, 5, 0xFF151515);
        fillCircle(context, centerX, centerY, 2, discColor);
        
        // Indicateur de lecture
        if (player.isPlaying()) {
            int noteOffset = (int) (Math.sin(System.currentTimeMillis() / 200.0) * 2);
            context.drawText(client.textRenderer, Text.literal("~"), centerX - 2, centerY - 4 + noteOffset, TEXT_PRIMARY, true);
        } else if (player.isPaused()) {
            context.drawText(client.textRenderer, Text.literal("II"), centerX - 4, centerY - 4, TEXT_PRIMARY, false);
        } else {
            context.drawText(client.textRenderer, Text.literal("-"), centerX - 2, centerY - 4, TEXT_SECONDARY, false);
        }
    }
    
    private void renderStatusLine(DrawContext context, int x, int y, MusicInfo music) {
        // Status
        String status;
        int statusColor;
        if (player.isPlaying()) {
            status = "Playing";
            statusColor = ACCENT_COLOR;
        } else if (player.isPaused()) {
            status = "Paused";
            statusColor = 0xFFFFAA00;
        } else {
            status = "Stopped";
            statusColor = TEXT_SECONDARY;
        }
        context.drawText(client.textRenderer, Text.literal(status), x, y, statusColor, false);
        
        // Shuffle indicator
        if (player.isShuffle()) {
            context.drawText(client.textRenderer, Text.literal("[S]"), x + 55, y, ACCENT_COLOR, false);
        }
        
        // Repeat indicator
        if (player.isRepeat()) {
            context.drawText(client.textRenderer, Text.literal("[R]"), x + 75, y, ACCENT_COLOR, false);
        }
        
        // Volume
        int vol = (int) (player.getVolume() * 100);
        String volText = "Vol:" + vol + "%";
        int volWidth = client.textRenderer.getWidth(volText);
        context.drawText(client.textRenderer, Text.literal(volText), x + OVERLAY_WIDTH - volWidth - 35, y, TEXT_SECONDARY, false);
        
        // Temps
        String time = music.getFormattedTime();
        context.drawText(client.textRenderer, Text.literal(time), x + 95, y, TEXT_SECONDARY, false);
    }
    
    private void renderHistory(DrawContext context, int x, int y) {
        List<MusicInfo> history = tracker.getHistory();
        
        if (history.isEmpty()) {
            drawRoundedRect(context, x, y, HISTORY_WIDTH, 35, 10, BG_COLOR);
            context.drawText(client.textRenderer, Text.literal("No history yet"), x + 12, y + 12, TEXT_SECONDARY, true);
            return;
        }
        
        int displayCount = Math.min(5, history.size());
        int totalHeight = displayCount * HISTORY_ITEM_HEIGHT + 25;
        
        // Fond
        drawRoundedRect(context, x, y, HISTORY_WIDTH, totalHeight, 10, BG_COLOR);
        drawRoundedBorder(context, x, y, HISTORY_WIDTH, totalHeight, 10, ACCENT_COLOR);
        
        // Titre
        context.drawText(client.textRenderer, Text.literal("# Recently Played"), x + 12, y + 8, ACCENT_COLOR, true);
        
        // Items
        int itemY = y + 25;
        for (int i = 0; i < displayCount; i++) {
            MusicInfo music = history.get(i);
            
            // Couleur
            fillCircle(context, x + 18, itemY + HISTORY_ITEM_HEIGHT / 2 - 2, 5, music.getDiscColor() | 0xFF000000);
            
            // Titre
            String title = music.getTitle();
            if (client.textRenderer.getWidth(title) > 110) {
                title = title.substring(0, 14) + "...";
            }
            context.drawText(client.textRenderer, Text.literal(title), x + 30, itemY + 4, TEXT_PRIMARY, false);
            
            // Artiste
            context.drawText(client.textRenderer, Text.literal(music.getArtist()), x + 30, itemY + 15, TEXT_SECONDARY, false);
            
            itemY += HISTORY_ITEM_HEIGHT;
        }
    }
    
    // ═══════════════════════════════════════════
    // CONTRÔLES
    // ═══════════════════════════════════════════
    
    public void toggleVisibility() {
        overlayEnabled = !overlayEnabled;
        playClickSound();
    }
    
    public void toggleHistory() {
        showHistory = !showHistory;
        playClickSound();
    }
    
    public void adjustVolume(float delta) {
        player.adjustVolume(delta);
    }
    
    public boolean isVisible() {
        return overlayEnabled;
    }
    
    private void playClickSound() {
        client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
    }
    
    // ═══════════════════════════════════════════
    // UTILITAIRES
    // ═══════════════════════════════════════════
    
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
    
    private void fillCircle(DrawContext context, int centerX, int centerY, int radius, int color) {
        for (int dy = -radius; dy <= radius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                if (dx * dx + dy * dy <= radius * radius) {
                    context.fill(centerX + dx, centerY + dy, centerX + dx + 1, centerY + dy + 1, color);
                }
            }
        }
    }
    
    private int darkenColor(int color, float factor) {
        int a = (color >> 24) & 0xFF;
        int r = (int) (((color >> 16) & 0xFF) * (1 - factor));
        int g = (int) (((color >> 8) & 0xFF) * (1 - factor));
        int b = (int) ((color & 0xFF) * (1 - factor));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}