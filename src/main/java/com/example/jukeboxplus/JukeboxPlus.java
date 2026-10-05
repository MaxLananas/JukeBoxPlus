package com.example.jukeboxplus;

import com.example.jukeboxplus.compat.Compat;
import com.example.jukeboxplus.compat.Platform;
import com.example.jukeboxplus.config.ModConfig;
import com.example.jukeboxplus.gui.Gfx;
import com.example.jukeboxplus.gui.OverlayUi;
import com.example.jukeboxplus.music.MusicPlayer;
import com.example.jukeboxplus.music.MusicTracker;
import com.example.jukeboxplus.music.TrackDatabase;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JukeboxPlus implements ClientModInitializer {

    public static final String MOD_ID = "jukeboxplus";
    public static final Logger LOGGER = LoggerFactory.getLogger("JukeboxPlus");
    public static final String VERSION = FabricLoader.getInstance().getModContainer(MOD_ID)
            .map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("dev");

    private static JukeboxPlus instance;

    private MusicTracker tracker;
    private MusicPlayer player;
    private OverlayUi overlay;

    /** Key actions, bound to key mappings by {@link Platform}. */
    public enum Action {
        OPEN_PLAYER("player", 'M'),
        TOGGLE_OVERLAY("toggle_overlay", 'J'),
        PLAY_PAUSE("playpause", 'P'),
        STOP("stop", 'O'),
        NEXT("next", ']'),
        PREVIOUS("previous", '['),
        VOLUME_UP("volume_up", '='),
        VOLUME_DOWN("volume_down", '-');

        public final String key;
        /** Default key as a character; the platform maps it to the version's key code. */
        public final char defaultKey;

        Action(String key, char defaultKey) {
            this.key = key;
            this.defaultKey = defaultKey;
        }

        public String translationKey() { return "key.jukeboxplus." + key; }
    }

    @Override
    public void onInitializeClient() {
        instance = this;
        Minecraft mc = Minecraft.getInstance();
        ModConfig.get();
        TrackDatabase.load();
        tracker = new MusicTracker(mc);
        player = new MusicPlayer(mc, tracker);
        overlay = new OverlayUi(player, tracker);

        Platform.registerKeys(this);
        Platform.registerHud(this);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            Platform.pollKeys(this);
            player.tick();
            tracker.tick();
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            player.onDisconnect();
            tracker.onDisconnect();
        });
        LOGGER.info("JukeboxPlus {} ready (Minecraft {})", VERSION, Compat.targetVersion());
    }

    // ----------------------------------------------------------------- actions

    public void run(Action action) {
        Minecraft mc = Minecraft.getInstance();
        switch (action) {
            case OPEN_PLAYER:    Compat.openPlayer(mc); break;
            case TOGGLE_OVERLAY: overlay.toggle(); break;
            case PLAY_PAUSE:
                if (player.hasTrack()) player.togglePause();
                else if (!TrackDatabase.all().isEmpty()) player.play(TrackDatabase.all().get(0), TrackDatabase.all(), 0);
                break;
            case STOP:           player.stop(); break;
            case NEXT:           player.next(); break;
            case PREVIOUS:       player.previous(); break;
            case VOLUME_UP:      player.adjustVolume(0.05f); break;
            case VOLUME_DOWN:    player.adjustVolume(-0.05f); break;
            default: break;
        }
    }

    /** HUD hook, called by the platform every frame with a version-specific {@link Gfx}. */
    public void renderHud(Gfx g) {
        Minecraft mc = Minecraft.getInstance();
        if (Compat.isHudHidden(mc)) return;
        if (Compat.isDebugShown(mc)) return;
        try {
            overlay.draw(g);
        } catch (Exception e) {
            LOGGER.error("Overlay rendering failed", e);
        }
    }

    /** Called from the sound engine mixin. Must never throw. */
    public static void onSoundPlay(SoundInstance sound) {
        JukeboxPlus i = instance;
        if (i == null || i.tracker == null) return;
        try {
            i.tracker.onSoundStarted(sound);
        } catch (Throwable t) {
            LOGGER.debug("Sound tracking failed", t);
        }
    }

    public static JukeboxPlus get()     { return instance; }
    public MusicTracker getTracker()    { return tracker; }
    public MusicPlayer getPlayer()      { return player; }
    public OverlayUi getOverlay()       { return overlay; }
}
