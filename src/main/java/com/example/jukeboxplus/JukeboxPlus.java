package com.example.jukeboxplus;

import com.example.jukeboxplus.config.ModConfig;
import com.example.jukeboxplus.gui.MusicOverlay;
import com.example.jukeboxplus.gui.MusicPlayerScreen;
import com.example.jukeboxplus.music.MusicPlayer;
import com.example.jukeboxplus.music.MusicTracker;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JukeboxPlus implements ClientModInitializer {

    public static final String MOD_ID = "jukeboxplus";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static JukeboxPlus instance;
    private MusicTracker musicTracker;
    private MusicPlayer musicPlayer;
    private MusicOverlay musicOverlay;

    private static KeyMapping toggleOverlayKey;
    private static KeyMapping openPlayerKey;
    private static KeyMapping toggleHistoryKey;
    private static KeyMapping playPauseKey;
    private static KeyMapping stopKey;
    private static KeyMapping volumeUpKey;
    private static KeyMapping volumeDownKey;

    @Override
    public void onInitializeClient() {
        instance = this;
        ModConfig.getInstance();

        musicTracker = new MusicTracker();
        musicPlayer = new MusicPlayer(musicTracker);
        musicOverlay = new MusicOverlay(musicTracker, musicPlayer);

        KeyMapping.Category cat = new KeyMapping.Category(ResourceLocation.parse("jukeboxplus:category"));
        toggleOverlayKey = bind("toggle", GLFW.GLFW_KEY_J, cat);
        openPlayerKey = bind("player", GLFW.GLFW_KEY_M, cat);
        toggleHistoryKey = bind("history", GLFW.GLFW_KEY_K, cat);
        playPauseKey = bind("playpause", GLFW.GLFW_KEY_P, cat);
        stopKey = bind("stop", GLFW.GLFW_KEY_O, cat);
        volumeUpKey = bind("volume_up", GLFW.GLFW_KEY_PAGE_UP, cat);
        volumeDownKey = bind("volume_down", GLFW.GLFW_KEY_PAGE_DOWN, cat);

        HudRenderCallback.EVENT.register((guiGraphics, deltaTracker) -> musicOverlay.render(guiGraphics));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            musicTracker.tick();
            musicPlayer.tick();
            handleKeys(client);
        });

        LOGGER.info("[JukeboxPlus] Initialized ✓");
    }

    private KeyMapping bind(String name, int key, KeyMapping.Category cat) {
        return KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.jukeboxplus." + name, InputConstants.Type.KEYSYM, key, cat));
    }

    private void handleKeys(Minecraft client) {
        while (toggleOverlayKey.consumeClick()) musicOverlay.toggleVisibility();
        while (openPlayerKey.consumeClick()) client.setScreen(new MusicPlayerScreen(musicPlayer, musicTracker));
        while (toggleHistoryKey.consumeClick()) musicOverlay.toggleHistory();
        while (playPauseKey.consumeClick()) musicPlayer.togglePlayPause();
        while (stopKey.consumeClick()) musicPlayer.stop();
        while (volumeUpKey.consumeClick()) musicPlayer.adjustVolume(0.1f);
        while (volumeDownKey.consumeClick()) musicPlayer.adjustVolume(-0.1f);
    }

    public static JukeboxPlus getInstance()  { return instance; }
    public MusicTracker getMusicTracker()     { return musicTracker; }
    public MusicPlayer getMusicPlayer()       { return musicPlayer; }
    public MusicOverlay getMusicOverlay()     { return musicOverlay; }
}
