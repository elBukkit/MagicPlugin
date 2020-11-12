package com.elmakers.mine.bukkit.tasks;

import com.elmakers.mine.bukkit.wand.Wand;

public class ApplyWandIconTask implements Runnable {
    private final Wand wand;

    public ApplyWandIconTask(Wand wand) {
        this.wand = wand;
    }

    @Override
    public void run() {
        wand.applyIcon();
    }
}
