package com.elmakers.mine.bukkit.api.effect;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

public interface SoundEffect {
    String getCustomSound();
    Sound getSound();
    float getVolume();
    float getPitch();
    int getRange();
    void play(Plugin controller, Entity entity);
    void play(Plugin controller, Location location);
}
