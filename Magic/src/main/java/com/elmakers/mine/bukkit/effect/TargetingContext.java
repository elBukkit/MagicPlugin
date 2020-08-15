package com.elmakers.mine.bukkit.effect;

import javax.annotation.Nonnull;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.api.magic.Mage;

public class TargetingContext extends MageEffectContext implements com.elmakers.mine.bukkit.api.effect.TargetingContext {
    public TargetingContext(@Nonnull Mage mage) {
        super(mage);
    }

    @Override
    public boolean canTarget(Entity entity) {
        return true;
    }

    @Override
    public boolean canTarget(Entity entity, Class<?> targetType) {
        return true;
    }

    @Override
    public boolean isTargetable(Block block) {
        return true;
    }
}
