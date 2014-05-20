package com.elmakers.mine.bukkit.effect.builtin;

import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.effect.EffectPlayer;

public class EffectSingle extends EffectPlayer {

    public EffectSingle() {

    }

    public EffectSingle(Plugin plugin) {
        super(plugin);
    }

    public void play() {
        if (playAtOrigin) {
            playEffect(origin);
        }
        if (playAtTarget && target != null) {
            playEffect(target);
        }
    }
}
