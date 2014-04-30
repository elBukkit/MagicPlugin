package com.elmakers.mine.bukkit.spell.builtin;

import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.spell.BrushSpell;

public class AbsorbSpell extends BrushSpell 
{   
	@SuppressWarnings("deprecation")
	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		Wand wand = mage.getActiveWand();
		if (wand == null) {
			return SpellResult.FAIL;
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
		
		// Add to the wand
		MaterialAndData mat = new MaterialAndData(material, data);
		wand.addBrush(mat.getKey());
		
		// And activate it
		wand.setActiveBrush(mat.getKey());
		
		return SpellResult.CAST;
	}
	
	@Override
	public boolean hasBrushOverride() 
	{
		return true;
	}
	
	@Override
	public boolean isUndoable()
	{
		return false;
	}
}
