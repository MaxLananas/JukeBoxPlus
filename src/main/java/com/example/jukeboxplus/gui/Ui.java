package com.example.jukeboxplus.gui;

import net.minecraft.client.resources.language.I18n;

/** Small helpers for the custom-drawn UI. */
public final class Ui {
    private Ui() {}

    public static String tr(String key, Object... args) {
        return I18n.get(key, args);
    }

    public static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && my >= y && mx < x + w && my < y + h;
    }

    public static String formatTime(long ms) {
        long s = Math.max(0, ms / 1000);
        return String.format("%d:%02d", s / 60, s % 60);
    }

    /** Ease-out cubic for animations. */
    public static float ease(float t) {
        t = Math.max(0f, Math.min(1f, t));
        float inv = 1f - t;
        return 1f - inv * inv * inv;
    }
}
