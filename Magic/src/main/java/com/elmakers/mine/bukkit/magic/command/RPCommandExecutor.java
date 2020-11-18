package com.elmakers.mine.bukkit.magic.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.magic.Mage;

public class RPCommandExecutor extends MagicTabExecutor {

    public RPCommandExecutor(MagicAPI api) {
        super(api, "getrp");
    }

    @Override
    public Collection<String> onTabComplete(CommandSender sender, String commandName, String[] args) {
        List<String> options = new ArrayList<>();
        if (!api.hasPermission(sender, getPermissionNode())) {
            return options;
        }
        if (args.length == 1) {
            options.add("auto");
            options.add("default");
            options.add("manual");
            options.add("off");
            options.add("url");
            options.add("download");
            options.addAll(controller.getAlternateResourcePacks());
        }
        return options;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!api.hasPermission(sender, getPermissionNode())) {
            sendNoPermission(sender);
            return true;
        }
        String subCommand = args.length > 0 ? args[0] : "";
        if (subCommand.equalsIgnoreCase("url") || subCommand.equalsIgnoreCase("download")) {
            sender.sendMessage(controller.getResourcePackURL());
            return true;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage(controller.getMessages().get("commands.in_game"));
            return true;
        }

        if (subCommand.isEmpty()) {
            sender.sendMessage(controller.getMessages().get("commands.getrp.sending"));
            controller.sendResourcePack((Player)sender);
            return true;
        }

        Mage mage = (Mage)controller.getMage(sender);
        if (subCommand.equalsIgnoreCase("auto")) {
            mage.setResourcePackEnabled(true);
            controller.sendResourcePack((Player)sender);
            sender.sendMessage(controller.getMessages().get("commands.getrp.auto"));
            return true;
        }

        if (subCommand.equalsIgnoreCase("off")) {
            mage.setResourcePackEnabled(false);
            sender.sendMessage(controller.getMessages().get("commands.getrp.turnoff"));
            return true;
        }

        if (subCommand.equalsIgnoreCase("manual")) {
            mage.setResourcePackEnabled(null);
            sender.sendMessage(controller.getMessages().get("commands.getrp.manual"));
            return true;
        }

        if (subCommand.equalsIgnoreCase("default")) {
            sender.sendMessage(controller.getMessages().get("commands.getrp.default"));
            mage.setPreferredResourcePack(null);
            controller.sendResourcePack((Player)sender);
            return true;
        }

        if (controller.getAlternateResourcePacks().contains(subCommand)) {
            sender.sendMessage(controller.getMessages().get("commands.getrp.preference").replace("$pack", subCommand));
            if (!mage.isResourcePackEnabledSet()) {
                sender.sendMessage(controller.getMessages().get("commands.getrp.noauto"));
            }
            mage.setPreferredResourcePack(subCommand);
            controller.sendResourcePack((Player)sender);
            return true;
        }

        String message = controller.getMessages().get("commands.unknown_command");
        message = message.replace("$command", subCommand);
        sender.sendMessage(message);

        return true;
    }
}
