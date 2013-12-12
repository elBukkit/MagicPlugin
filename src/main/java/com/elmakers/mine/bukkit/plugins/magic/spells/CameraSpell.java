package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.plugins.magic.Target;
import com.elmakers.mine.bukkit.utilities.SkinRenderer;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class CameraSpell extends Spell
{
	@SuppressWarnings("deprecation")
	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		this.targetEntity(Player.class);
		Target target = getTarget();
		Player targetPlayer  =player;
		if (target != null && target.isEntity() && (target.getEntity() instanceof Player))
		{
			castMessage("CLICK!");
			targetPlayer = (Player)target.getEntity();
		} else {
			castMessage("Selfie!");
		}
		World world = player.getWorld();
		MapView newMap = Bukkit.createMap(world);
		for(MapRenderer renderer : newMap.getRenderers()) {
			newMap.removeRenderer(renderer);
		}
		MapRenderer renderer = new SkinRenderer(targetPlayer.getName());
		newMap.addRenderer(renderer);
		ItemStack newMapItem = new ItemStack(Material.MAP, 1, newMap.getId());
		world.dropItemNaturally(player.getLocation(), newMapItem);
		return SpellResult.SUCCESS;
	}
}
