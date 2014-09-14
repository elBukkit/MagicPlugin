package com.elmakers.mine.bukkit.utility;

import org.bukkit.Sound;

public class SoundEffect {
    private final Sound sound;
    private float volume = 1.0f;
    private float pitch = 1.0f;

    public SoundEffect(Sound sound) {
        this.sound = sound;
    }

    public SoundEffect(Sound sound, float volume, float pitch) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

    public Sound getSound() {
        return sound;
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public String toString() {
        return sound.name() + "," + volume + "," + pitch;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof SoundEffect)) return false;

        SoundEffect otherEffect = (SoundEffect)other;
        return sound != otherEffect.sound || pitch != otherEffect.pitch || volume != otherEffect.volume;
    }

}
