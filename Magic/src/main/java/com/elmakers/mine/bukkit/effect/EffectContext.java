package com.elmakers.mine.bukkit.effect;

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.api.effect.EffectPlay;
import com.elmakers.mine.bukkit.api.magic.MageController;

public class EffectContext implements com.elmakers.mine.bukkit.api.effect.EffectContext {
    protected MageController controller;
    protected Location location;
    protected Collection<EffectPlay> currentEffects = null;

    public EffectContext(@Nonnull MageController controller) {
        this.controller = controller;
        currentEffects = new ArrayList<>();
    }

    public EffectContext(@Nonnull MageController controller, Location location) {
        this(controller);
        this.location = location;
    }

    @Override
    @Nonnull
    public MageController getController() {
        return controller;
    }

    @Nullable
    @Override
    public Location getTargetLocation() {
        return null;
    }

    @Nullable
    @Override
    public Location getLocation() {
        return location;
    }

    @Nullable
    @Override
    public Location getCastLocation() {
        return location;
    }

    @Nullable
    @Override
    public Location getWandLocation() {
        return location;
    }

    @Override
    public Location getEyeLocation() {
        return location;
    }

    @Nullable
    @Override
    public Entity getTargetEntity() {
        return null;
    }

    @Override
    public void cancelEffects() {
        for (EffectPlay player : currentEffects) {
            player.cancel();
        }
        currentEffects.clear();
    }

    @Override
    public Collection<EffectPlay> getCurrentEffects() {
        return currentEffects;
    }

    @Override
    public void trackEffects(com.elmakers.mine.bukkit.api.effect.EffectPlayer player) {
        player.setEffectPlayList(currentEffects);
    }
}
