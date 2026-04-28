package com.example.jukeboxplus;

import com.example.jukeboxplus.config.ModConfig;
import com.example.jukeboxplus.gui.MusicOverlay;
import com.example.jukeboxplus.gui.MusicPlayerScreen;
import com.example.jukeboxplus.music.MusicPlayer;
import com.example.jukeboxplus.music.MusicTracker;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
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

    // Keybinds
    private static KeyBinding toggleOverlayKey;
    private static KeyBinding openPlayerKey;
    private static KeyBinding toggleHistoryKey;
    private static KeyBinding playPauseKey;
    private static KeyBinding stopKey;
    private static KeyBinding volumeUpKey;
    private static KeyBinding volumeDownKey;

    @Override
    public void onInitializeClient() {
        instance = this;

        LOGGER.info("[JukeboxPlus] Initializing...");

        // Config
        ModConfig.getInstance();

        // Core systems
        musicTracker = new MusicTracker();
        musicPlayer = new MusicPlayer(musicTracker);

        // UI
        musicOverlay = new MusicOverlay(musicTracker, musicPlayer);

        // Keybinds
        registerKeybindings();

        // HUD render
        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            float tickDelta = tickCounter.getTickDelta(true);
            musicOverlay.render(context, tickDelta);
        });

        // Game tick
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {
                musicTracker.tick();
                musicPlayer.tick();
                handleKeybinds(client);
            }
        });

        LOGGER.info("[JukeboxPlus] Loaded successfully!");
    }

    private void registerKeybindings() {
        toggleOverlayKey = register("toggle", GLFW.GLFW_KEY_J);
        openPlayerKey = register("player", GLFW.GLFW_KEY_M);
        toggleHistoryKey = register("history", GLFW.GLFW_KEY_K);
        playPauseKey = register("playpause", GLFW.GLFW_KEY_P);
        stopKey = register("stop", GLFW.GLFW_KEY_O);
        volumeUpKey = register("volume_up", GLFW.GLFW_KEY_PAGE_UP);
        volumeDownKey = register("volume_down", GLFW.GLFW_KEY_PAGE_DOWN);
    }

    // Helper propre (évite duplication)
    private KeyBinding register(String name, int key) {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.jukeboxplus." + name,
                key,
                "key.categories.jukeboxplus"
        ));
    }

    private void handleKeybinds(MinecraftClient client) {
        while (toggleOverlayKey.wasPressed()) {
            musicOverlay.toggleVisibility();
        }

        while (openPlayerKey.wasPressed()) {
            client.setScreen(new MusicPlayerScreen(musicPlayer, musicTracker));
        }

        while (toggleHistoryKey.wasPressed()) {
            musicOverlay.toggleHistory();
        }

        while (playPauseKey.wasPressed()) {
            musicPlayer.togglePlayPause();
        }

        while (stopKey.wasPressed()) {
            musicPlayer.stop();
        }

        while (volumeUpKey.wasPressed()) {
            musicPlayer.adjustVolume(0.1f);
        }

        while (volumeDownKey.wasPressed()) {
            musicPlayer.adjustVolume(-0.1f);
        }
    }

    public static JukeboxPlus getInstance() {
        return instance;
    }

    public MusicTracker getMusicTracker() {
        return musicTracker;
    }

    public MusicPlayer getMusicPlayer() {
        return musicPlayer;
    }

    public MusicOverlay getMusicOverlay() {
        return musicOverlay;
    }
}
