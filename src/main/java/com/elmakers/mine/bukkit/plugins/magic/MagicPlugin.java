package com.elmakers.mine.bukkit.plugins.magic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.elmakers.mine.bukkit.blocks.BlockData;
import com.elmakers.mine.bukkit.plugins.magic.populator.WandChestRunnable;
import com.elmakers.mine.bukkit.plugins.magic.wand.Wand;
import com.elmakers.mine.bukkit.utilities.Messages;
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
	
	protected void populateChests(CommandSender sender, World world, int ymax)
	{
		checkRunningTask();
		if (runningTask != null) {
			sender.sendMessage("There is already a populate job running");
			return;
		}
		runningTask= new WandChestRunnable(controller, world, ymax);
		runningTask.runTaskTimer(this, 5, 5);
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
				addIfPermissible(sender, options, "Magic.commands.", "search");
				addIfPermissible(sender, options, "Magic.commands.", "cancel");
				addIfPermissible(sender, options, "Magic.commands.", "reload");
				addIfPermissible(sender, options, "Magic.commands.", "commit");
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
				mage = controller.getMage(args[0]);
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
		String commandName = cmd.getName();

		if (commandName.equalsIgnoreCase("magic") && args.length > 0)
		{
			String subCommand = args[0];
			if (sender instanceof Player)
			{
				if (!controller.hasPermission((Player)sender, "Magic.commands.magic." + subCommand)) return false;
			}
			if (subCommand.equalsIgnoreCase("reload"))
			{
				controller.clear();
				controller.load();
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
			if (subCommand.equalsIgnoreCase("populate") || subCommand.equalsIgnoreCase("search"))
			{   
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
					getLogger().warning("Usage: magic " + subCommand + " <world> <ymax>");
					return true;
				}
				if (subCommand.equalsIgnoreCase("search")) {
					ymax = 0;
				}
				populateChests(sender, world, ymax);
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
		}

		if (commandName.equalsIgnoreCase("wandp"))
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

		if (commandName.equalsIgnoreCase("castp"))
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

		if (!(sender instanceof Player)) {
			if (commandName.equalsIgnoreCase("spells"))
			{
				listSpells(sender, -1, null);
				return true;
			}
			if (commandName.equalsIgnoreCase("wand") && args.length > 0 && args[0].equalsIgnoreCase("list"))
			{
				onWandList(sender);
				return true;
			}
			
			return false;
		}

		// Everything beyond this point is is-game only
		Player player = (Player)sender;
		if (commandName.equalsIgnoreCase("wand"))
		{
			return processWandCommand("wand", sender, player, args);
		}

		if (commandName.equalsIgnoreCase("cast"))
		{
			if (!controller.hasPermission(player, "Magic.commands.cast")) return false;
			return processCastCommand(player, player, args);

		}

		if (commandName.equalsIgnoreCase("spells"))
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

		if (!controller.hasPermission(sender, "Magic.commands." + command + "")) return true;
		if (subCommand.length() > 0 && !controller.hasPermission(sender,"Magic.commands." + command +".wand." + subCommand, true)) return true;
		
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
		wand.organizeInventory();
		wand.activate(mage);
		mage.sendMessage("Wand reorganized");
		if (sender != player) {
			sender.sendMessage(player.getName() + "'s wand reorganized");
		}
		
		return true;
	}
	
	public boolean onWandConfigure(CommandSender sender, Player player, String[] parameters, boolean safe)
	{
		if (parameters.length < 2) {
			sender.sendMessage("Use: /wand configure <property> <value>");
			sender.sendMessage("Properties: cost_reduction, uses, health_regeneration, hunger_regeneration, xp_regeneration,");
			sender.sendMessage("  xp_max, protection, protection_physical, protection_projectiles,");
			sender.sendMessage("  protection_falling,protection_fire, protection_explosions, haste, power");
			
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
		ConfigurationNode node = new ConfigurationNode();
		node.setProperty(parameters[0], parameters[1]);
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
		
		fillWand(wand, player);
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
			if (!Wand.isValidMaterial(materialKey)) {
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
	
		Wand wand = Wand.createWand(controller, wandName);
		if (wand == null) {
			sender.sendMessage("No wand defined with key " + wandName);
			return true;
		}
		
		// Check for special "fill wands" configuration
		if (controller.fillWands() && parameters.length == 0) {
			fillWand(wand, player);
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
	
	protected void fillWand(Wand wand, Player player) {
		List<Spell> allSpells = controller.getAllSpells();

		for (Spell spell : allSpells)
		{
			if (spell.hasSpellPermission(player) && spell.getIcon().getMaterial() != Material.AIR)
			{
				wand.addSpell(spell.getKey());
			}
		}
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

		Player usePermissions = (sender instanceof Player) ? (Player)sender : player;
		Mage mage = controller.getMage(player);
		Spell spell = mage.getSpell(spellName, usePermissions);
		if (spell == null)
		{
			sender.sendMessage("Spell " + spellName + " unknown");
			return false;
		}

		// Make it free and skip cooldowns, if configured to do so.
		controller.toggleCastCommandOverrides(mage, true);
		spell.cast(parameters);
		controller.toggleCastCommandOverrides(mage, false);
		if (sender != player) {
			sender.sendMessage("Cast " + spellName + " on " + player.getName());
		}

		return true;
	}

	public boolean onReload(CommandSender sender, String[] parameters)
	{
		controller.load();
		sender.sendMessage("Configuration reloaded.");
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
			if (spell.getCategory().equalsIgnoreCase(category) && (player == null || spell.hasSpellPermission(player)))
			{
				categorySpells.add(spell);
			}
		}

		if (categorySpells.size() == 0)
		{
			player.sendMessage("You don't know any spells");
			return;
		}

		Collections.sort(categorySpells);
		for (Spell spell : categorySpells)
		{
			String name = spell.getName();
			String description = spell.getDescription();
			if (!name.equals(spell.getKey())) {
				description = name + " : " + description;
			}
			player.sendMessage(ChatColor.AQUA + spell.getKey() + ChatColor.BLUE + " [" + spell.getIcon().getMaterial().name().toLowerCase() + "] : " + ChatColor.YELLOW + description);
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
			player.sendMessage("You don't know any spells");
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

			sender.sendMessage("You know " + spellCount + " spells. [" + pageNumber + "/" + maxPages + "]");
		} else {
			sender.sendMessage("Listing " + spellCount + " spells.");	
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
	private WandChestRunnable runningTask = null;
}
