package com.elmakers.mine.bukkit.magic.command;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.utility.Messages;
import org.bukkit.inventory.ItemStack;

public abstract class MagicTabExecutor implements TabExecutor {
	protected MagicAPI api;
	
	public MagicTabExecutor(MagicAPI api) {
		this.api = api;
	}
	
	public abstract Collection<String> onTabComplete(CommandSender sender, String commandName, String[] args);
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		String completeCommand = args.length > 0 ? args[args.length - 1] : "";
	
		completeCommand = completeCommand.toLowerCase();
		Collection<String> allOptions = onTabComplete(sender, command.getName(), args);
		List<String>options = new ArrayList<String>();
		for (String option : allOptions) {
			String lowercase = option.toLowerCase();
			if (lowercase.startsWith(completeCommand)) {
				options.add(option);
			}
		}
        Collections.sort(options);
		
		return options;
	}

	
	public static String getMagicVersion() {
        String result = "Unknown-Version";

        InputStream stream = MagicTabExecutor.class.getClassLoader().getResourceAsStream("META-INF/maven/com.elmakers.mine.bukkit.plugins/Magic/pom.properties");
        Properties properties = new Properties();

        if (stream != null) {
            try {
                properties.load(stream);

                result = properties.getProperty("version");
            } catch (IOException ex) {
                Bukkit.getLogger().warning("Could not get Magic version");
            }
        }

        return result;
    }
	
	protected void sendNoPermission(CommandSender sender)
	{
        if (sender != null) sender.sendMessage(ChatColor.RED + "You are not allowed to use that command.");
	}

	protected void addIfPermissible(CommandSender sender, Collection<String> options, String permissionPrefix, String option)
	{
		if (api.hasPermission(sender, permissionPrefix + option))
		{
			options.add(option);
		}
	}

	protected void addIfPermissible(CommandSender sender, Collection<String> options, String permissionPrefix, String option, boolean defaultPermission)
	{
		if (api.hasPermission(sender, permissionPrefix + option, defaultPermission))
		{
			options.add(option);
		}
	}

	public boolean giveWand(CommandSender sender, Player player, String wandKey, boolean quiet, boolean giveItem, boolean giveValue, boolean showWorth)
	{
		Mage mage = api.getMage(player);
		Wand currentWand =  mage.getActiveWand();
		if (currentWand != null) {
			currentWand.closeInventory();
		}
	
		Wand wand = api.createWand(wandKey);
		if (wand != null) {
            if (giveItem) {
                api.giveItemToPlayer(player, wand.getItem());
                if (sender != player && !quiet) {
                    sender.sendMessage("Gave wand " + wand.getName() + " to " + player.getName());
                }
            }
            if (showWorth) {
                showWorth(sender, wand.getItem());
            }
		} else {
			if (!quiet) {
                if (wandKey == null) {
                    wandKey = "(default)";
                }
                sender.sendMessage(api.getMessages().getParameterized("wand.unknown_template", "$name", wandKey));
            }
			return false;
		}
		return true;
	}

    protected void showWorth(CommandSender sender, ItemStack item)
    {
       if (api.isWand(item) || (api.isUpgrade(item))) {
            Wand wand = api.getWand(item);
           if (wand == null) {
               sender.sendMessage("I'm not sure what that's worth, sorry!");
               return;
           }
           sender.sendMessage(ChatColor.AQUA + "WIP: Wand " + ChatColor.GOLD + wand.getName()
                   + ChatColor.AQUA + " is worth " + ChatColor.GREEN + wand.getWorth());
       } else if (api.isSpell(item)) {
           SpellTemplate template = api.getSpellTemplate(api.getSpell(item));
           if (template == null) {
               sender.sendMessage("I'm not sure what that's worth, sorry!");
               return;
           }
           sender.sendMessage(ChatColor.AQUA + "Spell " + ChatColor.GOLD + template.getName()
                + ChatColor.AQUA + " is worth " + ChatColor.GREEN + template.getWorth());
       } else if (api.isBrush(item)) {
           String materialBrush = api.getBrush(item);
           if (materialBrush == null) {
               sender.sendMessage("I'm not sure what that's worth, sorry!");
               return;
           }
           // TODO: Config-driven!
           int brushWorth = 500;
           sender.sendMessage(ChatColor.AQUA + "WIP: Brush " + ChatColor.GOLD + materialBrush
                   + ChatColor.AQUA + " is worth " + ChatColor.GREEN + brushWorth);
       } else {
           sender.sendMessage("I'm not sure what that's worth, sorry!");
       }
    }
}
