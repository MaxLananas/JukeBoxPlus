package com.example.jukeboxplus.compat;

import com.example.jukeboxplus.gui.PlayerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundEvent;

/**
 * Single entry point for everything that differs between Minecraft versions.
 * The implementation ({@link VersionCompat}) is provided by the compat overlay
 * selected at build time, see build.gradle.
 */
public final class Compat {
    private Compat() {}

    private static final VersionCompat IMPL = new VersionCompat();

    /** Displayed Minecraft version this jar was built for. */
    public static String targetVersion()                          { return IMPL.targetVersion(); }

    public static SoundEvent soundEvent(String id)                { return IMPL.soundEvent(id); }

    public static void stopVanillaMusic(Minecraft mc)             { IMPL.stopVanillaMusic(mc); }
    public static String soundLocation(SoundInstance instance)    { return IMPL.soundLocation(instance); }

    public static boolean pauseSound(SoundManager sm, SoundInstance i)  { return SoundCompat.pause(sm, i); }
    public static boolean resumeSound(SoundManager sm, SoundInstance i) { return SoundCompat.resume(sm, i); }

    public static Screen currentScreen(Minecraft mc)              { return IMPL.currentScreen(mc); }
    public static void setScreen(Minecraft mc, Screen screen)     { IMPL.setScreen(mc, screen); }
    public static boolean isHudHidden(Minecraft mc)               { return IMPL.isHudHidden(mc); }
    public static boolean isDebugShown(Minecraft mc)              { return IMPL.isDebugShown(mc); }

    public static void openPlayer(Minecraft mc) {
        Screen s = currentScreen(mc);
        if (s instanceof PlayerScreen) setScreen(mc, null);
        else setScreen(mc, new PlayerScreen(s));
    }
}
