package com.elmakers.mine.bukkit.effect.builtin;

import com.elmakers.mine.bukkit.effect.EffectPlayer;

public class EffectSingle extends EffectPlayer {

    public EffectSingle() {
    }

    public void play() {
        if (playAtOrigin) {
            playEffect(origin, getOriginEntity(), target, getTargetEntity());
        }
        if (playAtTarget && target != null) {
            playEffect(target, getTargetEntity(), origin, getOriginEntity());
        }
    }
}
