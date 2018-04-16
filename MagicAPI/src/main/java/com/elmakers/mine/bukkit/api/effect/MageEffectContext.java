package com.elmakers.mine.bukkit.api.effect;

import javax.annotation.Nonnull;

import com.elmakers.mine.bukkit.api.magic.Mage;

public interface MageEffectContext extends EffectContext {
    @Nonnull
    Mage getMage();
}
