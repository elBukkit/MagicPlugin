package com.elmakers.mine.bukkit.tasks;

import java.util.Collection;

import com.elmakers.mine.bukkit.api.effect.EffectPlay;

public class CancelEffectsTask implements Runnable {
    private final Collection<EffectPlay> cancelEffects;

    public CancelEffectsTask(Collection<EffectPlay> effects) {
        cancelEffects = effects;
    }

    @Override
    public void run() {
        for (EffectPlay play : cancelEffects) {
            play.cancel();
        }
    }
}
