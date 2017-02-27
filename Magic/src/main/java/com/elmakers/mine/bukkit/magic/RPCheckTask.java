package com.elmakers.mine.bukkit.magic;

public class RPCheckTask implements Runnable {
    private final MagicController controller;

    public RPCheckTask(MagicController controller) {
        this.controller = controller;
    }

    public void run() {
        controller.checkResourcePack(controller.getPlugin().getServer().getConsoleSender(), true);
    }
}
