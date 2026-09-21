package com.example.jukeboxplus.gui;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

/** Player screen for 26.1+. */
public final class PlayerScreen extends BasePlayerScreen {

    public PlayerScreen(Screen parent) {
        super(parent);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        drawUi(new GfxImpl(graphics), mouseX, mouseY);
        if (search.visible) search.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        // The UI draws its own dimmed backdrop; skip the vanilla blur/darkening.
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (clickUi(event.x(), event.y(), event.button())) return true;
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (releaseUi(event.x(), event.y(), event.button())) return true;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (dragUi(event.x(), event.y())) return true;
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        if (scrollUi(mouseX, mouseY, vertical)) return true;
        return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (keyUi(map(event.key()))) return true;
        return super.keyPressed(event);
    }

    /** Uses the game's own constants so it works whether the backend is GLFW (26.1/26.2) or SDL (26.3). */
    private static PlayerUi.Key map(int key) {
        if (key == InputConstants.KEY_SPACE) return PlayerUi.Key.SPACE;
        if (key == InputConstants.KEY_LEFT) return PlayerUi.Key.LEFT;
        if (key == InputConstants.KEY_RIGHT) return PlayerUi.Key.RIGHT;
        if (key == InputConstants.KEY_UP) return PlayerUi.Key.UP;
        if (key == InputConstants.KEY_DOWN) return PlayerUi.Key.DOWN;
        return null;
    }
}
