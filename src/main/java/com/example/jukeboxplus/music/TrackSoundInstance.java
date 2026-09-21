package com.example.jukeboxplus.music;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

/**
 * Sound instance used for tracks started from the player. Being tickable lets the
 * sound engine pick up volume changes live, and lets us end it on demand.
 */
public class TrackSoundInstance extends AbstractTickableSoundInstance {

    private final Track track;

    public TrackSoundInstance(Track track, SoundEvent event, float volume) {
        super(event, SoundSource.RECORDS, SoundInstance.createUnseededRandom());
        this.track = track;
        this.volume = volume;
        this.pitch = 1.0f;
        this.looping = false;
        this.delay = 0;
        this.relative = true;
        this.attenuation = Attenuation.NONE;
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    public Track getTrack() { return track; }

    public void setVolume(float volume) { this.volume = volume; }

    public void end() { stop(); }

    @Override
    public void tick() {
        // Nothing to do: the sound engine reads getVolume() every tick for tickable sounds.
    }
}
