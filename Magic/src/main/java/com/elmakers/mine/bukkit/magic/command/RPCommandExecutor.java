package com.elmakers.mine.bukkit.magic.command;

import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;

public class RPCommandExecutor extends MagicTabExecutor {

	public RPCommandExecutor(MagicAPI api) {
		super(api);
	}
	
	@Override
	public Collection<String> onTabComplete(CommandSender sender, String commandName, String[] args) {
		return new ArrayList<>();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!api.hasPermission(sender, "Magic.commands.getrp")) {
			sendNoPermission(sender);
			return true;
		}
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "This command may only be used in-game");
			return true;
		}

		api.getController().sendResourcePack((Player)sender);
		return true;
	}
}
