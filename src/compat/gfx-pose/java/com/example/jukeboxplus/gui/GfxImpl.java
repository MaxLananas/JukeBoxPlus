package com.example.jukeboxplus.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;

/** {@link Gfx} for 1.19.x, drawing through a {@link PoseStack}. */
public final class GfxImpl implements Gfx {

    private final PoseStack pose;
    private final Minecraft mc = Minecraft.getInstance();
    private final Font font = mc.font;

    public GfxImpl(PoseStack pose) {
        this.pose = pose;
    }

    @Override public void fill(int x1, int y1, int x2, int y2, int argb) {
        GuiComponent.fill(pose, x1, y1, x2, y2, argb);
    }

    @Override public void fillGradient(int x1, int y1, int x2, int y2, int top, int bottom) {
        GuiComponent.fillGradient(pose, x1, y1, x2, y2, top, bottom, 0);
    }

    @Override public void text(String text, int x, int y, int argb, boolean shadow) {
        if (shadow) font.drawShadow(pose, text, x, y, argb);
        else font.draw(pose, text, x, y, argb);
    }

    @Override public void centeredText(String text, int centerX, int y, int argb, boolean shadow) {
        text(text, centerX - font.width(text) / 2, y, argb, shadow);
    }

    @Override public int textWidth(String text)                 { return font.width(text); }
    @Override public String trimToWidth(String text, int width) { return font.plainSubstrByWidth(text, width); }
    @Override public int lineHeight()                           { return font.lineHeight; }
    @Override public void scissorOn(int x1, int y1, int x2, int y2) { GuiComponent.enableScissor(x1, y1, x2, y2); }
    @Override public void scissorOff()                          { GuiComponent.disableScissor(); }
    @Override public int screenWidth()                          { return mc.getWindow().getGuiScaledWidth(); }
    @Override public int screenHeight()                         { return mc.getWindow().getGuiScaledHeight(); }
}
