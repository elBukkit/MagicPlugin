package com.elmakers.mine.bukkit.effect;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Color;
import org.bukkit.Location;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.wand.Wand;

public class WandEffectContext extends TargetingContext implements com.elmakers.mine.bukkit.api.effect.WandEffectContext {
    protected @Nullable Wand wand;

    public WandEffectContext(@Nonnull Mage mage, @Nullable Wand wand) {
        super(mage);
        this.wand = wand;
    }

    @Override
    @Nullable
    public Wand getWand() {
        return wand;
    }

    @Override
    @Nullable
    public Wand checkWand() {
        boolean offhand = false;
        if (wand != null) {
            offhand = wand.isInOffhand();
            wand.deactivate();
        }
        wand = mage.checkWand();
        if (offhand) {
            wand = mage.getOffhandWand();
        }
        return wand;
    }

    @Nullable
    @Override
    public Location getCastLocation() {
        if (location != null) {
            return location;
        }
        Location wandLocation = wand == null ? null : wand.getLocation();
        return wandLocation == null ? getEyeLocation() : wandLocation;
    }

    @Override
    @Nullable
    public Color getEffectColor() {
        Color color = null;
        if (wand != null) {
            color = wand.getEffectColor();
        }
        if (color == null) {
            color = mage.getEffectColor();
        }
        return color;
    }

    @Override
    @Nullable
    public String getEffectParticle() {
        String particleName = null;
        if (wand != null) {
            particleName = wand.getEffectParticleName();
        }
        if (particleName == null) {
            particleName = mage.getEffectParticleName();
        }
        return particleName;
    }
}
