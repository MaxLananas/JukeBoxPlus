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

        LOGGER.info("JukeboxPlus loading...");

        // Config
        ModConfig.getInstance();

        // Systems
        musicTracker = new MusicTracker();
        musicPlayer = new MusicPlayer(musicTracker);

        // Overlay
        musicOverlay = new MusicOverlay(musicTracker, musicPlayer);

        // Keybinds
        registerKeybindings();

        // HUD
        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            float tickDelta = tickCounter.getTickDelta(true);
            musicOverlay.render(context, tickDelta);
        });

        // Tick
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {
                musicTracker.tick();
                musicPlayer.tick();
                handleKeybinds(client);
            }
        });

        LOGGER.info("JukeboxPlus loaded!");
    }

    private void registerKeybindings() {
        toggleOverlayKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.jukeboxplus.toggle",
                GLFW.GLFW_KEY_J,
                "key.categories.jukeboxplus"
        ));

        openPlayerKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.jukeboxplus.player",
                GLFW.GLFW_KEY_M,
                "key.categories.jukeboxplus"
        ));

        toggleHistoryKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.jukeboxplus.history",
                GLFW.GLFW_KEY_K,
                "key.categories.jukeboxplus"
        ));

        playPauseKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.jukeboxplus.playpause",
                GLFW.GLFW_KEY_P,
                "key.categories.jukeboxplus"
        ));

        stopKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.jukeboxplus.stop",
                GLFW.GLFW_KEY_O,
                "key.categories.jukeboxplus"
        ));

        volumeUpKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.jukeboxplus.volume_up",
                GLFW.GLFW_KEY_PAGE_UP,
                "key.categories.jukeboxplus"
        ));

        volumeDownKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.jukeboxplus.volume_down",
                GLFW.GLFW_KEY_PAGE_DOWN,
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
