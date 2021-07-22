package com.elmakers.mine.bukkit.tasks;

import com.elmakers.mine.bukkit.magic.MagicController;

public class MagicBlockUpdateTask implements Runnable {
    private final MagicController controller;

    public MagicBlockUpdateTask(MagicController controller) {
        this.controller = controller;
    }

    @Override
    public void run() {
        controller.tickMagicBlocks();
    }
}
