package com.example.jukeboxplus.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class MusicButton extends Button {

    private static final int BG       = 0xFF282828;
    private static final int BG_HOV   = 0xFF333333;
    private static final int BG_PLAY  = 0xFF1A3A1A;
    private static final int ACCENT   = 0xFF1DB954;
    private static final int TEXT     = 0xFFFFFFFF;
    private static final int MUTED    = 0xFF535353;

    private final int bgNormal;
    private final int bgHover;
    private final int textNormal;
    private final int textHover;

    public MusicButton(int x, int y, int w, int h, String label, Runnable onPress,
                       int bgNormal, int bgHover, int textNormal, int textHover) {
        super(x, y, w, h, Component.literal(label), b -> onPress.run(), btn -> btn.get());
        this.bgNormal = bgNormal;
        this.bgHover = bgHover;
        this.textNormal = textNormal;
        this.textHover = textHover;
    }

    public MusicButton(int x, int y, int w, int h, String label, Runnable onPress) {
        this(x, y, w, h, label, onPress, BG, BG_HOV, TEXT, 0xFFFFFFFF);
    }

    public MusicButton(int x, int y, int w, int h, String label, Runnable onPress, int accentBg) {
        this(x, y, w, h, label, onPress, accentBg, ACCENT, TEXT, 0xFFFFFFFF);
    }

    @Override
    protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float delta) {
        boolean hov = isHovered();
        int bx = getX(), by = getY(), bw = getWidth(), bh = getHeight();

        g.fill(bx, by, bx + bw, by + bh, hov ? bgHover : bgNormal);
        g.fill(bx, by, bx + bw, by + 1, hov ? ACCENT : 0xFF3E3E3E);
        g.fill(bx, by + bh - 1, bx + bw, by + bh, 0xFF3E3E3E);
        g.fill(bx, by, bx + 1, by + bh, 0xFF3E3E3E);
        g.fill(bx + bw - 1, by, bx + bw, by + bh, 0xFF3E3E3E);

        Font font = Minecraft.getInstance().font;
        String raw = getMessage().getString();
        if (font.width(raw) > bw - 8) {
            while (raw.length() > 1 && font.width(raw + "…") > bw - 8) raw = raw.substring(0, raw.length() - 1);
            raw += "…";
        }
        int color = hov ? textHover : textNormal;
        g.drawCenteredString(font, Component.literal(raw), bx + bw / 2, by + (bh - 8) / 2, color);
    }
}
