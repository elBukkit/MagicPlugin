package com.elmakers.mine.bukkit.api.effect;

import javax.annotation.Nullable;

import com.elmakers.mine.bukkit.api.wand.Wand;

public interface WandEffectContext extends MageEffectContext {
    @Nullable
    Wand getWand();
    @Nullable
    Wand checkWand();
}
