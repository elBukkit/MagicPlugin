package com.elmakers.mine.bukkit.api.effect;

import java.util.Collection;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;

public interface EffectPlayer {
    void setEffectPlayList(Collection<EffectPlay> plays);
    void setEffect(Effect effect);
    void setEffectData(int data);
    void setScale(float scale);
    void cancel();

    void setSound(Sound sound);
    void setSound(Sound sound, float volume, float pitch);
    void setParameterMap(ConfigurationSection parameters);
    @Deprecated
    void setParameterMap(Map<String, String> parameters);

    void setDelayTicks(int ticks);

    void setParticleOverride(String particleType);
    void setMaterial(MaterialAndData material);
    void setMaterial(Block block);
    void setColor(Color color);
    @Nullable
    Location getSourceLocation(@Nonnull EffectContext context);
    @Nullable
    Location getTargetLocation(@Nonnull EffectContext context);

    boolean playsAtOrigin();
    boolean playsAtTarget();
    boolean playsAtAllTargets();

    void start(EffectContext context);
    void start(Location origin, Location target);
    void start(Entity origin, Entity target);
    void start(Location origin, Entity originEntity, Location target, Entity targetEntity);
    void start(Location origin, Entity originEntity, Location target, Entity targetEntity, Collection<Entity> targets);

    @Deprecated
    boolean shouldUseHitLocation();
    @Deprecated
    boolean shouldUseWandLocation();
    @Deprecated
    boolean shouldUseCastLocation();
    @Deprecated
    boolean shouldUseEyeLocation();
    @Deprecated
    boolean shouldUseBlockLocation();
}
