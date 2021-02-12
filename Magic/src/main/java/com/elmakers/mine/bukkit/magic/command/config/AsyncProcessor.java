package com.elmakers.mine.bukkit.magic.command.config;

import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.magic.MageController;

public class AsyncProcessor {
    public static void success(MageController controller, CommandSender sender, String message) {
        message(controller, sender, message);
    }

    public static void message(MageController controller, CommandSender sender, String message) {
        final Plugin plugin = controller.getPlugin();
        plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {
                sender.sendMessage(message);
            }
        });
    }

    public static void fail(MageController controller, CommandSender sender, String message) {
        fail(controller, sender, message, null, null);
    }

    public static void fail(MageController controller, CommandSender sender, String message, String errorMessage, Exception ex) {
        final Plugin plugin = controller.getPlugin();
        plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {
                sender.sendMessage(ChatColor.RED + message);
                if (errorMessage != null) {
                    controller.getLogger().log(Level.WARNING, errorMessage, ex);
                }
            }
        });
    }
}
