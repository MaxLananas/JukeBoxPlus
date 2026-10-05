package com.example.jukeboxplus.mixin;

import com.example.jukeboxplus.JukeboxPlus;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Notifies the tracker when the game starts a sound. On this version {@code play} returns a {@code PlayResult}. */
@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin {

    @Inject(method = "play(Lnet/minecraft/client/resources/sounds/SoundInstance;)Lnet/minecraft/client/sounds/SoundEngine$PlayResult;", at = @At("HEAD"))
    private void jukeboxplus$onPlay(SoundInstance sound, CallbackInfoReturnable<SoundEngine.PlayResult> cir) {
        JukeboxPlus.onSoundPlay(sound);
    }
}
