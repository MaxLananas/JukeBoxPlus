package com.example.jukeboxplus.compat;

import com.example.jukeboxplus.mixin.SoundEngineAccessor;
import com.example.jukeboxplus.mixin.SoundManagerAccessor;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;

import java.util.Map;

/** Pause / resume of a single sound. Same on every version, kept out of {@link Compat} for clarity. */
final class SoundCompat {
    private SoundCompat() {}

    private static ChannelAccess.ChannelHandle handle(SoundManager manager, SoundInstance instance) {
        try {
            SoundEngine engine = ((SoundManagerAccessor) manager).jukeboxplus$getSoundEngine();
            Map<SoundInstance, ChannelAccess.ChannelHandle> map = ((SoundEngineAccessor) (Object) engine).jukeboxplus$getInstanceToChannel();
            return map.get(instance);
        } catch (Throwable t) {
            return null;
        }
    }

    static boolean pause(SoundManager manager, SoundInstance instance) {
        ChannelAccess.ChannelHandle h = handle(manager, instance);
        if (h == null) return false;
        h.execute(channel -> channel.pause());
        return true;
    }

    static boolean resume(SoundManager manager, SoundInstance instance) {
        ChannelAccess.ChannelHandle h = handle(manager, instance);
        if (h == null) return false;
        h.execute(channel -> channel.unpause());
        return true;
    }
}
