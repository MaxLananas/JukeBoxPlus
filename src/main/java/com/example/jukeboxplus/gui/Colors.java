package com.example.jukeboxplus.gui;

/** Palette shared by the overlay and the player screen. */
public final class Colors {
    private Colors() {}

    public static final int BG           = 0xF0141419;
    public static final int BG_LIGHT     = 0xFF1E1E26;
    public static final int PANEL        = 0xFF23232D;
    public static final int PANEL_HOVER  = 0xFF2E2E3A;
    public static final int PANEL_ACTIVE = 0xFF3A3A4A;
    public static final int BORDER       = 0xFF3C3C4C;
    public static final int ACCENT       = 0xFF4CC38A;
    public static final int ACCENT_DARK  = 0xFF2E7D57;
    public static final int ACCENT_SOFT  = 0x404CC38A;
    public static final int WARN         = 0xFFE0A030;
    public static final int TEXT         = 0xFFF2F2F2;
    public static final int TEXT_DIM     = 0xFFA0A0AC;
    public static final int TEXT_MUTED   = 0xFF6C6C78;
    public static final int TRACK_BG     = 0xFF34343F;

    public static int withAlpha(int argb, float alpha) {
        int a = Math.round(Math.max(0f, Math.min(1f, alpha)) * ((argb >>> 24) & 0xFF));
        return (a << 24) | (argb & 0x00FFFFFF);
    }

    public static int lerp(int a, int b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int aa = (a >>> 24) & 0xFF, ar = (a >>> 16) & 0xFF, ag = (a >>> 8) & 0xFF, ab = a & 0xFF;
        int ba = (b >>> 24) & 0xFF, br = (b >>> 16) & 0xFF, bg = (b >>> 8) & 0xFF, bb = b & 0xFF;
        int ra = Math.round(aa + (ba - aa) * t), rr = Math.round(ar + (br - ar) * t);
        int rg = Math.round(ag + (bg - ag) * t), rb = Math.round(ab + (bb - ab) * t);
        return (ra << 24) | (rr << 16) | (rg << 8) | rb;
    }
}
