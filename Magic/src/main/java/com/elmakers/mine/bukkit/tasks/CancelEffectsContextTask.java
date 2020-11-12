package com.elmakers.mine.bukkit.tasks;

import com.elmakers.mine.bukkit.api.effect.EffectContext;

public class CancelEffectsContextTask implements Runnable {
    private final EffectContext context;

    public CancelEffectsContextTask(EffectContext context) {
        this.context = context;
    }

    @Override
    public void run() {
        context.cancelEffects();
    }
}
