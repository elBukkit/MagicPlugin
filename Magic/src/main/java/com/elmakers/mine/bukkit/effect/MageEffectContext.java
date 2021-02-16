package com.elmakers.mine.bukkit.effect;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.google.common.base.Preconditions;

public class MageEffectContext extends EffectContext implements com.elmakers.mine.bukkit.api.effect.MageEffectContext {
    protected final @Nonnull Mage mage;

    public MageEffectContext(@Nonnull Mage mage) {
        super(mage.getController());
        this.mage = Preconditions.checkNotNull(mage);
    }

    @Nonnull
    @Override
    public Mage getMage() {
        return this.mage;
    }

    @Override
    @Nonnull
    public String getName() {
        return getMage().getName();
    }

    @Nullable
    @Override
    public Location getLocation() {
        if (location != null) {
            return location;
        }
        return mage.getLocation();
    }

    @Nullable
    @Override
    public Location getCastLocation() {
        return getEyeLocation();
    }

    @Nullable
    @Override
    public Location getWandLocation() {
        return getCastLocation();
    }

    @Override
    public Location getEyeLocation() {
        if (location != null) {
            return location;
        }
        return mage.getEyeLocation();
    }

    @Override
    public Entity getEntity() {
        return mage.getEntity();
    }

    @Nullable
    @Override
    public LivingEntity getLivingEntity() {
        return mage.getLivingEntity();
    }

    @Override
    @Nullable
    public Color getEffectColor() {
        return mage.getEffectColor();
    }

    @Override
    @Nullable
    public String getEffectParticle() {
        return mage.getEffectParticleName();
    }
}
