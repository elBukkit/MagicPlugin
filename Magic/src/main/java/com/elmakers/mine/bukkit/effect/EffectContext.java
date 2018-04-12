package com.elmakers.mine.bukkit.effect;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.google.common.base.Preconditions;

public class EffectContext implements com.elmakers.mine.bukkit.api.effect.EffectContext {
    protected final @Nonnull Mage mage;
    protected final @Nullable Wand wand;

    public EffectContext(@Nonnull Mage mage, @Nullable Wand wand) {
        this.mage = Preconditions.checkNotNull(mage);
        this.wand = wand;
    }

    @Nonnull
    @Override
    public Mage getMage() {
        return this.mage;
    }

    @Override
    @Nullable
    public Wand getWand() {
        return wand;
    }

    @Override
    @Nonnull
    public MageController getController() {
        Mage mage = getMage();
        return mage.getController();
    }

    @Nullable
    @Override
    public Location getTargetLocation() {
        return null;
    }

    @Nullable
    @Override
    public Location getLocation() {
        return mage.getLocation();
    }

    @Nullable
    @Override
    public Location getCastLocation() {
        Location wandLocation = wand == null ? null : wand.getLocation();
        return wandLocation == null ? getEyeLocation() : wandLocation;
    }

    @Nullable
    @Override
    public Location getWandLocation() {
        return getCastLocation();
    }

    @Override
    public Location getEyeLocation() {
        return mage.getEyeLocation();
    }

    @Nullable
    @Override
    public Entity getTargetEntity() {
        return null;
    }
}
