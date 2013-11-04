package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class MapSpell extends Spell
{
	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		Inventory inventory = player.getInventory();
		if (!inventory.contains(Material.MAP))
		{
			castMessage("Here's a map!");
			inventory.addItem(new ItemStack(Material.MAP));
			return SpellResult.SUCCESS;
		}
		HashMap<Integer,? extends ItemStack> currentMap = inventory.all(Material.MAP);
		ItemStack first = currentMap.values().iterator().next();
		short mapId = first.getDurability();
		castMessage("You've got map#" + mapId);

		return SpellResult.COST_FREE;
	}

	@Override
	public void onLoad(ConfigurationNode node)
	{
		disableTargeting();
	}
}
