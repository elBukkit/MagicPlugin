/**
 * 
 */
package com.elmakers.mine.bukkit.magic.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;

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
			if (args.length < 1) {
                if (sender != null) sender.sendMessage("Usage: /castp [player] [spell] <parameters>");
				return true;
			}
            String playerName = args[0];
            if (playerName.contains(":")) {
                String[] pieces = StringUtils.split(playerName, ":");
                String mageId = pieces[0];
                String mageName = (pieces.length > 0) ? pieces[1] : mageId;

                MageController controller = api.getController();
                Mage mage = controller.getMage(mageId, mageName);
                String[] castParameters = Arrays.copyOfRange(args, 1, args.length);
                if (castParameters.length < 1) {
                    if (sender != null) sender.sendMessage("Invalid command line, expecting more parameters");
                    return false;
                }

                String spellName = castParameters[0];
                Spell spell = mage.getSpell(spellName);
                if (spell == null) {
                    if (sender != null) sender.sendMessage("Unknown spell " + spellName);
                    return false;
                }

                String[] parameters = new String[castParameters.length - 1];
                for (int i = 1; i < castParameters.length; i++)
                {
                    parameters[i - 1] = castParameters[i];
                }

                if (sender != null) sender.sendMessage("Casting " + spell.getName() + " on " + mageName);
                return spell.cast(parameters);
            }

            Player player = Bukkit.getPlayer(playerName);
            if (player == null) {
                if (sender != null) sender.sendMessage("Can't find player " + playerName);
                return true;
            }
            if (!player.isOnline()) {
                if (sender != null) sender.sendMessage("Player " + playerName + " is not online");
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

	public boolean processCastCommand(CommandSender sender, Entity entity, String[] castParameters)
	{
		if (castParameters.length < 1) return false;

		String spellName = castParameters[0];
		String[] parameters = new String[castParameters.length - 1];
		for (int i = 1; i < castParameters.length; i++)
		{
			parameters[i - 1] = castParameters[i];
		}
		api.cast(spellName, parameters, sender, entity);
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
			Collection<SpellTemplate> spellList = api.getSpellTemplates();
			for (SpellTemplate spell : spellList) {
				addIfPermissible(sender, options, "Magic." + commandName+ ".", spell.getKey(), true);
			}
		}
		
		if (args.length > 1)
		{
			String spellName = args[0];
			SpellTemplate spell = api.getSpellTemplate(spellName);
			if (spell != null) {
				if (args.length % 2 == 0 || args.length < 2) {
					spell.getParameters(options);
				} else {
					spell.getParameterOptions(options, args[args.length - 2]);
				}
			}
		}
		
		Collections.sort(options);
		return options;
	}

}
