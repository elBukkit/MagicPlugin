package com.elmakers.mine.bukkit.api.effect;

import com.elmakers.mine.bukkit.api.action.CastContext;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import org.bukkit.entity.Entity;

import java.util.Collection;
import java.util.Map;

public interface EffectPlayer {
    void setEffectPlayList(Collection<EffectPlay> plays);
    void setEffect(Effect effect);
    void setEffectData(int data);
    void setScale(float scale);

    void setSound(Sound sound);
    void setSound(Sound sound, float volume, float pitch);
    void setParameterMap(Map<String, String> parameters);

    void setDelayTicks(int ticks);

    void setParticleOverride(String particleType);
    void setMaterial(MaterialAndData material);
    void setMaterial(Block block);
    void setColor(Color color);
    Location getSourceLocation(CastContext context);
    Location getTargetLocation(CastContext context);

    boolean playsAtOrigin();
    boolean playsAtTarget();
    boolean playsAtAllTargets();

    void start(Location origin, Location target);
    void start(Entity origin, Entity target);
    void start(Location origin, Entity originEntity, Location target, Entity targetEntity);
    void start(Location origin, Entity originEntity, Location target, Entity targetEntity, Collection<Entity> targets);

    @Deprecated boolean shouldUseHitLocation();
    @Deprecated boolean shouldUseWandLocation();
    @Deprecated boolean shouldUseCastLocation();
    @Deprecated boolean shouldUseEyeLocation();
    @Deprecated boolean shouldUseBlockLocation();
}
