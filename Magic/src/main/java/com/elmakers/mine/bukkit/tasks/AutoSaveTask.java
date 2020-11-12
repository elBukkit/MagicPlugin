package com.elmakers.mine.bukkit.tasks;

import com.elmakers.mine.bukkit.magic.MagicController;

public class AutoSaveTask implements Runnable {
    private final MagicController controller;

    public AutoSaveTask(MagicController controller) {
        this.controller = controller;
    }

    @Override
    public void run() {
        controller.info("Auto-saving Magic data");
        controller.save(true);
    }
}
