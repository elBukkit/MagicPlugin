package com.elmakers.mine.bukkit.plugins.magic.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.utilities.Messages;

public class SpellsCommandExecutor extends MagicTabExecutor {

	public SpellsCommandExecutor(MagicAPI api) {
		super(api);
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, String comandName, String[] args) {
		return new ArrayList<String>();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!api.hasPermission(sender, "Magic.commands.spells")) {
			sendNoPermission(sender);
			return true;
		}
		listSpells(sender, -1, args.length > 0 ? args[0] : null);
		return true;
	}
	

	public void listSpellsByCategory(CommandSender sender, String category)
	{
		List<SpellTemplate> categorySpells = new ArrayList<SpellTemplate>();
		Collection<SpellTemplate> spellVariants = api.getSpellTemplates();
		Player player = sender instanceof Player ? (Player)sender : null;
		for (SpellTemplate spell : spellVariants)
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
		for (SpellTemplate spell : categorySpells)
		{
			String name = spell.getName();
			String description = spell.getDescription();
			if (!name.equals(spell.getKey())) {
				description = name + " : " + description;
			}
			sender.sendMessage(ChatColor.AQUA + spell.getKey() + ChatColor.BLUE + " [" + spell.getIcon().getName() + "] : " + ChatColor.YELLOW + description);
		}
	}

	public void listCategories(Player player)
	{
		HashMap<String, Integer> spellCounts = new HashMap<String, Integer>();
		List<String> spellGroups = new ArrayList<String>();
		Collection<SpellTemplate> spellVariants = api.getSpellTemplates();

		for (SpellTemplate spell : spellVariants)
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
		Collection<SpellTemplate> spellVariants = api.getSpellTemplates();

		int spellCount = 0;
		for (SpellTemplate spell : spellVariants)
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
			for (SpellTemplate spell : group.spells)
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
}
