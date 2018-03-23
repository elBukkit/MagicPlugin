package com.elmakers.mine.bukkit.magic.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.utility.DeprecatedUtils;

public class MagicServerCommandExecutor extends MagicTabExecutor {
	public MagicServerCommandExecutor(MagicAPI api) {
		super(api);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String server;
        Player player;
        if (sender instanceof Player)
        {
            if (args.length < 1)
            {
                return false;
            }

            player = (Player)sender;
            if (!player.hasPermission("Magic.commands.mserver"))
            {
                return false;
            }

            if (args.length >= 2)
            {
                player = DeprecatedUtils.getPlayer(args[0]);
                server = args[1];
            }
            else
            {
                server = args[0];
            }
        }
        else
        {
            if (args.length < 2)
            {
                return false;
            }

            player = DeprecatedUtils.getPlayer(args[0]);
            server = args[1];
        }
        api.getController().sendPlayerToServer(player, server);
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
