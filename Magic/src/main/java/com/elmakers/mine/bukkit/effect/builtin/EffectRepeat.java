package com.elmakers.mine.bukkit.effect.builtin;

import com.elmakers.mine.bukkit.effect.EffectRepeating;

public class EffectRepeat extends EffectRepeating {

    @Override
    public void iterate() {
        playEffect();
    }
}
