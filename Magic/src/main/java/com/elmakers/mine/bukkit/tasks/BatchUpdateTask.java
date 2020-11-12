package com.elmakers.mine.bukkit.tasks;

import com.elmakers.mine.bukkit.magic.MagicController;

public class BatchUpdateTask implements Runnable {
    private final MagicController controller;

    public BatchUpdateTask(MagicController controller) {
        this.controller = controller;
    }

    @Override
    public void run() {
        controller.processPendingBatches();
    }
}
