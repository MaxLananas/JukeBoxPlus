package com.example.jukeboxplus.mixin;

import com.example.jukeboxplus.JukeboxPlus;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundEngine.class)
public class SoundEngineMixin {

    @Inject(method = "play", at = @At("HEAD"))
    private void jukeboxplus$onPlay(SoundInstance sound, CallbackInfoReturnable<?> cir) {
        try {
            if (JukeboxPlus.getInstance() != null && JukeboxPlus.getInstance().getMusicTracker() != null) {
                JukeboxPlus.getInstance().getMusicTracker().onSoundPlayed(sound);
            }
        } catch (Exception ignored) {}
    }
}
