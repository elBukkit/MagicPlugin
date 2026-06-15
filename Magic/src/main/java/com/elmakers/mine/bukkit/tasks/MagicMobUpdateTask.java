package com.elmakers.mine.bukkit.tasks;

import com.elmakers.mine.bukkit.magic.MagicController;

public class MagicMobUpdateTask implements Runnable {
    private final MagicController controller;

    public MagicMobUpdateTask(MagicController controller) {
        this.controller = controller;
    }

    @Override
    public void run() {
        controller.tickMagicMobs();
    }
}
