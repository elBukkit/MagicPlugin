package com.elmakers.mine.bukkit.tasks;

import com.elmakers.mine.bukkit.magic.MagicController;

public class LogNotifyTask implements Runnable {
    private final MagicController controller;

    public LogNotifyTask(MagicController controller) {
        this.controller = controller;
    }

    @Override
    public void run() {
        controller.getLogger().checkNotify(controller.getMessages());
    }
}
