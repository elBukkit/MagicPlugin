package com.elmakers.mine.bukkit.effect;

import org.bukkit.Sound;

import de.slikey.effectlib.util.CustomSound;

public class SoundEffect extends CustomSound implements com.elmakers.mine.bukkit.api.effect.SoundEffect {
    public SoundEffect(Sound sound) {
        super(sound);
    }

    public SoundEffect(String key) {
        super(key);
    }
}
