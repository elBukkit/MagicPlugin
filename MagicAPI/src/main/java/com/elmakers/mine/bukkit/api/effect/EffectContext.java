package com.elmakers.mine.bukkit.api.effect;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.wand.Wand;

public interface EffectContext {
    @Nonnull
    Mage getMage();
    @Nullable
    Wand getWand();
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
}
