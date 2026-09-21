package com.example.jukeboxplus.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.client.resources.sounds.SoundInstance;

/** Compat overlay for Minecraft 26.2. */
final class VersionCompat {

    String targetVersion() {
        return "26.2";
    }

    SoundEvent soundEvent(String id) {
        return SoundEvent.createVariableRangeEvent(Identifier.parse(id));
    }

    void stopVanillaMusic(Minecraft mc) {
        mc.getMusicManager().stopPlaying();
    }

    Screen currentScreen(Minecraft mc) {
        return mc.gui.screen();
    }

    void setScreen(Minecraft mc, Screen screen) {
        mc.gui.setScreen(screen);
    }

    boolean isHudHidden(Minecraft mc) {
        return mc.gui.hud.isHidden();
    }

    boolean isDebugShown(Minecraft mc) {
        return mc.gui.hud.getDebugOverlay().showDebugScreen();
    }

    /** Resource id of the .ogg behind a sound instance (accessor renamed in 26.x). */
    String soundLocation(SoundInstance instance) {
        return String.valueOf(instance.getSound().getIdentifier());
    }
}
