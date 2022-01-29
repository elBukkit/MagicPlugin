package com.elmakers.mine.bukkit.api.warp;

import java.util.Collection;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.api.effect.EffectPlayer;

public interface WarpDescription {
    @Nonnull
    String getKey();
    boolean isMaintainDirection();
    @Nullable
    Collection<EffectPlayer> getEffects();
    boolean hasPermission(Entity entity);
}
