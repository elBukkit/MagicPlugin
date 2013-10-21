package com.elmakers.mine.bukkit.plugins.magic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.elmakers.mine.bukkit.dao.BlockData;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class MagicPlugin extends JavaPlugin
{	
	/*
	 * Public API
	 */
	public Spells getSpells()
	{
		return spells;
	}

	/*
	 * Plugin interface
	 */

	public void onEnable() 
	{
		initialize();

		BlockData.setServer(getServer());
		PluginManager pm = getServer().getPluginManager();

		pm.registerEvents(spells, this);

		PluginDescriptionFile pdfFile = this.getDescription();
		log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled");
	}

	protected void initialize()
	{
		spells.initialize(this);
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
				if (!spells.hasPermission((Player)sender, "Magic.commands.magic." + subCommand)) return true;
			}
			if (subCommand.equalsIgnoreCase("reload"))
			{
				spells.clear();
				spells.load();
				return true;
			}
			if (subCommand.equalsIgnoreCase("reset"))
			{   
				spells.reset();
				return true;
			}
		}

		// Everything beyond this point is is-game only
		if (!(sender instanceof Player)) return false;

		Player player = (Player)sender;

		if (commandName.equalsIgnoreCase("wand"))
		{
			String subCommand = "";
			String[] args2 = args;

			if (args.length > 0) {
				subCommand = args[0];;
				args2 = new String[args.length - 1];
				for (int i = 1; i < args.length; i++) {
					args2[i - 1] = args[i];
				}
			}
			if (subCommand.equalsIgnoreCase("list"))
			{
				if (!spells.hasPermission(player, "Magic.commands.wand." + subCommand)) return true;

				onWandList(player);
				return true;
			}
			if (subCommand.equalsIgnoreCase("add"))
			{
				if (!spells.hasPermission(player, "Magic.commands.wand." + subCommand)) return true;

				onWandAdd(player, args2);
				return true;
			}
			if (subCommand.equalsIgnoreCase("remove"))
			{   
				if (!spells.hasPermission(player, "Magic.commands.wand." + subCommand)) return true;

				onWandRemove(player, args2);
				return true;
			}

			if (subCommand.equalsIgnoreCase("name"))
			{
				if (!spells.hasPermission(player, "Magic.commands.wand." + subCommand)) return true;

				onWandName(player, args2);
				return true;
			}

			if (!spells.hasPermission(player, "Magic.commands.wand")) return true;
			return onWand(player, args);
		}

		if (commandName.equalsIgnoreCase("cast"))
		{
			if (!spells.hasPermission(player, "Magic.commands.cast")) return true;
			return onCast(player, args);
		}

		if (commandName.equalsIgnoreCase("spells"))
		{
			if (!spells.hasPermission(player, "Magic.commands.spells")) return true;
			return onSpells(player, args);
		}

		return false;
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
			String name = templateConfig.getString("name");
			String description = templateConfig.getString("description");
			description = ChatColor.YELLOW + description; 
			if (!name.equals(key)) {
				description = ChatColor.BLUE + name + ChatColor.WHITE + " : " + description;
			}
			sender.sendMessage(ChatColor.AQUA + key + ChatColor.WHITE + " : " + description);
		}

		return true;
	}
	
	public boolean onWandAdd(Player player, String[] parameters)
	{
		if (parameters.length < 1) {
			player.sendMessage("Use: /wand add <spell|material> [material]");
			return true;
		}
		
		Wand wand = Wand.getActiveWand(player);
		if (wand == null) {
			player.sendMessage("Equip a wand first (use /wand if needed)");
			return true;
		}

		PlayerSpells playerSpells = spells.getPlayerSpells(player);
		String spellName = parameters[0];
		if (spellName.equals("material")) {
			if (parameters.length < 2) {
				player.sendMessage("Use: /wand add material <material> [data]");
				return true;
			}
			String materialName = parameters[1];
			byte data = 0;
			Material material = Material.AIR;
			if (materialName.equals("erase")) {
				material = Wand.EraseMaterial;
			} else {
				List<Material> buildingMaterials = spells.getBuildingMaterials();
				material = ConfigurationNode.toMaterial(materialName);
				if (material == null || material == Material.AIR || !buildingMaterials.contains(material)) {
					player.sendMessage(materialName + " is not a valid material");
					return true;
				}
				if (parameters.length > 2) {
					data = (byte)Integer.parseInt(parameters[2]);
				}
			}
			wand.saveInventory(playerSpells);
			wand.addMaterial(material, data);
			wand.updateInventory(playerSpells);
			return true;
		}
		Spell spell = playerSpells.getSpell(spellName);
		if (spell == null)
		{
			player.sendMessage("Spell '" + spellName + "' unknown, Use /spells for spell list");
			return true;
		}

		wand.saveInventory(playerSpells);
		wand.addSpell(spellName);
		wand.updateInventory(playerSpells);

		return true;
	}

	public boolean onWandRemove(Player player, String[] parameters)
	{
		if (parameters.length < 1) {
			player.sendMessage("Use: /wand remove <spell|material> [material]");
			return true;
		}
		
		Wand wand = Wand.getActiveWand(player);
		if (wand == null) {
			player.sendMessage("Equip a wand first (use /wand if needed)");
			return true;
		}

		PlayerSpells playerSpells = spells.getPlayerSpells(player);
		String spellName = parameters[0];
		
		if (spellName.equals("material")) {
			if (parameters.length < 2) {
				player.sendMessage("Use: /wand remove material <material> [data]");
				return true;
			}
			String materialName = parameters[1];
			Material material = Material.AIR;
			byte data = 0;
			if (materialName.equals("erase")) {
				material = Wand.EraseMaterial;
			} else {
				material = ConfigurationNode.toMaterial(materialName);
				if (parameters.length > 2) {
					data = (byte)Integer.parseInt(parameters[2]);
				}
			}
			wand.saveInventory(playerSpells);
			wand.removeMaterial(material, data);
			wand.updateInventory(playerSpells);
			return true;
		}

		wand.saveInventory(playerSpells);
		wand.removeSpell(spellName);
		wand.updateInventory(playerSpells);

		return true;
	}

	public boolean onWandName(Player player, String[] parameters)
	{
		if (parameters.length < 1) {
			player.sendMessage("Use: /wand name <name>");
			return true;
		}
		
		Wand wand = Wand.getActiveWand(player);
		if (wand == null) {
			player.sendMessage("Equip a wand first (use /wand if needed)");
			return true;
		}
		
		wand.setName(StringUtils.join(parameters, " "));

		return true;
	}

	public boolean onWand(Player player, String[] parameters)
	{
		boolean holdingWand = Wand.isActive(player);
		String wandName = "default";
		if (parameters.length > 0)
		{
			wandName = parameters[0];
		}

		if (!holdingWand)
		{
			Wand wand = Wand.createWand(wandName);
		
			// Place directly in hand if possible
			PlayerInventory inventory = player.getInventory();
			ItemStack inHand = inventory.getItemInHand();
			if (inHand == null || inHand.getType() == Material.AIR) {
				PlayerSpells playerSpells = spells.getPlayerSpells(player);
				inventory.setItem(inventory.getHeldItemSlot(), wand.getItem());
				if (playerSpells.storeInventory()) {
					// Create spell inventory
					wand.updateInventory(playerSpells);
				}
			} else {
				player.getInventory().addItem(wand.getItem());
			}
		}
		else 
		{
			player.sendMessage("Unequip your current wand to create a new one");
		}
		return true;
	}

	public boolean onCast(Player player, String[] castParameters)
	{
		if (castParameters.length < 1) return false;

		String spellName = castParameters[0];
		String[] parameters = new String[castParameters.length - 1];
		for (int i = 1; i < castParameters.length; i++)
		{
			parameters[i - 1] = castParameters[i];
		}

		PlayerSpells playerSpells = spells.getPlayerSpells(player);
		Spell spell = playerSpells.getSpell(spellName);
		if (spell == null)
		{
			return false;
		}

		spell.cast(parameters);

		return true;
	}

	public boolean onReload(CommandSender sender, String[] parameters)
	{
		spells.load();
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

	public void listSpellsByCategory(Player player,String category)
	{
		List<Spell> categorySpells = new ArrayList<Spell>();
		List<Spell> spellVariants = spells.getAllSpells();
		for (Spell spell : spellVariants)
		{
			if (spell.getCategory().equalsIgnoreCase(category) && spell.hasSpellPermission(player))
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
			player.sendMessage(ChatColor.AQUA + spell.getKey() + ChatColor.BLUE + " [" + spell.getMaterial().name().toLowerCase() + "] : " + ChatColor.YELLOW + description);
		}
	}

	public void listCategories(Player player)
	{
		HashMap<String, Integer> spellCounts = new HashMap<String, Integer>();
		List<String> spellGroups = new ArrayList<String>();
		List<Spell> spellVariants = spells.getAllSpells();

		for (Spell spell : spellVariants)
		{
			if (!spell.hasSpellPermission(player)) continue;

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

	public void listSpells(Player player, int pageNumber, String category)
	{
		if (category != null)
		{
			listSpellsByCategory(player, category);
			return;
		}

		HashMap<String, SpellGroup> spellGroups = new HashMap<String, SpellGroup>();
		List<Spell> spellVariants = spells.getAllSpells();

		int spellCount = 0;
		for (Spell spell : spellVariants)
		{
			if (!spell.hasSpellPermission(player))
			{
				continue;
			}
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

		int maxLines = 5;
		int maxPages = spellCount / maxLines + 1;
		if (pageNumber > maxPages)
		{
			pageNumber = maxPages;
		}

		player.sendMessage("You know " + spellCount + " spells. [" + pageNumber + "/" + maxPages + "]");

		int currentPage = 1;
		int lineCount = 0;
		int printedCount = 0;
		for (SpellGroup group : sortedGroups)
		{
			if (printedCount > maxLines) break;

			boolean isFirst = true;
			Collections.sort(group.spells);
			for (Spell spell : group.spells)
			{
				if (printedCount > maxLines) break;

				if (currentPage == pageNumber)
				{
					if (isFirst)
					{
						player.sendMessage(group.groupName + ":");
						isFirst = false;
					}
					String name = spell.getName();
					String description = spell.getDescription();
					if (!name.equals(spell.getKey())) {
						description = name + " : " + description;
					}
					player.sendMessage(ChatColor.AQUA + spell.getKey() + ChatColor.BLUE + " [" + spell.getMaterial().name().toLowerCase() + "] : " + ChatColor.YELLOW + description);
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
		spells.clear();
	}

	/*
	 * Private data
	 */	
	private final Spells spells = new Spells();
	private final Logger log = Logger.getLogger("Minecraft");
}
