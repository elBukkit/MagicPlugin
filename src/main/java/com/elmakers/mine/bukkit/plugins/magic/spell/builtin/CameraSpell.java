package com.elmakers.mine.bukkit.plugins.magic.spell.builtin;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.plugins.magic.spell.TargetingSpell;
import com.elmakers.mine.bukkit.utilities.ConfigurationUtils;
import com.elmakers.mine.bukkit.utilities.Target;
import com.elmakers.mine.bukkit.utility.URLMap;

public class CameraSpell extends TargetingSpell
{
	@SuppressWarnings("deprecation")
	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		ItemStack newMapItem = null;
		Integer priority = ConfigurationUtils.getInteger(parameters, "priority", null);
		
		// Check for special case id
		if (parameters.contains("id"))
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
					if (target.hasEntity()) {
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
			if (parameters.contains("reload")) {
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
