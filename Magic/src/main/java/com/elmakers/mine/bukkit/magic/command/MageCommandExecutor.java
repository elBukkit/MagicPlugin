package com.elmakers.mine.bukkit.magic.command;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.magic.MagicProperties;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.magic.BaseMagicConfigurable;
import com.elmakers.mine.bukkit.utility.DeprecatedUtils;

import com.elmakers.mine.bukkit.utility.InventoryUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class MageCommandExecutor extends MagicConfigurableExecutor {
	public MageCommandExecutor(MagicAPI api) {
		super(api);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0)
		{
			if (!api.hasPermission(sender, "Magic.commands.mage")) {
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
				player = DeprecatedUtils.getPlayer(args[1]);
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
			player = DeprecatedUtils.getPlayer(args[1]);
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
			return onMageCheck(sender, player, args2);
		}
        if (subCommand.equalsIgnoreCase("reset"))
        {
            return onMageReset(sender, player);
        }
		if (subCommand.equalsIgnoreCase("debug"))
		{
			return onMageDebug(sender, player, args2);
		}
		if (subCommand.equalsIgnoreCase("getdata"))
		{
			return onMageGetData(sender, player, args2);
		}
		if (subCommand.equalsIgnoreCase("setdata"))
		{
			return onMageSetData(sender, player, args2);
		}
        if (subCommand.equalsIgnoreCase("unbind"))
        {
            onMageUnbind(sender, player);
            return true;
        }
        if (subCommand.equalsIgnoreCase("configure"))
        {
            onMageConfigure(sender, player, args2, false);
            return true;
        }
        if (subCommand.equalsIgnoreCase("upgrade"))
        {
            onMageConfigure(sender, player, args2, true);
            return true;
        }

        if (subCommand.equalsIgnoreCase("describe"))
        {
            onMageDescribe(sender, player, args2);
            return true;
        }

		sender.sendMessage("Unknown mage command: " + subCommand);
		return true;
	}
	
	@Override
	public Collection<String> onTabComplete(CommandSender sender, String commandName, String[] args) {
		List<String> options = new ArrayList<>();
		if (args.length == 1) {
            addIfPermissible(sender, options, "Magic.commands.mage.", "configure");
            addIfPermissible(sender, options, "Magic.commands.mage.", "describe");
            addIfPermissible(sender, options, "Magic.commands.mage.", "upgrade");
            addIfPermissible(sender, options, "Magic.commands.mage.", "getdata");
            addIfPermissible(sender, options, "Magic.commands.mage.", "setdata");
            addIfPermissible(sender, options, "Magic.commands.mage.", "check");
            addIfPermissible(sender, options, "Magic.commands.mage.", "debug");
			addIfPermissible(sender, options, "Magic.commands.mage.", "reset");
            addIfPermissible(sender, options, "Magic.commands.mage.", "unbind");
		} else if (args.length == 2) {
			options.addAll(api.getPlayerNames());
            if (args[0].equalsIgnoreCase("configure") || args[0].equalsIgnoreCase("describe") || args[0].equalsIgnoreCase("upgrade")) {
                for (String key : BaseMagicConfigurable.PROPERTY_KEYS) {
                    options.add(key);
                }
            }
		} else if (args.length == 3) {
			if (args[0].equalsIgnoreCase("setdata") || args[0].equalsIgnoreCase("getdata")) {
                Player player = DeprecatedUtils.getPlayer(args[1]);
                if (player != null) {
                    Mage mage = api.getMage(player);
                    ConfigurationSection data = mage.getData();
                    options.addAll(data.getKeys(false));
                }
            }
		}
		return options;
	}

    public boolean onMageCheck(CommandSender sender, Player player, String[] args)
    {
        Mage mage = api.getMage(player);
		mage.debugPermissions(sender, null);
        return true;
    }

    public boolean onMageReset(CommandSender sender, Player player)
    {
        api.getController().deleteMage(player.getUniqueId().toString());
        sender.sendMessage(ChatColor.RED + "Reset player " + player.getName());
        return true;
    }

    public boolean onMageDebug(CommandSender sender, Player player, String[] args)
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

    public boolean onMageGetData(CommandSender sender, Player player, String[] args)
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
                sender.sendMessage(ChatColor.RED + "Unknown subsection or spell: " + args[0]);
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

    public boolean onMageSetData(CommandSender sender, Player player, String[] args)
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

    public boolean onMageUnbind(CommandSender sender, Player player)
    {
        Mage mage = api.getMage(player);
        mage.unbindAll();

        mage.sendMessage(api.getMessages().get("wand.unboundall"));
        if (sender != player) {
            sender.sendMessage(api.getMessages().getParameterized("wand.player_unboundall", "$name", player.getName()));
        }
        return true;
    }

    public boolean onMageConfigure(CommandSender sender, Player player, String[] parameters, boolean safe)
    {
        Mage mage = api.getMage(player);
        return onConfigure("mage", mage.getProperties(), sender, player, parameters, safe);
    }

    public boolean onMageDescribe(CommandSender sender, Player player, String[] parameters) {
        // Force-save wand data so it is up to date
        Mage mage = api.getMage(player);
        MagicProperties mageProperties = mage.getProperties();

        if (parameters.length == 0) {
            sender.sendMessage(ChatColor.BLUE + "Use " + ChatColor.AQUA + "/mage describe <property>" + ChatColor.BLUE + " for specific properties");
            mageProperties.describe(sender);
        } else {
            Object property = mageProperties.getProperty(parameters[0]);
            if (property == null) {
                sender.sendMessage(ChatColor.DARK_AQUA + parameters[0] + ChatColor.GRAY + ": " + ChatColor.RED + "(Not Set)");
            } else {
                sender.sendMessage(ChatColor.DARK_AQUA + parameters[0] + ChatColor.GRAY + ": " + ChatColor.WHITE + InventoryUtils.describeProperty(property));
            }
        }

        return true;
    }
}
