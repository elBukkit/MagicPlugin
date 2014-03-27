package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.Target;
import com.elmakers.mine.bukkit.utilities.URLMap;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class CameraSpell extends Spell
{
	@SuppressWarnings("deprecation")
	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		ItemStack newMapItem = null;
		Integer priority = parameters.getInteger("priority", null);
		
		// Check for special case id
		if (parameters.containsKey("id"))
		{
			newMapItem = new ItemStack(Material.MAP, 1, (short)parameters.getInt("id", 0));
			String mapName = parameters.getString("name", "Image");
			ItemMeta meta = newMapItem.getItemMeta();
			// TODO: How to handle names with spaces in them?
			meta.setDisplayName(mapName);
			newMapItem.setItemMeta(meta);
		}
		
		// Check for special case url
		if (newMapItem == null) {
			String url = parameters.getString("url");
			if (url != null) {
				int x = parameters.getInt("x", 0);
				int y = parameters.getInt("y", 0);
				int width = parameters.getInt("width", 0);
				int height = parameters.getInt("height", 0);
				String mapName = parameters.getString("name", "Photo");
				newMapItem = URLMap.getURLItem(getWorld().getName(), url, mapName, x, y, width, height, priority);
			}
		}
		
		if (newMapItem == null) {
			Target target = getTarget();
			String playerName = parameters.getString("name");
			String metaName = null;
			if (playerName == null) 
			{
				if (target != null)
				{
					if (target.isEntity()) {
						Entity targetEntity = target.getEntity();
						if (targetEntity instanceof Player) {
							playerName = ((Player)targetEntity).getName();
						} else {
							playerName = getMobSkin(targetEntity.getType());
							if (playerName != null) {
								metaName = targetEntity.getType().getName();
							}
						}
					} else {
						Block targetBlock = target.getBlock();
						if (targetBlock == null) {
							return SpellResult.NO_TARGET;
						}
						playerName = getBlockSkin(targetBlock.getType());
						if (playerName != null) {
							metaName = target.getBlock().getType().name();
						}
					}
				}

				if (playerName == null)
				{
					Player player = getPlayer();
					if (player == null) {
						return SpellResult.NO_TARGET;
					}
					playerName = player.getName();
				}
			}
			if (parameters.containsKey("reload")) {
				URLMap.forceReloadPlayerPortrait(getWorld().getName(), playerName);
			}
			newMapItem = URLMap.getPlayerPortrait(getWorld().getName(), playerName, priority, metaName);
		}
		if (newMapItem == null) {
			return SpellResult.FAIL;
		}
		getWorld().dropItemNaturally(getLocation(), newMapItem);
		
		return SpellResult.CAST;
	}
}
