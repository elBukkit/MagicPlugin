package com.elmakers.mine.bukkit.tasks;

import com.elmakers.mine.bukkit.wand.Wand;

public class OpenWandTask implements Runnable {
    private final Wand wand;

    public OpenWandTask(Wand wand) {
        this.wand = wand;
    }

    @Override
    public void run() {
        wand.showActiveIcon(true);
        wand.playPassiveEffects("open");
    }
}
