package com.elmakers.mine.bukkit.tasks;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.elmakers.mine.bukkit.magic.MagicController;

public class LogWatchdogTask implements Runnable {
    private final MagicController controller;
    private final CommandSender sender;

    public LogWatchdogTask(MagicController controller, CommandSender sender) {
        this.controller = controller;
        this.sender = sender;
    }

    @Override
    public void run() {
        // This only sends messages or changes logger flags, so it should be thread-safe
        controller.resetLoading(sender);
        sender.sendMessage(ChatColor.RED + " Configuration load taking longer than expected");
        sender.sendMessage(ChatColor.GRAY + " Further errors and warnings will go to the server logs");
    }
}
