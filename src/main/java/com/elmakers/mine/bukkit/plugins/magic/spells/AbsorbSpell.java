package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.plugins.magic.Wand;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class AbsorbSpell extends Spell 
{
	@SuppressWarnings("deprecation")
	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		Material material = Material.AIR;
		List<Material> buildingMaterials = spells.getBuildingMaterials();

		material = parameters.getMaterial("material", material);
		byte data = 0;
		if (material == Material.AIR || !buildingMaterials.contains(material))
		{
			if (!isUnderwater())
			{
				noTargetThrough(Material.STATIONARY_WATER);
				noTargetThrough(Material.WATER);
			}
			Block target = getTargetBlock();
	
			if (target == null) 
			{
				castMessage("No target");
				return SpellResult.NO_TARGET;
			}
	
			material = target.getType();
			data = target.getData();
		}
		
		// Add to the wand
		Wand wand = Wand.getActiveWand(player);
		wand.addMaterial(spells.getPlayerSpells(player), material, data);
		castMessage("Absorbing some " + material.name().toLowerCase());
		
		return SpellResult.SUCCESS;
	}
}
