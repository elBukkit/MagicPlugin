package com.elmakers.mine.bukkit.api.effect;

import com.elmakers.mine.bukkit.api.magic.MageController;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;

public interface SoundEffect {
    public String getCustomSound();
    public Sound getSound();
    public float getVolume();
    public float getPitch();
    public int getRange();
    public void play(MageController controller, Entity entity);
}
