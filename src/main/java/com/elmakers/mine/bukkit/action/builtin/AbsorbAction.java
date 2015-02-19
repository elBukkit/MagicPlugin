package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.api.action.BlockAction;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.spell.BaseSpellAction;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Set;

public class AbsorbAction extends BaseSpellAction implements BlockAction
{   
	@SuppressWarnings("deprecation")
	@Override
	public SpellResult perform(ConfigurationSection parameters, Block target)
	{
		Wand wand = getMage().getActiveWand();
		if (wand == null)
		{
			return SpellResult.FAIL;
		}
	
		Material material = target.getType();
		byte data = target.getData();

		Set<Material> buildingMaterials = getController().getBuildingMaterials();
        Set<Material> restrictedMaterials = getMage().getRestrictedMaterials();
		if (material == null || material == Material.AIR || !buildingMaterials.contains(material) || restrictedMaterials.contains(material))
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
}
