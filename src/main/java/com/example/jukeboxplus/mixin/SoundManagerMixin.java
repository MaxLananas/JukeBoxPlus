package com.example.jukeboxplus.mixin;

import com.example.jukeboxplus.JukeboxPlus;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundManager.class)
public class SoundManagerMixin {
    
    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;)V", at = @At("HEAD"))
    private void onSoundPlay(SoundInstance sound, CallbackInfo ci) {
        try {
            if (JukeboxPlus.getInstance() != null && 
                JukeboxPlus.getInstance().getMusicTracker() != null) {
                JukeboxPlus.getInstance().getMusicTracker().onSoundPlayed(sound);
            }
        } catch (Exception e) {
            // Ignore silently
        }
    }
    
    @Inject(method = "stop(Lnet/minecraft/client/sound/SoundInstance;)V", at = @At("HEAD"))
    private void onSoundStop(SoundInstance sound, CallbackInfo ci) {
        try {
            if (JukeboxPlus.getInstance() != null && 
                JukeboxPlus.getInstance().getMusicTracker() != null) {
                JukeboxPlus.getInstance().getMusicTracker().onSoundStopped(sound);
            }
        } catch (Exception e) {
            // Ignore silently
        }
    }
}