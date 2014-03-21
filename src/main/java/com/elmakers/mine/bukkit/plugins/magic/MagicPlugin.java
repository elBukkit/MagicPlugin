package com.elmakers.mine.bukkit.plugins.magic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.elmakers.mine.bukkit.blocks.BlockData;
import com.elmakers.mine.bukkit.blocks.MaterialBrush;
import com.elmakers.mine.bukkit.plugins.magic.populator.MagicRunnable;
import com.elmakers.mine.bukkit.plugins.magic.populator.WandChestRunnable;
import com.elmakers.mine.bukkit.plugins.magic.populator.WandCleanupRunnable;
import com.elmakers.mine.bukkit.plugins.magic.wand.LostWand;
import com.elmakers.mine.bukkit.plugins.magic.wand.Wand;
import com.elmakers.mine.bukkit.utilities.Messages;
import com.elmakers.mine.bukkit.utilities.URLMap;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class MagicPlugin extends JavaPlugin
{	
	/*
	 * Public API
	 */
	public MagicController getController()
	{
		return controller;
	}

	/*
	 * Plugin interface
	 */

	public void onEnable() 
	{
		if (controller == null) {
			controller = new MagicController(this);
		}
		initialize();

		BlockData.setServer(getServer());
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(controller, this);
	}

	protected void initialize()
	{
		controller.initialize();
	}
	
	protected void checkRunningTask()
	{
		if (runningTask != null && runningTask.isFinished()) {
			runningTask = null;
		}
	}

	@SuppressWarnings("deprecation")
	protected void handleWandCommandTab(List<String> options, Mage player, CommandSender sender, Command cmd, String alias, String[] args)
	{
		if (args.length == 0) {
			return;
		}
		if (args.length == 1) {
			addIfPermissible(sender, options, "Magic.commands." + cmd + ".", "add");
			addIfPermissible(sender, options, "Magic.commands." + cmd + ".", "remove");
			addIfPermissible(sender, options, "Magic.commands." + cmd + ".", "name");
			addIfPermissible(sender, options, "Magic.commands." + cmd + ".", "fill");
			addIfPermissible(sender, options, "Magic.commands." + cmd + ".", "configure");
			addIfPermissible(sender, options, "Magic.commands." + cmd + ".", "organize");
			addIfPermissible(sender, options, "Magic.commands." + cmd + ".", "combine");
			addIfPermissible(sender, options, "Magic.commands." + cmd + ".", "upgrade");
			addIfPermissible(sender, options, "Magic.commands." + cmd + ".", "describe");
			Collection<String> allWands = Wand.getWandKeys();
			for (String wandKey : allWands) {
				addIfPermissible(sender, options, "Magic.commands." + cmd.getName() + ".wand.", wandKey, true);
			}
			return;
		}
		
		if (args.length == 2) {
			String subCommand = args[0];
			String subCommandPNode = "Magic.commands." + cmd.getName() + "." + subCommand;
			
			if (!controller.hasPermission(sender, subCommandPNode)) {
				return;
			}
			
			subCommandPNode += ".";
			
			if (subCommand.equalsIgnoreCase("add")) {
				List<Spell> spellList = controller.getAllSpells();
				for (Spell spell : spellList) {
					addIfPermissible(sender, options, subCommandPNode, spell.getKey(), true);
				}
				addIfPermissible(sender, options, subCommandPNode, "material", true);
			}
			
			if (subCommand.equalsIgnoreCase("configure")) {
				for (String key : Wand.PROPERTY_KEYS) {
					options.add(key);
				}
			}
			
			if (subCommand.equalsIgnoreCase("remove")) {
				Wand activeWand = player == null ? null : player.getActiveWand();
				if (activeWand != null) {
					Collection<String> spellNames = activeWand.getSpells();
					for (String spellName : spellNames) {
						options.add(spellName);
					}
					
					options.add("material");
				}
			}
			
			if (subCommand.equalsIgnoreCase("combine")) {
				Collection<String> allWands = Wand.getWandKeys();
				for (String wandKey : allWands) {
					addIfPermissible(sender, options, "Magic.commands." + cmd.getName() + ".combine.", wandKey, true);
				}
			}
		}
		
		if (args.length == 3)
		{
			String subCommand = args[0];
			String subCommand2 = args[1];
			
			String subCommandPNode = "Magic.commands." + cmd.getName() + "." + subCommand + "." + subCommand2;
			
			if (!controller.hasPermission(sender, subCommandPNode, true)) {
				return;
			}
			
			if (subCommand.equalsIgnoreCase("remove") && subCommand2.equalsIgnoreCase("material")) {
				Wand activeWand = player == null ? null : player.getActiveWand();
				if (activeWand != null) {
					Collection<String> materialNames = activeWand.getMaterialKeys();
					for (String materialName : materialNames) {
						options.add(materialName);
					}
				}
			}
			
			if (subCommand.equalsIgnoreCase("add") && subCommand2.equalsIgnoreCase("material")) {
				Material[] materials = Material.values();
				for (Material material : materials) {
					// Kind of a hack..
					if (material.getId() < 256) {
						options.add(material.name().toLowerCase());
					}
				}
			}
		}
		
		// TODO : Custom completion for configure, upgrade
	}

	protected void handleCastCommandTab(List<String> options, CommandSender sender, Command cmd, String alias, String[] args)
	{
		if (args.length == 1) {
			List<Spell> spellList = controller.getAllSpells();
			for (Spell spell : spellList) {
				addIfPermissible(sender, options, "Magic." + cmd.getName() + ".", spell.getKey(), true);
			}
			
			return;
		}
		
		// TODO : Custom completion for spell parameters
	}
	
	protected void addIfPermissible(CommandSender sender, List<String> options, String permissionPrefix, String option, boolean defaultValue)
	{
		if (controller.hasPermission(sender, permissionPrefix + option, defaultValue))
		{
			options.add(option);
		}
	}
	
	protected void addIfPermissible(CommandSender sender, List<String> options, String permissionPrefix, String option)
	{
		addIfPermissible(sender, options, permissionPrefix, option, false);
	}
	
	@EventHandler
	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args)
	{
		Mage mage = null;
		if (sender instanceof Player) {
			mage = controller.getMage((Player)sender);
		}
		String completeCommand = args.length > 0 ? args[args.length - 1] : "";
		List<String> options = new ArrayList<String>();
		if (cmd.getName().equalsIgnoreCase("magic"))
		{
			if (args.length == 1) {
				addIfPermissible(sender, options, "Magic.commands.", "populate");
				addIfPermissible(sender, options, "Magic.commands.", "generate");
				addIfPermissible(sender, options, "Magic.commands.", "search");
				addIfPermissible(sender, options, "Magic.commands.", "clean");
				addIfPermissible(sender, options, "Magic.commands.", "clearcache");
				addIfPermissible(sender, options, "Magic.commands.", "cancel");
				addIfPermissible(sender, options, "Magic.commands.", "load");
				addIfPermissible(sender, options, "Magic.commands.", "save");
				addIfPermissible(sender, options, "Magic.commands.", "commit");
				addIfPermissible(sender, options, "Magic.commands.", "list");
			} else if (args.length == 2) {
				if (args[1].equalsIgnoreCase("list")) {
					addIfPermissible(sender, options, "Magic.commands.list", "maps");
					addIfPermissible(sender, options, "Magic.commands.list", "wands");
				}
			}
		}
		else if (cmd.getName().equalsIgnoreCase("wand")) 
		{
			handleWandCommandTab(options, mage, sender, cmd, alias, args);
		}
		else if (cmd.getName().equalsIgnoreCase("wandp")) 
		{
			if (args.length == 1) {
				options.addAll(MagicController.getPlayerNames());
			} else if (args.length > 1) {
				String[] args2 = Arrays.copyOfRange(args, 1, args.length);
				handleWandCommandTab(options, mage, sender, cmd, alias, args2);
			}
		}
		else if (cmd.getName().equalsIgnoreCase("cast")) 
		{
			handleCastCommandTab(options, sender, cmd, alias, args);
		}
		else if (cmd.getName().equalsIgnoreCase("castp")) 
		{
			if (args.length == 1) {
				options.addAll(MagicController.getPlayerNames());
			} else if (args.length > 1) {
				String[] args2 = Arrays.copyOfRange(args, 1, args.length);
				handleCastCommandTab(options, sender, cmd, alias, args2);
			}
		}
		
		if (completeCommand.length() > 0) {
			completeCommand = completeCommand.toLowerCase();
			List<String> allOptions = options;
			options = new ArrayList<String>();
			for (String option : allOptions) {
				String lowercase = option.toLowerCase();
				if (lowercase.startsWith(completeCommand)) {
					options.add(option);
				}
			}
		}
		
		Collections.sort(options);
		
		return options;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		if (commandLabel.equalsIgnoreCase("magic") && args.length > 0)
		{
			String subCommand = args[0];
			if (sender instanceof Player)
			{
				if (!controller.hasPermission((Player)sender, "Magic.commands.magic." + subCommand)) return false;
			}
			if (subCommand.equalsIgnoreCase("save"))
			{
				controller.save();
				URLMap.save();
				sender.sendMessage("Data saved.");
				return true;
			}
			if (subCommand.equalsIgnoreCase("load"))
			{		
				controller.loadConfiguration();
				URLMap.loadConfiguration();
				sender.sendMessage("Configuration reloaded.");
				return true;
			}
			if (subCommand.equalsIgnoreCase("clearcache"))
			{		
				controller.clearCache();
				URLMap.clearCache();
				sender.sendMessage("Image map and schematic caches cleared.");
				return true;
			}
			if (subCommand.equalsIgnoreCase("commit"))
			{
				if (controller.commitAll()) {
					sender.sendMessage("All changes committed");
				} else {
					sender.sendMessage("Nothing to commit");
				}
				return true;
			}
			if (subCommand.equalsIgnoreCase("list"))
			{
				String usage = "Usage: magic list <wands [player]|maps [keyword]>";
				String listCommand = "";
				if (args.length > 1)
				{
					listCommand = args[1];
					if (!controller.hasPermission(sender, "Magic.commands.magic." + subCommand + "." + listCommand)) return false;
				}
				
				if (listCommand.equalsIgnoreCase("wands")) {
					String owner = "";
					if (args.length > 2) {
						owner = args[2];
					}
					Collection<LostWand> lostWands = controller.getLostWands();
					int shown = 0;
					for (LostWand lostWand : lostWands) {
						Location location = lostWand.getLocation();
						if (owner.length() > 0 && !owner.equalsIgnoreCase	(lostWand.getOwner())) {
							continue;
						}
						shown++;
						sender.sendMessage(ChatColor.AQUA + lostWand.getName() + ChatColor.WHITE + " (" + lostWand.getOwner() + ") @ " + ChatColor.BLUE + location.getWorld().getName() + " " +
								location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ());
					}
					
					sender.sendMessage(shown + " lost wands found" + (owner.length() > 0 ? " for " + owner : ""));
					return true;
				} else if (listCommand.equalsIgnoreCase("maps")) {
					String keyword = "";
					if (args.length > 2) {
						keyword = args[2];
					}

					int shown = 0;
					Set<Entry<Short, URLMap>> allMaps = URLMap.getAll();
					for (Entry<Short, URLMap> mapRecord : allMaps) {
						Short mapId = mapRecord.getKey();
						URLMap map = mapRecord.getValue();
						if (map == null || mapId == null) continue;
						
						if (map.matches(keyword)) {
							shown++;
							String name = map.getName();
							name = (name == null ? "(None)" : name);
							sender.sendMessage(ChatColor.AQUA + "" + mapId + ChatColor.WHITE + ": " + 
									name + " => " + ChatColor.GRAY + map.getURL());
						}
					}
					if (shown == 0) {
						sender.sendMessage("No maps found" + (keyword.length() > 0 ? " matching " + keyword : "") + ", use /castp <player> camera [url|player] [...]");
					} else {
						sender.sendMessage(shown + " maps found matching " + keyword);
					}
					return true;
				}
			
				sender.sendMessage(usage);
				return true;
			}
			if (subCommand.equalsIgnoreCase("populate") || subCommand.equalsIgnoreCase("search") || subCommand.equalsIgnoreCase("generate"))
			{   
				checkRunningTask();
				if (runningTask != null) {
					sender.sendMessage("Cancel current job first");
					return true;
				}
				World world = null;
				int ymax = 50;
				if (sender instanceof Player) {
					world = ((Player)sender).getWorld();
					if (args.length > 1) {
						ymax = Integer.parseInt(args[1]);
					}
				} else {
					if (args.length > 1) {
						String worldName = args[1];
						world = Bukkit.getWorld(worldName);
					}
					if (args.length > 2) {
						ymax = Integer.parseInt(args[2]);
					}
				}
				if (world == null) {
					sender.sendMessage("Usage: magic " + subCommand + " <world> <ymax>");
					return true;
				}
				WandChestRunnable chestRunnable = new WandChestRunnable(controller, world, ymax);
				runningTask = chestRunnable;
				if (subCommand.equalsIgnoreCase("search")) {
					ymax = 0;
					sender.sendMessage("Searching for wands in " + world.getName());
				} else if (subCommand.equalsIgnoreCase("generate")) {
					sender.sendMessage("Generating chunks, and adding wands in " + world.getName() + " below y=" + ymax);
					chestRunnable.setGenerate(true);
				} else {
					sender.sendMessage("Populating chests with wands in " + world.getName() + " below y=" + ymax);
				}
				runningTask.runTaskTimer(this, 5, 5);
				return true;
			}
			if (subCommand.equalsIgnoreCase("cancel"))
			{ 
				checkRunningTask();
				if (runningTask != null) {
					runningTask.cancel();
					runningTask = null;
					sender.sendMessage("Job cancelled");
				} else {
					sender.sendMessage("There is no job running");
				}
				return true;
			}
			if (subCommand.equalsIgnoreCase("clean"))
			{ 
				checkRunningTask();
				if (runningTask != null) {
					sender.sendMessage("Cancel current job first");
					return true;
				}
				World world = null;
				String owner = null;
				if (args.length > 1) {
					owner = args[1];
				}
				if (sender instanceof Player) {
					world = ((Player)sender).getWorld();
				} else {
					if (args.length > 2) {
						String worldName = args[2];
						world = Bukkit.getWorld(worldName);
					}
				}

				String ownerName = owner == null ? "(Unowned)" : owner;
				if (world == null) {
					sender.sendMessage("Cleaning up lost wands in all worlds for owner: " + ownerName);
				} else {
					sender.sendMessage("Cleaning up lost wands in world " + world.getName() + " for owner " + ownerName);
				}
				runningTask = new WandCleanupRunnable(controller, world, owner);
				runningTask.runTaskTimer(this, 5, 5);
				
				return true;
			}
		}

		if (commandLabel.equalsIgnoreCase("wandp"))
		{
			if (args.length == 0) {
				sender.sendMessage("Usage: /wandp [player] [wand name/command]");
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
			return processWandCommand("wandp", sender, player, args2);
		}

		if (commandLabel.equalsIgnoreCase("castp"))
		{
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
			Player player = null;
			if (sender instanceof Player) {
				player = (Player)sender;
			}
			if (!controller.hasPermission(player, "Magic.commands.cast")) return false;
			return processCastCommand(sender, player, args);
		}

		if (!(sender instanceof Player)) {
			if (commandLabel.equalsIgnoreCase("spells"))
			{
				listSpells(sender, -1, args.length > 0 ? args[0] : null);
				return true;
			}
			if (commandLabel.equalsIgnoreCase("wand") && args.length > 0 && args[0].equalsIgnoreCase("list"))
			{
				onWandList(sender);
				return true;
			}
			
			return false;
		}

		// Everything beyond this point is is-game only
		Player player = (Player)sender;
		if (commandLabel.equalsIgnoreCase("wand"))
		{
			return processWandCommand("wand", sender, player, args);
		}

		if (commandLabel.equalsIgnoreCase("spells"))
		{
			if (!controller.hasPermission(player, "Magic.commands.spells")) return false;
			return onSpells(player, args);
		}

		return false;
	}
	
	protected boolean processWandCommand(String command, CommandSender sender, Player player, String[] args)
	{
		String subCommand = "";
		String[] args2 = args;

		if (args.length > 0) {
			subCommand = args[0];
			args2 = new String[args.length - 1];
			for (int i = 1; i < args.length; i++) {
				args2[i - 1] = args[i];
			}
		}
		if (subCommand.equalsIgnoreCase("list"))
		{
			if (!controller.hasPermission(sender, "Magic.commands." + command + "." + subCommand)) return true;

			onWandList(sender);
			return true;
		}
		if (subCommand.equalsIgnoreCase("add"))
		{
			if (!controller.hasPermission(sender, "Magic.commands." + command + "." + subCommand)) return true;
			if (args2.length > 0 && args2[0].equals("material") && !controller.hasPermission(sender,"Magic.commands.wand.add." + args2[0], true)) return true;
			if (args2.length > 0 && !controller.hasPermission(sender,"Magic.commands.wand.add.spell." + args2[0], true)) return true;
			onWandAdd(sender, player, args2);
			return true;
		}
		if (subCommand.equalsIgnoreCase("configure"))
		{
			if (!controller.hasPermission(sender, "Magic.commands." + command + "." + subCommand)) return true;

			onWandConfigure(sender, player, args2, false);
			return true;
		}
		if (subCommand.equalsIgnoreCase("enchant"))
		{
			if (!controller.hasPermission(sender, "Magic.commands." + command + "." + subCommand)) return true;

			onWandEnchant(sender, player);
			return true;
		}
		if (subCommand.equalsIgnoreCase("unenchant"))
		{
			if (!controller.hasPermission(sender, "Magic.commands." + command + "." + subCommand)) return true;

			onWandUnenchant(sender, player);
			return true;
		}
		if (subCommand.equalsIgnoreCase("organize"))
		{
			if (!controller.hasPermission(sender, "Magic.commands." + command + "." + subCommand)) return true;

			onWandOrganize(sender, player);
			return true;
		}
		if (subCommand.equalsIgnoreCase("combine"))
		{
			if (!controller.hasPermission(sender, "Magic.commands." + command + "." + subCommand)) return true;
			if (args.length > 0 && !controller.hasPermission(sender,"Magic.commands." + command + ".combine." + args[0], true)) return true;
			
			onWandCombine(sender, player, args2);
			return true;
		}
		if (subCommand.equalsIgnoreCase("describe"))
		{
			if (!controller.hasPermission(sender, "Magic.commands." + command + "." + subCommand)) return true;

			onWandDescribe(sender, player);
			return true;
		}
		if (subCommand.equalsIgnoreCase("upgrade"))
		{
			if (!controller.hasPermission(sender, "Magic.commands." + command + "." + subCommand)) return true;

			onWandConfigure(sender, player, args2, true);
			return true;
		}
		if (subCommand.equalsIgnoreCase("organize"))
		{
			if (!controller.hasPermission(sender, "Magic.commands." + command + "." + subCommand)) return true;

			onWandOrganize(sender, player);
			return true;
		}
		if (subCommand.equalsIgnoreCase("fill"))
		{
			if (!controller.hasPermission(sender, "Magic.commands." + command + "." + subCommand)) return true;

			onWandFill(sender, player);
			return true;
		}
		if (subCommand.equalsIgnoreCase("remove"))
		{   
			if (!controller.hasPermission(sender, "Magic.commands." + command + "." + subCommand)) return true;

			onWandRemove(sender, player, args2);
			return true;
		}

		if (subCommand.equalsIgnoreCase("name"))
		{
			if (!controller.hasPermission(sender, "Magic.commands." + command + "." + subCommand)) return true;

			onWandName(sender, player, args2);
			return true;
		}

		if (subCommand.length() == 0) 
		{
			if (!controller.hasPermission(sender, "Magic.commands." + command)) return true;
			if (!controller.hasPermission(sender, "Magic.commands." + command + ".wand.default", true)) return true;
		} 
		else 
		{
			if (!controller.hasPermission(sender,"Magic.commands." + command +".wand." + subCommand, true)) return true;
		}
		
		return onWand(sender, player, args);
	}

	public boolean onWandList(CommandSender sender) {
		Collection<ConfigurationNode> templates = Wand.getWandTemplates();
		Map<String, ConfigurationNode> nameMap = new TreeMap<String, ConfigurationNode>();
		for (ConfigurationNode templateConfig : templates)
		{
			nameMap.put(templateConfig.getString("key"), templateConfig);
		}
		for (ConfigurationNode templateConfig : nameMap.values())
		{
			String key = templateConfig.getString("key");
			String name = Messages.get("wands." + key + ".name", Messages.get("wand.default_name"));
			String description = Messages.get("wands." + key + ".description", "");
			description = ChatColor.YELLOW + description; 
			if (!name.equals(key)) {
				description = ChatColor.BLUE + name + ChatColor.WHITE + " : " + description;
			}
			sender.sendMessage(ChatColor.AQUA + key + ChatColor.WHITE + " : " + description);
		}

		return true;
	}

	public boolean onWandDescribe(CommandSender sender, Player player) {
		Mage mage = controller.getMage(player);
		Wand wand = mage.getActiveWand();
		if (wand == null) {
			if (sender != player) {
				sender.sendMessage(player.getName() + " isn't holding a wand");
			} else {
				mage.sendMessage("Equip a wand first");
			}
			return true;
		}
		
		wand.describe(sender);

		return true;
	}
	
	public boolean onWandOrganize(CommandSender sender, Player player)
	{
		Mage mage = controller.getMage(player);
		Wand wand = mage.getActiveWand();
		if (wand == null) {
			mage.sendMessage("Equip a wand first");
			if (sender != player) {
				sender.sendMessage(player.getName() + " isn't holding a wand");
			}
			return true;
		}
		
		wand.deactivate();
		wand.organizeInventory(mage);
		wand.activate(mage);
		mage.sendMessage("Wand reorganized");
		if (sender != player) {
			sender.sendMessage(player.getName() + "'s wand reorganized");
		}
		
		return true;
	}
	
	public boolean onWandEnchant(CommandSender sender, Player player)
	{
		Mage mage = controller.getMage(player);
		ItemStack heldItem = player.getItemInHand();
		if (heldItem == null || heldItem.getType() == Material.AIR)
		{
			mage.sendMessage("Equip an item first");
			if (sender != player) {
				sender.sendMessage(player.getName() + " isn't holding an item");
			}
			return false;
		}
		
		Wand wand = new Wand(controller, heldItem.getType(), heldItem.getDurability());
		player.setItemInHand(wand.getItem());
		wand.activate(mage);
		
		mage.sendMessage("Your " + MaterialBrush.getMaterialName(heldItem.getType(), (byte)heldItem.getDurability()) + " has been enchanted");
		if (sender != player) {
			sender.sendMessage(player.getName() + "'s  " + MaterialBrush.getMaterialName(heldItem.getType(), (byte)heldItem.getDurability()) + " been enchanted");
		}
		
		return true;
	}
	
	public boolean onWandUnenchant(CommandSender sender, Player player)
	{
		Mage mage = controller.getMage(player);
		Wand wand = mage.getActiveWand();
		
		// Trying to make sure the player is actually holding the active wand
		// Just in case. This isn't fool-proof though, if they have more than one wand.
		if (wand == null || !Wand.isWand(player.getItemInHand())) {
			mage.sendMessage("Equip a wand first");
			if (sender != player) {
				sender.sendMessage(player.getName() + " isn't holding a wand");
			}
			return false;
		}

		wand.unenchant();
		player.setItemInHand(wand.getItem());
		mage.setActiveWand(null);
		
		mage.sendMessage("Your wand has been unenchanted");
		if (sender != player) {
			sender.sendMessage(player.getName() + "'s wand has been unenchanted");
		}
		return true;
	}
	
	public boolean onWandConfigure(CommandSender sender, Player player, String[] parameters, boolean safe)
	{
		if (parameters.length < 2) {
			sender.sendMessage("Use: /wand configure <property> <value>");
			sender.sendMessage("Properties: " + StringUtils.join(Wand.PROPERTY_KEYS, ", "));
			return false;
		}

		Mage mage = controller.getMage(player);
		Wand wand = mage.getActiveWand();
		if (wand == null) {
			mage.sendMessage("Equip a wand first");
			if (sender != player) {
				sender.sendMessage(player.getName() + " isn't holding a wand");
			}
			return false;
		}
		if (!wand.isModifiable()) {
			mage.sendMessage("Your wand can not be modified");
			if (sender != player) {
				sender.sendMessage(player.getName() + "'s wand can not be modified");
			}
			return false;
		}
		ConfigurationNode node = new ConfigurationNode();
		String value = parameters[1];
		for (int i = 2; i < parameters.length; i++) {
			value = value + " " + parameters[i];
		}
		node.setProperty(parameters[0], value);
		wand.deactivate();
		wand.configureProperties(node, safe);
		wand.activate(mage);
		mage.sendMessage("Wand reconfigured");
		if (sender != player) {
			sender.sendMessage(player.getName() + "'s wand reconfigured");
		}
		return true;
	}

	public boolean onWandCombine(CommandSender sender, Player player, String[] parameters)
	{
		if (parameters.length < 1) {
			sender.sendMessage("Use: /wand combine <wandname>");
			return true;
		}

		Mage mage = controller.getMage(player);
		Wand wand = mage.getActiveWand();
		if (wand == null) {
			mage.sendMessage("Equip a wand first");
			if (sender != player) {
				sender.sendMessage(player.getName() + " isn't holding a wand");
			}
			return true;
		}

		if (!wand.isModifiable()) {
			mage.sendMessage("Your wand can not be modified");
			if (sender != player) {
				sender.sendMessage(player.getName() + "'s wand can not be modified");
			}
			return true;
		}

		String wandName = parameters[0];
		Wand newWand = Wand.createWand(controller, wandName);
		if (newWand == null) {
			sender.sendMessage("Unknown wand name " + wandName);
			return true;
		}
		wand.deactivate();
		wand.add(newWand);
		wand.activate(mage);
		
		mage.sendMessage("Wand upgraded");
		if (sender != player) {
			sender.sendMessage(player.getName() + "'s wand upgraded");
		}
		return true;
	}

	public boolean onWandFill(CommandSender sender, Player player)
	{
		Mage mage = controller.getMage(player);
		Wand wand = mage.getActiveWand();
		if (wand == null) {
			mage.sendMessage("Equip a wand first");
			if (sender != player) {
				sender.sendMessage(player.getName() + " isn't holding a wand");
			}
			return true;
		}
		
		wand.fill(player);
		mage.sendMessage("Your wand now contains all the spells you know");
		if (sender != player) {
			sender.sendMessage(player.getName() + "'s wand filled");
		}
		
		return true;
	}
	
	public boolean onWandAdd(CommandSender sender, Player player, String[] parameters)
	{
		if (parameters.length < 1) {
			sender.sendMessage("Use: /wand add <spell|material> [material:data]");
			return true;
		}

		Mage mage = controller.getMage(player);
		Wand wand = mage.getActiveWand();
		if (wand == null) {
			mage.sendMessage("Equip a wand first");
			if (sender != player) {
				sender.sendMessage(player.getName() + " isn't holding a wand");
			}
			return true;
		}
		if (!wand.isModifiable()) {
			mage.sendMessage("This wand can not be modified");
			if (sender != player) {
				sender.sendMessage(player.getName() + "'s wand can't be modified");
			}
			return true;
		}

		String spellName = parameters[0];
		if (spellName.equals("material")) {
			if (parameters.length < 2) {
				sender.sendMessage("Use: /wand add material <material:data>");
				return true;
			}
			
			String materialKey = parameters[1];
			if (!MaterialBrush.isValidMaterial(materialKey, false)) {
				sender.sendMessage(materialKey + " is not a valid material");
				return true;
			}
			
			if (wand.addMaterial(materialKey, true, false)) {
				mage.sendMessage("Material '" + materialKey + "' has been added to your wand");
				if (sender != player) {
					sender.sendMessage("Added material '" + materialKey + "' to " + player.getName() + "'s wand");
				}
			} else {
				mage.sendMessage("Material activated: " + materialKey);
				if (sender != player) {
					sender.sendMessage(player.getName() + "'s wand already has material " + materialKey);
				}
			}
			return true;
		}
		Spell spell = mage.getSpell(spellName);
		if (spell == null)
		{
			sender.sendMessage("Spell '" + spellName + "' unknown, Use /spells for spell list");
			return true;
		}

		if (wand.addSpell(spellName, true)) {
			mage.sendMessage("Spell '" + spell.getName() + "' has been added to your wand");
			if (sender != player) {
				sender.sendMessage("Added '" + spell.getName() + "' to " + player.getName() + "'s wand");
			}
		} else {
			mage.sendMessage(spell.getName() + " activated");
			if (sender != player) {
				sender.sendMessage(player.getName() + "'s wand already has " + spell.getName());
			}
		}

		return true;
	}

	public boolean onWandRemove(CommandSender sender, Player player, String[] parameters)
	{
		if (parameters.length < 1) {
			sender.sendMessage("Use: /wand remove <spell|material> [material:data]");
			return true;
		}

		Mage mage = controller.getMage(player);
		Wand wand = mage.getActiveWand();
		if (wand == null) {
			mage.sendMessage("Equip a wand first");
			if (sender != player) {
				sender.sendMessage(player.getName() + " isn't holding a wand");
			}
			return true;
		}
		if (!wand.isModifiable()) {
			mage.sendMessage("This wand can not be modified");
			if (sender != player) {
				sender.sendMessage(player.getName() + "'s wand can't be modified");
			}
			return true;
		}

		String spellName = parameters[0];	
		if (spellName.equals("material")) {
			if (parameters.length < 2) {
				sender.sendMessage("Use: /wand remove material <material:data>");
				return true;
			}
			String materialKey = parameters[1];
			if (wand.removeMaterial(materialKey)) {
				mage.sendMessage("Material '" + materialKey + "' has been removed from your wand");
				if (sender != player) {
					sender.sendMessage("Removed material '" + materialKey + "' from " + player.getName() + "'s wand");
				}
			} else {
				if (sender != player) {
					sender.sendMessage(player.getName() + "'s wand does not have material " + materialKey);
				}
			}
			return true;
		}
		if (wand.removeSpell(spellName)) {
			mage.sendMessage("Spell '" + spellName + "' has been removed from your wand");
			if (sender != player) {
				sender.sendMessage("Removed '" + spellName + "' from " + player.getName() + "'s wand");
			}
		} else {
			if (sender != player) {
				sender.sendMessage(player.getName() + "'s wand does not have " + spellName);
			}
		}

		return true;
	}

	public boolean onWandName(CommandSender sender, Player player, String[] parameters)
	{
		if (parameters.length < 1) {
			sender.sendMessage("Use: /wand name <name>");
			return true;
		}
		
		Mage mage = controller.getMage(player);
		Wand wand = mage.getActiveWand();
		if (wand == null) {
			mage.sendMessage("Equip a wand first");
			if (sender != player) {
				sender.sendMessage(player.getName() + " isn't holding a wand");
			}
			return true;
		}
		
		wand.setName(StringUtils.join(parameters, " "));
		sender.sendMessage("Wand renamed");

		return true;
	}

	public boolean onWand(CommandSender sender, Player player, String[] parameters)
	{
		String wandName = null;
		if (parameters.length > 0)
		{
			wandName = parameters[0];
		}

		Mage mage = controller.getMage(player);
		Wand currentWand =  mage.getActiveWand();
		if (currentWand != null) {
			currentWand.closeInventory();
		}
	
		Wand wand = Wand.createWand(controller, wandName, mage);
		if (wand == null) {
			sender.sendMessage("No wand defined with key " + wandName);
			return true;
		}
		
		// Check for special "fill wands" configuration
		if (controller.fillWands() && parameters.length == 0) {
			wand.fill(player);
		}
	
		// Place directly in hand if possible
		PlayerInventory inventory = player.getInventory();
		ItemStack inHand = inventory.getItemInHand();
		if (inHand == null || inHand.getType() == Material.AIR) {
			inventory.setItem(inventory.getHeldItemSlot(), wand.getItem());
			wand.activate(mage);
		} else {
			player.getInventory().addItem(wand.getItem());
		}
		if (sender != player) {
			sender.sendMessage("Gave wand " + wand.getName() + " to " + player.getName());
		}
		return true;
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

		Player usePermissions = (sender == player) ? player : (sender instanceof Player ? (Player)sender : null);
		CommandSender mageController = player == null ? sender : player;
		Location targetLocation = null;
		if (sender instanceof BlockCommandSender) {
			targetLocation = ((BlockCommandSender)sender).getBlock().getLocation();
		}
		if (sender instanceof Player) {
			targetLocation = ((Player)player).getLocation();
		}
		Mage mage = controller.getMage(mageController);
		Spell spell = mage.getSpell(spellName, usePermissions);
		if (spell == null)
		{
			sender.sendMessage("Spell " + spellName + " unknown");
			return false;
		}

		// Make it free and skip cooldowns, if configured to do so.
		controller.toggleCastCommandOverrides(mage, true);
		spell.cast(parameters, targetLocation);
		controller.toggleCastCommandOverrides(mage, false);
		if (sender != player) {
			String castMessage = "Cast " + spellName;
			if (player != null) {
				castMessage += " on " + player.getName();
			}
			sender.sendMessage(castMessage);
		}

		return true;
	}
	
	public boolean onSpells(Player player, String[] parameters)
	{
		int pageNumber = 1;
		String category = null;
		if (parameters.length > 0)
		{
			try
			{
				pageNumber = Integer.parseInt(parameters[0]);
			}
			catch (NumberFormatException ex)
			{
				pageNumber = 1;
				category = parameters[0];
			}
		}
		listSpells(player, pageNumber, category);

		return true;
	}


	/* 
	 * Help commands
	 */

	public void listSpellsByCategory(CommandSender sender, String category)
	{
		List<Spell> categorySpells = new ArrayList<Spell>();
		List<Spell> spellVariants = controller.getAllSpells();
		Player player = sender instanceof Player ? (Player)sender : null;
		for (Spell spell : spellVariants)
		{
			String spellCategory = spell.getCategory();
			if (spellCategory != null && spellCategory.equalsIgnoreCase(category) 
				&& (player == null || spell.hasSpellPermission(player)))
			{
				categorySpells.add(spell);
			}
		}

		if (categorySpells.size() == 0)
		{
			String message = Messages.get("general.no_spells_in_category");
			message = message.replace("$category", category);
			sender.sendMessage(message);
			return;
		}
		sender.sendMessage(category + ":");
		Collections.sort(categorySpells);
		for (Spell spell : categorySpells)
		{
			String name = spell.getName();
			String description = spell.getDescription();
			if (!name.equals(spell.getKey())) {
				description = name + " : " + description;
			}
			sender.sendMessage(ChatColor.AQUA + spell.getKey() + ChatColor.BLUE + " [" + spell.getIcon().getMaterial().name().toLowerCase() + "] : " + ChatColor.YELLOW + description);
		}
	}

	public void listCategories(Player player)
	{
		HashMap<String, Integer> spellCounts = new HashMap<String, Integer>();
		List<String> spellGroups = new ArrayList<String>();
		List<Spell> spellVariants = controller.getAllSpells();

		for (Spell spell : spellVariants)
		{
			if (player != null && !spell.hasSpellPermission(player)) continue;
			if (spell.getCategory() == null) continue;
			
			Integer spellCount = spellCounts.get(spell.getCategory());
			if (spellCount == null || spellCount == 0)
			{
				spellCounts.put(spell.getCategory(), 1);
				spellGroups.add(spell.getCategory());
			}
			else
			{
				spellCounts.put(spell.getCategory(), spellCount + 1);
			}
		}
		if (spellGroups.size() == 0)
		{
			player.sendMessage(Messages.get("general.no_spells"));
			return;
		}

		Collections.sort(spellGroups);
		for (String group : spellGroups)
		{
			player.sendMessage(group + " [" + spellCounts.get(group) + "]");
		}
	}

	public void listSpells(CommandSender sender, int pageNumber, String category)
	{
		if (category != null)
		{
			listSpellsByCategory(sender, category);
			return;
		}
		Player player = sender instanceof Player ? (Player)sender : null;

		HashMap<String, SpellGroup> spellGroups = new HashMap<String, SpellGroup>();
		List<Spell> spellVariants = controller.getAllSpells();

		int spellCount = 0;
		for (Spell spell : spellVariants)
		{
			if (player != null && !spell.hasSpellPermission(player))
			{
				continue;
			}
			if (spell.getCategory() == null) continue;
			spellCount++;
			SpellGroup group = spellGroups.get(spell.getCategory());
			if (group == null)
			{
				group = new SpellGroup();
				group.groupName = spell.getCategory();
				spellGroups.put(group.groupName, group);	
			}
			group.spells.add(spell);
		}

		List<SpellGroup> sortedGroups = new ArrayList<SpellGroup>();
		sortedGroups.addAll(spellGroups.values());
		Collections.sort(sortedGroups);

		int maxLines = -1;
		if (pageNumber >= 0) {
			maxLines = 5;
			int maxPages = spellCount / maxLines + 1;
			if (pageNumber > maxPages)
			{
				pageNumber = maxPages;
			}
			String message = Messages.get("general.spell_list_page");
			message = message.replace("$count", Integer.toString(spellCount));
			message = message.replace("$pages", Integer.toString(maxPages));
			message = message.replace("$page", Integer.toString(pageNumber));
			sender.sendMessage(message);
		} else {
			String message = Messages.get("general.spell_list");
			message = message.replace("$count", Integer.toString(spellCount));
			sender.sendMessage(message);	
		}

		int currentPage = 1;
		int lineCount = 0;
		int printedCount = 0;
		for (SpellGroup group : sortedGroups)
		{
			if (printedCount > maxLines && maxLines > 0) break;

			boolean isFirst = true;
			Collections.sort(group.spells);
			for (Spell spell : group.spells)
			{
				if (printedCount > maxLines && maxLines > 0) break;

				if (currentPage == pageNumber || maxLines < 0)
				{
					if (isFirst)
					{
						sender.sendMessage(group.groupName + ":");
						isFirst = false;
					}
					String name = spell.getName();
					String description = spell.getDescription();
					if (!name.equals(spell.getKey())) {
						description = name + " : " + description;
					}
					sender.sendMessage(ChatColor.AQUA + spell.getKey() + ChatColor.BLUE + " [" + spell.getIcon().getMaterial().name().toLowerCase() + "] : " + ChatColor.YELLOW + description);
					printedCount++;
				}
				lineCount++;
				if (lineCount == maxLines)
				{
					lineCount = 0;
					currentPage++;
				}	
			}
		}
	}

	public void onDisable() 
	{
		controller.save();
		controller.clear();
	}

	/*
	 * Private data
	 */	
	private MagicController controller = null;
	private MagicRunnable runningTask = null;
}
