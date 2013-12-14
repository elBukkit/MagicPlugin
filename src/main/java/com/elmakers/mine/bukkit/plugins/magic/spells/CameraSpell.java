package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.plugins.magic.Target;
import com.elmakers.mine.bukkit.utilities.URLMapRenderer;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class CameraSpell extends Spell
{
	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		// Check for special case url
		ItemStack newMapItem = null;
		String url = parameters.getString("url");
		if (url != null) {
			int x = parameters.getInt("x", 0);
			int y = parameters.getInt("y", 0);
			int width = parameters.getInt("width", 0);
			int height = parameters.getInt("height", 0);
			newMapItem = URLMapRenderer.getURLItem(url, x, y, width, height, "Photo");
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
				URLMapRenderer.forceReloadPlayerPortrait(playerName);
			}
			newMapItem = URLMapRenderer.getPlayerPortrait(playerName);
		}
		if (newMapItem == null) {
			sendMessage("Failed to load photo");
			return SpellResult.FAILURE;
		}
		player.getWorld().dropItemNaturally(player.getLocation(), newMapItem);
		
		// Kinda hacky, but safe.
		URLMapRenderer.save();
		return SpellResult.SUCCESS;
	}
}
