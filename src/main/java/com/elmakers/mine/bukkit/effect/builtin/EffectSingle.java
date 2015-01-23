package com.elmakers.mine.bukkit.effect.builtin;

import com.elmakers.mine.bukkit.effect.EffectPlayer;

public class EffectSingle extends EffectPlayer {

    public EffectSingle() {
    }

    public void play() {
        playEffect(origin, getOriginEntity(), target, getTargetEntity());
    }
}
