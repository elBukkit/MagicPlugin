package com.elmakers.mine.bukkit.api.effect;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

public interface SoundEffect {
    public String getCustomSound();
    public Sound getSound();
    public float getVolume();
    public float getPitch();
    public int getRange();
    public void play(Plugin controller, Entity entity);
    public void play(Plugin controller, Location location);
}
