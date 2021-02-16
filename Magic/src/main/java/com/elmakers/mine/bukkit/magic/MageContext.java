package com.elmakers.mine.bukkit.magic;

import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import com.elmakers.mine.bukkit.api.magic.CasterProperties;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.effect.EffectContext;
import com.google.common.base.Preconditions;

public class MageContext extends EffectContext implements com.elmakers.mine.bukkit.api.magic.MageContext {
    protected final @Nonnull Mage mage;

    public MageContext(@Nonnull Mage mage) {
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

    @Override
    public boolean canTarget(Entity entity) {
        return true;
    }

    @Override
    public boolean canTarget(Entity entity, Class<?> targetType) {
        return true;
    }

    @Nullable
    @Override
    public Wand getWand() {
        return mage.getActiveWand();
    }

    @Nullable
    @Override
    public Wand checkWand() {
        return mage.checkWand();
    }

    @Override
    public boolean isTargetable(Block block) {
        return true;
    }

    @Override
    public Logger getLogger() {
        return getController().getLogger();
    }

    @Override
    @Nonnull
    public CasterProperties getActiveProperties() {
        return mage.getActiveProperties();
    }

    @Nullable
    @Override
    public Double getAttribute(String attributeKey) {
        return mage.getAttribute(attributeKey);
    }

    @Override
    @Nullable
    public Double getVariable(String variable) {
        ConfigurationSection mageVariables = mage.getVariables();
        if (mageVariables != null && mageVariables.contains(variable)) {
            return mageVariables.getDouble(variable);
        }
        return null;
    }

    @Override
    @Nonnull
    public String getMessage(String key) {
        return getMessage(key, "");
    }

    @Override
    @Nonnull
    public String getMessage(String key, String def) {
        return controller.getMessages().get(key, def);
    }
}
