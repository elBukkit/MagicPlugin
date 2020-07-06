package com.elmakers.mine.bukkit.api.effect;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public interface SoundEffect {
    boolean isCustom();
    String getCustomSound();
    Sound getSound();
    float getVolume();
    float getPitch();
    int getRange();
    void play(Plugin controller, Entity entity);
    void play(Plugin controller, Location location);
    void stop(Player player);
}
