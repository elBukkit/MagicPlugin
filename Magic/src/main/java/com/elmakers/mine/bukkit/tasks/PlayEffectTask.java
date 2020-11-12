package com.elmakers.mine.bukkit.tasks;

import com.elmakers.mine.bukkit.effect.EffectPlayer;

public class PlayEffectTask implements Runnable {
    private final EffectPlayer effect;

    public PlayEffectTask(EffectPlayer effect) {
        this.effect = effect;
    }

    @Override
    public void run() {
        effect.startPlay();
    }
}
