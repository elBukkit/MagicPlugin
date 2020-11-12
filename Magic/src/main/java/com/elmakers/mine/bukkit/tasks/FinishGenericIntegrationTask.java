package com.elmakers.mine.bukkit.tasks;

import com.elmakers.mine.bukkit.magic.MagicController;

public class FinishGenericIntegrationTask implements Runnable {
    private final MagicController controller;

    public FinishGenericIntegrationTask(MagicController controller) {
        this.controller = controller;
    }

    @Override
    public void run() {
        controller.finishGenericIntegration();
    }
}
