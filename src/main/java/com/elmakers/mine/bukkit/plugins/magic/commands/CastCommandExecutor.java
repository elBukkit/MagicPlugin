/**
 * 
 */
package com.elmakers.mine.bukkit.plugins.magic.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.spell.Spell;

public class CastCommandExecutor extends MagicTabExecutor {
	
	public CastCommandExecutor(MagicAPI api) {
		super(api);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		if (commandLabel.equalsIgnoreCase("castp"))
		{
			if (!api.hasPermission(sender, "Magic.commands.castp")) {
				sendNoPermission(sender);
				return true;
			}
			if (args.length == 0) {
				sender.sendMessage("Usage: /castp [player] [spell] <parameters>");
				return true;
			}
			Player player = Bukkit.getPlayer(args[0]);
			if (player == null) {
				sender.sendMessage("Can't find player " + args[0]);
				return true;
			}
			if (!player.isOnline()) {
				sender.sendMessage("Player " + args[0] + " is not online");
				return true;
			}
			String[] args2 = Arrays.copyOfRange(args, 1, args.length);
			return processCastCommand(sender, player, args2);
		}

		if (commandLabel.equalsIgnoreCase("cast"))
		{
			if (!api.hasPermission(sender, "Magic.commands.cast")) {
				sendNoPermission(sender);
				return true;
			}
			Player player = null;
			if (sender instanceof Player) {
				player = (Player)sender;
			}
			return processCastCommand(sender, player, args);
		}
		
		return false;
	}

	public boolean processCastCommand(CommandSender sender, Player player, String[] castParameters)
	{
		if (castParameters.length < 1) return false;

		String spellName = castParameters[0];
		String[] parameters = new String[castParameters.length - 1];
		for (int i = 1; i < castParameters.length; i++)
		{
			parameters[i - 1] = castParameters[i];
		}
		api.cast(spellName, parameters, sender, player);
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String commandName, String[] args) {
		List<String> options = new ArrayList<String>();
		
		if (commandName.equalsIgnoreCase("castp")) 
		{
			if (args.length == 1) {
				options.addAll(api.getPlayerNames());
				Collections.sort(options);
				return options;
			} else if (args.length > 1) {
				args = Arrays.copyOfRange(args, 1, args.length);
			}
		}
		
		if (args.length == 1) {
			Collection<Spell> spellList = api.getSpells();
			for (Spell spell : spellList) {
				addIfPermissible(sender, options, "Magic." + commandName+ ".", spell.getKey(), true);
			}
		}
		
		if (args.length > 1)
		{
			// TODO, only do this on even numbers, call to spell to get options for odd ones
			String spellName = args[0];
			Spell spell = api.getSpell(spellName);
			if (spell != null) {
				spell.getParameters(options);
			}
		}
		
		Collections.sort(options);
		return options;
	}

}
