package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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
		MapView newMap = SkinRenderer.getPlayerPortrait(playerName, spells);
		ItemStack newMapItem = new ItemStack(Material.MAP, 1, newMap.getId());
		ItemMeta meta = newMapItem.getItemMeta();
		meta.setDisplayName("Photo of " + playerName);
		newMapItem.setItemMeta(meta);
		player.getWorld().dropItemNaturally(player.getLocation(), newMapItem);
		return SpellResult.SUCCESS;
	}
}
