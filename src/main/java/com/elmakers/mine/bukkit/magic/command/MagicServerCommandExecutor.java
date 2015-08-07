package com.elmakers.mine.bukkit.magic.command;

import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
                player = Bukkit.getPlayer(args[0]);
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

            player = Bukkit.getPlayer(args[0]);
            server = args[1];
        }
        api.getController().sendPlayerToServer(player, server);
        return true;
	}

	@Override
	public Collection<String> onTabComplete(CommandSender sender, String commandName, String[] args) {
		List<String> options = new ArrayList<String>();
        if (!sender.hasPermission("Magic.commands.mserver")) return options;

		if (args.length == 1) {
            options.addAll(api.getPlayerNames());
		}
		return options;
	}
}
