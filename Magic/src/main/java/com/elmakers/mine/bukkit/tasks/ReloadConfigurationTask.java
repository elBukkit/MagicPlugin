package com.elmakers.mine.bukkit.tasks;

import org.bukkit.Bukkit;

import com.elmakers.mine.bukkit.magic.MagicController;

public class ReloadConfigurationTask implements Runnable {
    private final MagicController controller;

    public ReloadConfigurationTask(MagicController controller) {
        this.controller = controller;
    }

    @Override
    public void run() {
        controller.loadConfiguration(Bukkit.getConsoleSender());
    }
}
