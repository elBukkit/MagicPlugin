package com.elmakers.mine.bukkit.tasks;

import com.elmakers.mine.bukkit.magic.MagicController;

public class FinalizeIntegrationTask implements Runnable {
    private final MagicController controller;

    public FinalizeIntegrationTask(MagicController controller) {
        this.controller = controller;
    }

    @Override
    public void run() {
        controller.finalizeIntegration();
    }
}
