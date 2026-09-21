package com.example.jukeboxplus.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.client.resources.sounds.SoundInstance;

/** Compat overlay for Minecraft 1.20.2 - 1.20.6. */
final class VersionCompat {

    String targetVersion() {
        return "1.20.2 - 1.20.6";
    }

    SoundEvent soundEvent(String id) {
        return SoundEvent.createVariableRangeEvent(new ResourceLocation(id));
    }

    void stopVanillaMusic(Minecraft mc) {
        mc.getMusicManager().stopPlaying();
    }

    Screen currentScreen(Minecraft mc) {
        return mc.screen;
    }

    void setScreen(Minecraft mc, Screen screen) {
        mc.setScreen(screen);
    }

    boolean isHudHidden(Minecraft mc) {
        return mc.options.hideGui;
    }

    boolean isDebugShown(Minecraft mc) {
        return mc.gui.getDebugOverlay().showDebugScreen();
    }

    /** Resource id of the .ogg behind a sound instance (accessor renamed in 26.x). */
    String soundLocation(SoundInstance instance) {
        return String.valueOf(instance.getSound().getLocation());
    }
}
