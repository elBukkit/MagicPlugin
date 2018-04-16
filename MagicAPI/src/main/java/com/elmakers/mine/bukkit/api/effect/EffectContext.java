package com.elmakers.mine.bukkit.api.effect;

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.api.magic.MageController;

public interface EffectContext {
    @Nullable
    Location getEyeLocation();
    @Nullable
    Location getLocation();
    @Nullable
    Entity getTargetEntity();
    @Nullable
    Location getTargetLocation();
    @Nonnull
    MageController getController();
    @Nullable
    Location getCastLocation();
    @Nullable
    Location getWandLocation();

    void cancelEffects();
    void trackEffects(EffectPlayer player);
    Collection<EffectPlay> getCurrentEffects();
}
