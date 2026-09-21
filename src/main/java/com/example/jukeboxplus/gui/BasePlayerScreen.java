package com.example.jukeboxplus.gui;

import com.example.jukeboxplus.JukeboxPlus;
import com.example.jukeboxplus.compat.Compat;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Version-independent part of the player screen. The per-version {@code PlayerScreen}
 * subclass only overrides the vanilla methods whose signatures changed over time and
 * forwards them here.
 */
public abstract class BasePlayerScreen extends Screen {

    protected final Screen parent;
    protected final PlayerUi ui;
    protected EditBox search;
    private boolean closing;

    protected BasePlayerScreen(Screen parent) {
        super(Component.translatable("jukeboxplus.screen.title"));
        this.parent = parent;
        JukeboxPlus mod = JukeboxPlus.get();
        this.ui = new PlayerUi(mod.getPlayer(), mod.getTracker());
        this.ui.setCloseHandler(this::closeScreen);
    }

    @Override
    protected void init() {
        int[] box = ui.searchBox(this.width, this.height);
        String previous = search != null ? search.getValue() : ui.getQuery();
        search = new EditBox(this.font, box[0] + 1, box[1], box[2], box[3], Component.translatable("jukeboxplus.screen.search"));
        search.setMaxLength(64);
        search.setBordered(true);
        search.setTextColor(Colors.TEXT);
        search.setValue(previous);
        search.setResponder(ui::setQuery);
        styleSearch(search);
        addWidget(search);
    }

    /** Hook for versions that support hints. */
    protected void styleSearch(EditBox box) {}

    protected boolean searchFocused() {
        return search != null && search.isFocused();
    }

    protected void closeScreen() {
        if (closing) return;
        closing = true;
        Compat.setScreen(this.minecraft, parent);
    }

    // ---------------------------------------------------------------- forwards

    protected void drawUi(Gfx g, int mouseX, int mouseY) {
        ui.draw(g, mouseX, mouseY, searchFocused());
        // Widgets are drawn after the panel so the search box sits on top.
        if (!ui.isSettingsOpen()) {
            search.visible = true;
        } else {
            search.visible = false;
        }
    }

    protected boolean clickUi(double mx, double my, int button) {
        if (search != null && search.visible && search.isMouseOver(mx, my)) return false; // let the box handle it
        if (search != null) search.setFocused(false);
        return ui.mouseClicked(mx, my, button);
    }

    protected boolean releaseUi(double mx, double my, int button) {
        return ui.mouseReleased(mx, my, button);
    }

    protected boolean dragUi(double mx, double my) {
        return ui.mouseDragged(mx, my);
    }

    protected boolean scrollUi(double mx, double my, double amount) {
        return ui.mouseScrolled(mx, my, amount);
    }

    /** GLFW-style key codes shared by all versions; SDL versions translate before calling. */
    protected boolean keyUi(PlayerUi.Key key) {
        return ui.keyPressed(key, searchFocused());
    }

    @Override
    public void tick() {
        ui.tick();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        closeScreen();
    }
}
