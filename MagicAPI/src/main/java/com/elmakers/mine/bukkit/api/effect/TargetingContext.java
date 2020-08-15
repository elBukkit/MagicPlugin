package com.elmakers.mine.bukkit.api.effect;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

public interface TargetingContext extends MageEffectContext {
    boolean isTargetable(Block block);
    boolean canTarget(Entity entity);
    boolean canTarget(Entity entity, Class<?> targetType);
}
