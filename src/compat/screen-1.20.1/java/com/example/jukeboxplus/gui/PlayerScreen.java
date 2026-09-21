package com.example.jukeboxplus.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

/** Player screen for 1.20 - 1.20.1. */
public final class PlayerScreen extends BasePlayerScreen {

    public PlayerScreen(Screen parent) {
        super(parent);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        drawUi(new GfxImpl(graphics), mouseX, mouseY);
        if (search.visible) search.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void unfocusSearch() {
        if (search != null) search.setFocused(false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (clickUi(mouseX, mouseY, button)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (releaseUi(mouseX, mouseY, button)) return true;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (dragUi(mouseX, mouseY)) return true;
        return super.mouseDragged(mouseX, mouseY, button, dx, dy);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (scrollUi(mouseX, mouseY, amount)) return true;
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyUi(GlfwKeys.map(keyCode))) return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
