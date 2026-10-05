package com.example.jukeboxplus.gui;

/**
 * Tiny drawing abstraction so the UI code is identical on every Minecraft version.
 * One implementation per rendering API lives in {@code src/compat/gfx-*} and wraps
 * {@code PoseStack} (1.19), {@code GuiGraphics} (1.20 - 1.21.11) or
 * {@code GuiGraphicsExtractor} (26.1+).
 */
public interface Gfx {

    void fill(int x1, int y1, int x2, int y2, int argb);

    void fillGradient(int x1, int y1, int x2, int y2, int argbTop, int argbBottom);

    void text(String text, int x, int y, int argb, boolean shadow);

    void centeredText(String text, int centerX, int y, int argb, boolean shadow);

    int textWidth(String text);

    /** Longest prefix of {@code text} that fits in {@code width} pixels. */
    String trimToWidth(String text, int width);

    int lineHeight();

    void scissorOn(int x1, int y1, int x2, int y2);

    void scissorOff();

    /** Scaled GUI width of the window. */
    int screenWidth();

    /** Scaled GUI height of the window. */
    int screenHeight();

    // ------------------------------------------------------------------ helpers

    default void rect(int x, int y, int w, int h, int argb) {
        if (w <= 0 || h <= 0) return;
        fill(x, y, x + w, y + h, argb);
    }

    default void border(int x, int y, int w, int h, int argb) {
        rect(x, y, w, 1, argb);
        rect(x, y + h - 1, w, 1, argb);
        rect(x, y, 1, h, argb);
        rect(x + w - 1, y, 1, h, argb);
    }

    /** Text clipped with an ellipsis if it does not fit. */
    default void textClipped(String text, int x, int y, int maxWidth, int argb, boolean shadow) {
        if (text == null || text.isEmpty() || maxWidth <= 0) return;
        if (textWidth(text) <= maxWidth) {
            text(text, x, y, argb, shadow);
            return;
        }
        int dots = textWidth("...");
        String cut = trimToWidth(text, Math.max(0, maxWidth - dots)).trim();
        text(cut + "...", x, y, argb, shadow);
    }

    default void textRight(String text, int rightX, int y, int argb, boolean shadow) {
        text(text, rightX - textWidth(text), y, argb, shadow);
    }
}
