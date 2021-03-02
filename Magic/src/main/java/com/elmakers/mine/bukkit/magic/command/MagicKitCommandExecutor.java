package com.elmakers.mine.bukkit.magic.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.kit.Kit;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.utility.DeprecatedUtils;
import com.elmakers.mine.bukkit.utility.NMSUtils;

public class MagicKitCommandExecutor extends MagicTabExecutor {
    public MagicKitCommandExecutor(MagicAPI api) {
        super(api, "mkit");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!api.hasPermission(sender, getPermissionNode())) {
            sendNoPermission(sender);
            return true;
        }

        if (args.length == 0 || args.length > 2) {
            sender.sendMessage(controller.getMessages().get("commands.mkit.usage"));
            return true;
        }

        String playerName = null;
        String kitName = null;

        if (args.length == 1) {
            kitName = args[0];
        } else {
            playerName = args[0];
            Player testPlayer = DeprecatedUtils.getPlayer(playerName);
            if (testPlayer == null && !playerName.startsWith("@")) {
                kitName = args[0];
                playerName = null;
            } else {
                kitName = args[1];
            }
        }

        Kit kit = controller.getKit(kitName);
        if (kit == null) {
            sender.sendMessage(controller.getMessages().get("commands.mkit.unknown_kit"));
            return true;
        }

        List<Player> players = new ArrayList<>();
        if (playerName != null && sender.hasPermission("Magic.commands.mkit.others")) {
            List<Entity> targets = NMSUtils.selectEntities(sender, playerName);
            if (targets != null) {
                for (Entity entity : targets) {
                    if (entity instanceof Player) {
                        players.add((Player)entity);
                    }
                }
            } else {
                Player player = DeprecatedUtils.getPlayer(playerName);
                if (player == null) {
                    sender.sendMessage(controller.getMessages().get("commands.mkit.console_usage").replace("$player", playerName));
                    return true;
                }
                players.add(player);
            }
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage(controller.getMessages().get("commands.mkit.console_usage"));
                return true;
            }
            players.add((Player)sender);
        }

        for (Player player : players) {
            Mage mage = controller.getMage(player);
            long cooldownRemaining = kit.getRemainingCooldown(mage);
            if (cooldownRemaining > 0) {
                String timeDescription = controller.getMessages().getTimeDescription(cooldownRemaining, "wait", "cooldown");
                String message = controller.getMessages().get("commands.mkit.cooldown");
                sender.sendMessage(message.replace("$time", timeDescription));
                continue;
            }
            if (!kit.isAllowed(mage)) {
                sender.sendMessage(controller.getMessages().get("commands.mkit.no_requirements"));
                continue;
            }
            kit.give(mage);
        }

        return true;
    }

    @Override
    public Collection<String> onTabComplete(CommandSender sender, String commandName, String[] args) {
        Set<String> options = new HashSet<>();
        if (!sender.hasPermission("Magic.commands.mkit")) return options;

        if (args.length == 1 && sender.hasPermission("Magic.commands.mkit.others")) {
            options.addAll(api.getPlayerNames());
        }

        Mage mage = controller.getMage(sender);
        if (args.length == 1 || args.length == 2) {
            for (String key : controller.getKitKeys()) {
                Kit kit = controller.getKit(key);
                if (kit != null && kit.isAllowed(mage)) {
                    options.add(key);
                }
            }
        }
        return options;
    }
}
