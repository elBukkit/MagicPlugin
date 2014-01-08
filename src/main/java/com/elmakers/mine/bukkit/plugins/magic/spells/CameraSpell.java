package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.plugins.magic.Target;
import com.elmakers.mine.bukkit.utilities.URLMap;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class CameraSpell extends Spell
{
	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		ItemStack newMapItem = null;
		
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
				newMapItem = URLMap.getURLItem(url, x, y, width, height, mapName);
			}
		}
		
		if (newMapItem == null) {
			this.targetEntity(Player.class);
			Target target = getTarget();
			String playerName = parameters.getString("name");
			if (playerName == null) 
			{
				Player targetPlayer = player;
				if (target != null && target.isEntity() && (target.getEntity() instanceof Player))
				{
					castMessage("CLICK!");
					targetPlayer = (Player)target.getEntity();
				} 
				else 
				{
					castMessage("Selfie!");
				}
				playerName = targetPlayer.getName();
			}
			if (parameters.containsKey("reload")) {
				URLMap.forceReloadPlayerPortrait(playerName);
			}
			newMapItem = URLMap.getPlayerPortrait(playerName);
		}
		if (newMapItem == null) {
			sendMessage("Failed to load photo");
			return SpellResult.FAILURE;
		}
		player.getWorld().dropItemNaturally(player.getLocation(), newMapItem);
		player.getWorld().playEffect(player.getEyeLocation(), Effect.SMOKE, 0);
		
		return SpellResult.SUCCESS;
	}
}
