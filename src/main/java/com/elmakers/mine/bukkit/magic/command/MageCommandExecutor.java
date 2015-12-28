package com.elmakers.mine.bukkit.magic.command;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.spell.Spell;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class MageCommandExecutor extends MagicMapExecutor {
	public MageCommandExecutor(MagicAPI api) {
		super(api);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0)
		{
			if (api.hasPermission(sender, "Magic.commands.mage")) {
				sendNoPermission(sender);
				return true;
			}
			return false;
		}
		
		String subCommand = args[0];
		if (sender instanceof Player)
		{
			if (!api.hasPermission(sender, "Magic.commands.mage." + subCommand)) {
				sendNoPermission(sender);
				return true;
			}
		}

		Player player = null;
		int argStart = 1;

		if (sender instanceof Player) {
			if (args.length > 1)
			{
				player = Bukkit.getPlayer(args[1]);
			}
			if (player == null)
			{
				player = (Player)sender;
			}
			else
			{
				argStart = 2;
			}
		} else {
			if (args.length <= 1) {
				sender.sendMessage("Must specify a player name");
				return true;
			}
			argStart = 2;
			player = Bukkit.getPlayer(args[1]);
			if (player == null) {
				sender.sendMessage("Can't find player " + args[1]);
				return true;
			}
			if (!player.isOnline()) {
				sender.sendMessage("Player " + args[1] + " is not online");
				return true;
			}
		}

		String[] args2 = Arrays.copyOfRange(args, argStart, args.length);

		if (subCommand.equalsIgnoreCase("check"))
		{
			return onMagicCheck(sender, player, args2);
		}
        if (subCommand.equalsIgnoreCase("delete"))
        {
            return onMagicDelete(sender, player);
        }
		if (subCommand.equalsIgnoreCase("debug"))
		{
			return onMagicDebug(sender, player, args2);
		}
		if (subCommand.equalsIgnoreCase("describe"))
		{
			return onMagicDescribe(sender, player, args2);
		}
		if (subCommand.equalsIgnoreCase("configure"))
		{
			return onMagicConfigure(sender, player, args2);
		}

		sender.sendMessage("Unknown mage command: " + subCommand);
		return true;
	}
	
	@Override
	public Collection<String> onTabComplete(CommandSender sender, String commandName, String[] args) {
		List<String> options = new ArrayList<String>();
		if (args.length == 1) {
            addIfPermissible(sender, options, "Magic.commands.mage.", "describe");
            addIfPermissible(sender, options, "Magic.commands.mage.", "configure");
            addIfPermissible(sender, options, "Magic.commands.mage.", "check");
            addIfPermissible(sender, options, "Magic.commands.mage.", "debug");
			addIfPermissible(sender, options, "Magic.commands.mage.", "delete");
		} else if (args.length == 2) {
			options.addAll(api.getPlayerNames());
		} else if (args.length == 3) {
			if (args[0].equalsIgnoreCase("configure") || args[0].equalsIgnoreCase("describe")) {
                Player player = Bukkit.getPlayer(args[1]);
                if (player != null) {
                    Mage mage = api.getMage(player);
                    ConfigurationSection data = mage.getData();
                    options.addAll(data.getKeys(false));
                }
            }
		}
		return options;
	}

    public boolean onMagicCheck(CommandSender sender, Player player, String[] args)
    {
        Mage mage = api.getMage(player);
		mage.debugPermissions(sender, null);
        return true;
    }

    public boolean onMagicDelete(CommandSender sender, Player player)
    {
        api.getController().deleteMage(player.getUniqueId().toString());
        sender.sendMessage(ChatColor.RED + "Deleted player " + player.getName());
        return true;
    }

    public boolean onMagicDebug(CommandSender sender, Player player, String[] args)
    {
        Mage mage = api.getMage(player);
        if (args.length > 0) {
            try {
                int level = Integer.parseInt(args[0]);
                mage.setDebugLevel(level);
				if (level > 0) {
					mage.setDebugger(sender);
				} else {
					mage.setDebugger(null);
				}
                sender.sendMessage(ChatColor.GOLD + "Setting debug level for  " + ChatColor.AQUA + player.getDisplayName() + ChatColor.GOLD + " to " + ChatColor.GREEN + Integer.toString(level));
            } catch (Exception ex) {
                sender.sendMessage("Expecting integer, got: " + args[0]);
            }
            return true;
        }
        if (mage.getDebugLevel() > 0) {
            sender.sendMessage(ChatColor.GOLD + "Disabling debug for " + ChatColor.AQUA + player.getDisplayName());
            mage.setDebugLevel(0);
			mage.setDebugger(null);
        } else {
            sender.sendMessage(ChatColor.AQUA + "Enabling debug for " + ChatColor.AQUA + player.getDisplayName());
            mage.setDebugLevel(1);
			mage.setDebugger(sender);
        }
        return true;
    }

    public boolean onMagicDescribe(CommandSender sender, Player player, String[] args)
    {
        Mage mage = api.getMage(player);
        ConfigurationSection data = mage.getData();
        if (args != null && args.length > 0)
        {
            if (args[0].equals("*"))
            {
                sender.sendMessage(ChatColor.GOLD + "Mage data for " + ChatColor.AQUA + player.getDisplayName() + ChatColor.GOLD + ": ");
                Collection<Spell> spells = mage.getSpells();
                if (spells.size() == 0) {
                    sender.sendMessage(ChatColor.RED + "No spell casts!");
                    return true;
                }
                for (Spell spell : spells) {
                    sender.sendMessage(ChatColor.LIGHT_PURPLE + spell.getName() + ChatColor.AQUA + " Cast Count: " + ChatColor.GOLD + spell.getCastCount());
                }
                return true;
            }
            Spell spell = mage.getSpell(args[0]);
            if (spell != null)
            {
                sender.sendMessage(ChatColor.GOLD + "Mage data for " + ChatColor.AQUA + player.getDisplayName() + ChatColor.GOLD + ": " + ChatColor.LIGHT_PURPLE + spell.getName());
                sender.sendMessage(ChatColor.AQUA + " Cast Count: " + ChatColor.GOLD + spell.getCastCount());
                return true;
            }
            ConfigurationSection subSection = data.getConfigurationSection(args[0]);
            if (subSection == null) {
                sender.sendMessage(ChatColor.RED + "Unknown subsection: " + args[0]);
                return true;
            }
            data = subSection;
        }
        Collection<String> keys = data.getKeys(false);
        sender.sendMessage(ChatColor.GOLD + "Mage data for " + ChatColor.AQUA + player.getDisplayName());
        for (String key : keys) {
            if (data.isConfigurationSection(key)) {
                ConfigurationSection subSection = data.getConfigurationSection(key);
                sender.sendMessage(ChatColor.AQUA + " " + key + ChatColor.DARK_AQUA + " (" + subSection.getKeys(true).size() + " items)");
            } else {
                String value = data.getString(key);
                if (value != null) {
                    sender.sendMessage(ChatColor.AQUA + " " + key + ChatColor.DARK_AQUA + " (" + value + ")");
                } else {
                    sender.sendMessage(ChatColor.AQUA + " " + key);
                }
            }
        }
        return true;
    }

    public boolean onMagicConfigure(CommandSender sender, Player player, String[] args)
    {
        Mage mage = api.getMage(player);
        if (args.length == 1)
        {
            ConfigurationSection data = mage.getData();
            String key = args[0];
            if (!data.contains(key)) {
                sender.sendMessage(ChatColor.RED + "No data found with key " + ChatColor.AQUA + key + ChatColor.RED + " for " + ChatColor.DARK_AQUA + player.getDisplayName());
                return true;
            }
            data.set(key, null);
            sender.sendMessage(ChatColor.GOLD + "Removed data for key " + ChatColor.AQUA + key + ChatColor.GOLD  + " for " + ChatColor.DARK_AQUA + player.getDisplayName());
            return true;
        }
        if (args.length != 2)
        {
            return false;
        }
        if (args[0].equals("*"))
        {
            long value = 0;
            try {
                value = Long.parseLong(args[1]);
            } catch (Exception ex) {
                sender.sendMessage(ChatColor.RED + "Cast count must be a number");
                return true;
            }
            Collection<Spell> spells = mage.getSpells();
            for (Spell spell : spells)
            {
                spell.setCastCount(value);
            }
            sender.sendMessage(ChatColor.GOLD + "Set all spell cast counts to " + ChatColor.AQUA + value + ChatColor.GOLD + " for " + ChatColor.DARK_AQUA + player.getDisplayName());
            return true;
        }
        Spell spell = mage.getSpell(args[0]);
        if (spell != null)
        {
            long value = 0;
            try {
                value = Long.parseLong(args[1]);
            } catch (Exception ex) {
                sender.sendMessage(ChatColor.RED + "Cast count must be a number");
                return true;
            }
            spell.setCastCount(value);
            sender.sendMessage(ChatColor.GOLD + "Set " + ChatColor.AQUA + spell.getName() + ChatColor.GOLD + " cast count to " + ChatColor.AQUA + value + ChatColor.GOLD + " for " + ChatColor.DARK_AQUA + player.getDisplayName());
            return true;
        }

        ConfigurationSection data = mage.getData();
        String key = args[0];
        String value = args[1];
        data.set(key, value);
        sender.sendMessage(ChatColor.GOLD + "Set " + ChatColor.AQUA + key + ChatColor.GOLD + " to " + ChatColor.AQUA + value + ChatColor.GOLD + " for " + ChatColor.DARK_AQUA + player.getDisplayName());
        return true;
    }
}
