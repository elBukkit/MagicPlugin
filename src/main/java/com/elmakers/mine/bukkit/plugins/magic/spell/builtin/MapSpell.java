package com.elmakers.mine.bukkit.plugins.magic.spell.builtin;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapView;

import com.elmakers.mine.bukkit.plugins.magic.spell.SpellResult;
import com.elmakers.mine.bukkit.plugins.magic.spell.TargetingSpell;

public class MapSpell extends TargetingSpell
{
	@SuppressWarnings("deprecation")
	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		World world = getWorld();
		MapView newMap = Bukkit.createMap(world);
		ItemStack newMapItem = new ItemStack(Material.MAP, 1, newMap.getId());
		world.dropItemNaturally(getLocation(), newMapItem);
		return SpellResult.CAST;
	}
}
