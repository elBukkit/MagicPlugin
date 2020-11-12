package com.elmakers.mine.bukkit.tasks;

import com.elmakers.mine.bukkit.magic.MagicController;

public class MigrationTask implements Runnable {
    private final MagicController controller;

    public MigrationTask(MagicController controller) {
        this.controller = controller;
    }

    @Override
    public void run() {
        controller.checkForMigration();
    }
}
