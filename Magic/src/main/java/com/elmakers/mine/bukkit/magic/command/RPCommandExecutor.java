package com.elmakers.mine.bukkit.magic.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.rp.ResourcePackPreference;
import com.elmakers.mine.bukkit.magic.Mage;
import com.elmakers.mine.bukkit.utility.StringUtils;

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
        Mage mage = (Mage)controller.getMage(sender);
        String subCommand = args.length > 0 ? args[0] : "";
        if (subCommand.equalsIgnoreCase("download")) {
            if (!(sender instanceof Player)) {
                if (controller.isResourcePackEnabled()) {
                    mage.sendMessage(controller.getResourcePackURL(sender));
                } else {
                    if (controller.isResourcePackSetByServer()) {
                        mage.sendMessage(controller.getMessages().get("commands.getrp.disabled_server"));
                    } else {
                        mage.sendMessage(controller.getMessages().get("commands.getrp.disabled"));
                    }
                }
                return true;
            }

            String message;
            if (controller.isResourcePackEnabled()) {
                message = controller.getMessages().get("commands.getrp.download");
                message = message.replace("$url", controller.getResourcePackURL(sender));
            } else {
                message = controller.getMessages().get("commands.getrp.download_disabled");
            }
            mage.sendMessage(message);
            mage.setResourcePackPreference(ResourcePackPreference.DOWNLOADED);
            return true;
        }

        if (!controller.isResourcePackEnabled()) {
            List<String> resourcePackPlugins = controller.getResourcePackPlugins();
            if (controller.isResourcePackSetByServer()) {
                mage.sendMessage(controller.getMessages().get("commands.getrp.disabled_server"));
            } else if (!resourcePackPlugins.isEmpty()) {
                String message = controller.getMessages().get("commands.getrp.disabled_plugins");
                message = message.replace("$plugins", StringUtils.join(resourcePackPlugins, ","));
                mage.sendMessage(message);
            } else {
                mage.sendMessage(controller.getMessages().get("commands.getrp.disabled"));
            }
            return true;
        }

        if (subCommand.equalsIgnoreCase("url")) {
            mage.sendMessage(controller.getResourcePackURL(sender));
            return true;
        }
        if (!(sender instanceof Player)) {
            mage.sendMessage(controller.getMessages().get("commands.in_game"));
            return true;
        }

        if (subCommand.isEmpty()) {
            mage.sendMessage(controller.getMessages().get("commands.getrp.sending"));
            controller.sendResourcePack((Player)sender);
            return true;
        }
        if (subCommand.equalsIgnoreCase("auto")) {
            mage.setResourcePackPreference(ResourcePackPreference.AUTOMATIC);
            controller.sendResourcePack((Player)sender);
            mage.sendMessage(controller.getMessages().get("commands.getrp.auto"));
            return true;
        }

        if (subCommand.equalsIgnoreCase("off")) {
            mage.setResourcePackPreference(ResourcePackPreference.DISABLED);
            mage.sendMessage(controller.getMessages().get("commands.getrp.turnoff"));
            return true;
        }

        if (subCommand.equalsIgnoreCase("manual")) {
            mage.setResourcePackPreference(ResourcePackPreference.MANUAL);
            mage.sendMessage(controller.getMessages().get("commands.getrp.manual"));
            return true;
        }

        if (subCommand.equalsIgnoreCase("default")) {
            sender.sendMessage(controller.getMessages().get("commands.getrp.default"));
            mage.setResourcePackPreference(ResourcePackPreference.DEFAULT);
            mage.setPreferredResourcePack(null);
            controller.sendResourcePack((Player)sender);
            return true;
        }

        if (controller.getAlternateResourcePacks().contains(subCommand)) {
            mage.sendMessage(controller.getMessages().get("commands.getrp.preference").replace("$pack", subCommand));
            if (mage.getResourcePackPreference() != ResourcePackPreference.AUTOMATIC) {
                mage.sendMessage(controller.getMessages().get("commands.getrp.noauto"));
            }
            mage.setPreferredResourcePack(subCommand);
            controller.sendResourcePack((Player)sender);
            return true;
        }

        String message = controller.getMessages().get("commands.unknown_command");
        message = message.replace("$command", subCommand);
        mage.sendMessage(message);

        return true;
    }
}
