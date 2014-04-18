package com.elmakers.mine.bukkit.plugins.magic.spell.builtin;

import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.plugins.magic.spell.TargetingSpell;

public class HatSpell extends TargetingSpell 
{   
	@SuppressWarnings("deprecation")
	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		Player player = getPlayer();
		if (player == null) {
			return SpellResult.PLAYER_REQUIRED;
		}
		Material material = Material.AIR;
		Set<Material> buildingMaterials = controller.getBuildingMaterials();
		byte data = 0;
		if (!isUnderwater())
		{
			noTargetThrough(Material.STATIONARY_WATER);
			noTargetThrough(Material.WATER);
		}
		Block target = getTargetBlock();

		if (target == null) 
		{
			return SpellResult.NO_TARGET;
		}
	
		material = target.getType();
		data = target.getData();
		
		if (material == null || material == Material.AIR || !buildingMaterials.contains(material))
		{
			return SpellResult.NO_TARGET;
		}
		ItemStack hatItem = new ItemStack(material, 1, (short)data);
		ItemStack itemStack = player.getInventory().getHelmet();
		player.getInventory().setHelmet(hatItem);
		if (itemStack != null && itemStack.getType() != Material.AIR) {
			player.getWorld().dropItemNaturally(player.getLocation(), itemStack);
		}
		return SpellResult.CAST;
	}
}
