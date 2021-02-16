package com.elmakers.mine.bukkit.api.magic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.api.effect.EffectContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.wand.Wand;

public interface MageContext extends EffectContext {
    @Nonnull
    Mage getMage();

    boolean isTargetable(Block block);
    boolean canTarget(Entity entity);
    boolean canTarget(Entity entity, Class<?> targetType);

    @Nullable
    Wand getWand();
    @Nullable
    Wand checkWand();
}
