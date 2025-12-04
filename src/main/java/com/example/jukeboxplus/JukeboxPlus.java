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
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JukeboxPlus implements ClientModInitializer {
    
    public static final String MOD_ID = "jukeboxplus";
    public static final Logger LOGGER = LoggerFactory.getLogger("JukeboxPlus");
    
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
        
        // Charger config
        ModConfig.getInstance();
        
        // Initialiser le tracker et player
        musicTracker = new MusicTracker();
        musicPlayer = new MusicPlayer(musicTracker);
        
        // Initialiser l'overlay
        musicOverlay = new MusicOverlay(musicTracker, musicPlayer);
        
        // Enregistrer les keybinds
        registerKeybindings();
        
        // HUD Render
        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            float tickDelta = tickCounter.getTickDelta(true);
            musicOverlay.render(context, tickDelta);
        });
        
        // Tick events
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {
                musicTracker.tick();
                musicPlayer.tick();
                handleKeybinds(client);
            }
        });
        
        LOGGER.info("╔════════════════════════════════════════╗");
        LOGGER.info("║       JukeboxPlus loaded! v2.0         ║");
        LOGGER.info("║  J = Toggle | M = Music Player         ║");
        LOGGER.info("╚════════════════════════════════════════╝");
    }
    
    private void registerKeybindings() {
        toggleOverlayKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.jukeboxplus.toggle",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_J,
            "category.jukeboxplus"
        ));
        
        openPlayerKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.jukeboxplus.player",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_M,
            "category.jukeboxplus"
        ));
        
        toggleHistoryKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.jukeboxplus.history",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            "category.jukeboxplus"
        ));
        
        playPauseKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.jukeboxplus.playpause",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            "category.jukeboxplus"
        ));
        
        stopKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.jukeboxplus.stop",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            "category.jukeboxplus"
        ));
        
        volumeUpKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.jukeboxplus.volume_up",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_PAGE_UP,
            "category.jukeboxplus"
        ));
        
        volumeDownKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.jukeboxplus.volume_down",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_PAGE_DOWN,
            "category.jukeboxplus"
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