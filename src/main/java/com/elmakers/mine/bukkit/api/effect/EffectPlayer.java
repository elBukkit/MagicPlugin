package com.elmakers.mine.bukkit.api.effect;

import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import org.bukkit.entity.Entity;

import java.util.Collection;

public interface EffectPlayer {
    public void setEffectPlayList(Collection<EffectPlay> plays);
    public void setEffect(Effect effect);
    public void setEffectData(int data);
    public void setScale(float scale);

    public void setSound(Sound sound);
    public void setSound(Sound sound, float volume, float pitch);

    public void setDelayTicks(int ticks);

    public void setParticleOverride(String particleType);
    public void setMaterial(MaterialAndData material);
    public void setMaterial(Block block);
    public void setColor(Color color);
    public void setOffset(float x, float y, float z);
    public boolean shouldUseHitLocation();
    public boolean shouldUseWandLocation();
    public boolean shouldUseEyeLocation();

    public void start(Location origin, Location target);
    public void start(Entity origin, Entity target);
    public void start(Location origin, Entity originEntity, Location target, Entity targetEntity);
    public void start(Location origin, Entity originEntity, Location target, Entity targetEntity, Collection<Entity> targets);
}
