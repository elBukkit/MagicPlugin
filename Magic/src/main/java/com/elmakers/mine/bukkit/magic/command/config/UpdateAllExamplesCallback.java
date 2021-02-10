package com.elmakers.mine.bukkit.magic.command.config;

import org.bukkit.command.CommandSender;

import com.elmakers.mine.bukkit.magic.MagicController;

public class UpdateAllExamplesCallback implements ExampleUpdatedCallback {
    private final CommandSender sender;
    private final MagicController controller;
    private volatile int loadingCount = 0;

    public UpdateAllExamplesCallback(CommandSender sender, MagicController controller) {
        this.controller = controller;
        this.sender = sender;
    }

    public synchronized void loading() {
        loadingCount++;
    }

    public synchronized void check() {
        if (loadingCount == 0) {
            String message = controller.getMessages().get("commands.mconfig.example.fetch.success_all");
            sender.sendMessage(message);
            controller.loadConfiguration(sender);
        }
    }

    @Override
    public synchronized void updated(boolean success, String exampleKey, String url) {
        loadingCount--;
        check();
    }
}
