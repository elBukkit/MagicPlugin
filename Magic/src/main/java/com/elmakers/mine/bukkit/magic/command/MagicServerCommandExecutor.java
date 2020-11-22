package com.elmakers.mine.bukkit.magic.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.utility.DeprecatedUtils;
import com.elmakers.mine.bukkit.utility.NMSUtils;

public class MagicServerCommandExecutor extends MagicTabExecutor {
    public MagicServerCommandExecutor(MagicAPI api) {
        super(api, "mserver");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String server;

        if (args.length < 1) {
            return false;
        }

        List<Entity> targets;
        Player player = sender instanceof Player ? (Player)sender : null;
        if (args.length < 2) {
            if (player == null) {
                return false;
            }
            server = args[0];
            targets = new ArrayList<>();
            targets.add(player);
        } else {
            targets = NMSUtils.selectEntities(sender, args[0]);
            if (targets == null) {
                Player findPlayer = DeprecatedUtils.getPlayer(args[0]);
                if (findPlayer != null) {
                    targets = new ArrayList<>();
                    targets.add(findPlayer);
                }
            }
            server = args[1];
        }
        if (targets == null || targets.isEmpty()) {
            sender.sendMessage("No targets founds");
            return true;
        }
        for (Entity entity : targets) {
            if (entity instanceof Player) {
                api.getController().sendPlayerToServer((Player)entity, server);
            }
        }
        return true;
    }

    @Override
    public Collection<String> onTabComplete(CommandSender sender, String commandName, String[] args) {
        List<String> options = new ArrayList<>();
        if (!sender.hasPermission("Magic.commands.mserver")) return options;

        if (args.length == 1) {
            options.addAll(api.getPlayerNames());
        }
        return options;
    }
}
