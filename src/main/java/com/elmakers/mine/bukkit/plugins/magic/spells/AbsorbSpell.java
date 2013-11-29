package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.plugins.magic.PlayerSpells;
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
		PlayerSpells playerSpells = spells.getPlayerSpells(player);
		Wand wand = playerSpells.getActiveWand();
		if (wand == null) {
			castMessage("This spell requires a wand");
			return SpellResult.NO_TARGET;
		}
		Material material = Material.AIR;
		Set<Material> buildingMaterials = spells.getBuildingMaterials();
		byte data = 0;
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
		
		if (material == null || material == Material.AIR || !buildingMaterials.contains(material))
		{
			return SpellResult.NO_TARGET;
		}
		
		// Add to the wand
		wand.addMaterial(material, data);
		castMessage("Absorbing some " + material.name().toLowerCase());
		
		return SpellResult.SUCCESS;
	}
	
	@Override
	public boolean usesMaterial() {
		return true;
	}
}
