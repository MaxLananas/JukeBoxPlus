package com.example.jukeboxplus.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/** {@link Gfx} for 1.20 - 1.21.11, drawing through {@link GuiGraphics}. */
public final class GfxImpl implements Gfx {

    private final GuiGraphics g;
    private final Font font = Minecraft.getInstance().font;

    public GfxImpl(GuiGraphics g) {
        this.g = g;
    }

    @Override public void fill(int x1, int y1, int x2, int y2, int argb) {
        g.fill(x1, y1, x2, y2, argb);
    }

    @Override public void fillGradient(int x1, int y1, int x2, int y2, int top, int bottom) {
        g.fillGradient(x1, y1, x2, y2, top, bottom);
    }

    @Override public void text(String text, int x, int y, int argb, boolean shadow) {
        g.drawString(font, text, x, y, argb, shadow);
    }

    @Override public void centeredText(String text, int centerX, int y, int argb, boolean shadow) {
        g.drawString(font, text, centerX - font.width(text) / 2, y, argb, shadow);
    }

    @Override public int textWidth(String text)                 { return font.width(text); }
    @Override public String trimToWidth(String text, int width) { return font.plainSubstrByWidth(text, width); }
    @Override public int lineHeight()                           { return font.lineHeight; }
    @Override public void scissorOn(int x1, int y1, int x2, int y2) { g.enableScissor(x1, y1, x2, y2); }
    @Override public void scissorOff()                          { g.disableScissor(); }
    @Override public int screenWidth()                          { return g.guiWidth(); }
    @Override public int screenHeight()                         { return g.guiHeight(); }
}
