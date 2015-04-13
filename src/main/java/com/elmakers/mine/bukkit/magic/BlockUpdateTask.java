package com.elmakers.mine.bukkit.magic;

public class BlockUpdateTask implements Runnable {
    private final MagicController controller;

    public BlockUpdateTask(MagicController controller) {
        this.controller = controller;
    }

    @Override
    public void run() {
        controller.processBlockUpdates();
    }
}