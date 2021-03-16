package com.elmakers.mine.bukkit.tasks;

import org.bukkit.command.CommandSender;

import com.elmakers.mine.bukkit.magic.MagicController;

public class PostStartupLoadTask implements Runnable {
    private final MagicController controller;
    private final ConfigurationLoadTask loader;
    private final CommandSender sender;

    public PostStartupLoadTask(MagicController controller, ConfigurationLoadTask loader, CommandSender sender) {
        this.controller = controller;
        this.loader = loader;
        this.sender = sender;
    }

    @Override
    public void run() {
        controller.finalizePostStartupLoad(loader, sender);
    }
}
