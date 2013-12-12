package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapView;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class MapSpell extends Spell
{
	@SuppressWarnings("deprecation")
	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		World world = player.getWorld();
		MapView newMap = Bukkit.createMap(world);
		castMessage("Here's a map!");
		ItemStack newMapItem = new ItemStack(Material.MAP, 1, newMap.getId());
		world.dropItemNaturally(player.getLocation(), newMapItem);
		return SpellResult.SUCCESS;
	}

	@Override
	public void onLoad(ConfigurationNode node)
	{
		disableTargeting();
	}
}
