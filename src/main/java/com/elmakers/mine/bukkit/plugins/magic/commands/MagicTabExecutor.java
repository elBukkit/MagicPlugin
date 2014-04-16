package com.elmakers.mine.bukkit.plugins.magic.commands;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.block.MaterialBrush;
import com.elmakers.mine.bukkit.utilities.Messages;

public abstract class MagicTabExecutor implements TabExecutor {
	protected MagicAPI api;
	
	public MagicTabExecutor(MagicAPI api) {
		this.api = api;
	}
	
	public abstract List<String> onTabComplete(CommandSender sender, String commandName, String[] args);
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		String completeCommand = args.length > 0 ? args[args.length - 1] : "";
	
		completeCommand = completeCommand.toLowerCase();
		List<String> allOptions = onTabComplete(sender, command.getName(), args);
		List<String>options = new ArrayList<String>();
		for (String option : allOptions) {
			String lowercase = option.toLowerCase();
			if (lowercase.startsWith(completeCommand)) {
				options.add(option);
			}
		}
		
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
	
	public void addMaterials(Collection<String> options) {
		Material[] materials = Material.values();
		for (Material material : materials) {
			// Only show blocks
			if (material.isBlock()) {
				options.add(material.name().toLowerCase());
			}
		}
		
		// Add special materials
		for (String brushName : MaterialBrush.SPECIAL_MATERIAL_KEYS) {
			options.add(brushName.toLowerCase());
		}
		
		// Add schematics
		Collection<String> schematics = api.getSchematicNames();
		for (String schematic : schematics) {
			options.add("schematic:" + schematic);
		}
	}
	
	protected void sendNoPermission(CommandSender sender)
	{
		sender.sendMessage(ChatColor.RED + "You are not allowed to use that command.");
	}

	protected void addIfPermissible(CommandSender sender, List<String> options, String permissionPrefix, String option)
	{
		if (api.hasPermission(sender, permissionPrefix + option))
		{
			options.add(option);
		}
	}

	protected void addIfPermissible(CommandSender sender, List<String> options, String permissionPrefix, String option, boolean defaultPermission)
	{
		if (api.hasPermission(sender, permissionPrefix + option, defaultPermission))
		{
			options.add(option);
		}
	}

	public boolean onGiveWand(CommandSender sender, Player player, String wandKey)
	{
		Mage mage = api.getMage(player);
		Wand currentWand =  mage.getActiveWand();
		if (currentWand != null) {
			currentWand.closeInventory();
		}
	
		Wand wand = api.createWand(wandKey);
		if (wand != null) {
			api.giveItemToPlayer(player, wand.getItem());
			if (sender != player) {
				sender.sendMessage("Gave wand " + wand.getName() + " to " + player.getName());
			}
		} else {
			sender.sendMessage(Messages.getParameterized("wand.unknown_template", "$name", wandKey));
		}
		return true;
	}
}
