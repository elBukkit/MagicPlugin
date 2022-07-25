package com.elmakers.mine.bukkit.tasks;

import com.elmakers.mine.bukkit.magic.MagicController;

public class FinalizeImmediatelyTask implements Runnable {
    private final MagicController controller;

    public FinalizeImmediatelyTask(MagicController controller) {
        this.controller = controller;
    }

    @Override
    public void run() {
        controller.finalizeImmediately();
    }
}
