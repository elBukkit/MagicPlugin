package com.elmakers.mine.bukkit.effect;

import javax.annotation.Nonnull;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.api.magic.Mage;

public class NPCTargetingContext extends TargetingContext {
    public NPCTargetingContext(@Nonnull Mage mage) {
        super(mage);
    }

    @Override
    public boolean canTarget(Entity entity) {
        return mage.getController().isMagicNPC(entity);
    }

    @Override
    public boolean canTarget(Entity entity, Class<?> targetType) {
        return canTarget(entity);
    }

    @Override
    public boolean isTargetable(Block block) {
        return false;
    }
}
