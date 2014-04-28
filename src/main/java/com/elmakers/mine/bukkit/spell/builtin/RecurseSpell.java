package com.elmakers.mine.bukkit.spell.builtin;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.block.batch.BlockRecurse;
import com.elmakers.mine.bukkit.block.batch.ReplaceMaterialAction;
import com.elmakers.mine.bukkit.spell.BrushSpell;

public class RecurseSpell extends BrushSpell 
{
	private final BlockRecurse blockRecurse = new BlockRecurse();

	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		Block targetBlock = getTargetBlock();
		
		if (targetBlock == null) 
		{
			return SpellResult.NO_TARGET;
		}
		if (!hasBuildPermission(targetBlock)) 
		{
			return SpellResult.INSUFFICIENT_PERMISSION;
		}
		
		MaterialBrush buildWith = getMaterialBrush();

		int size = parameters.getInt("size", 8);
		size = (int)(mage.getRadiusMultiplier() * size);
		blockRecurse.setMaxRecursion(size);

		Material targetMaterial = targetBlock.getType();
		ReplaceMaterialAction action = new ReplaceMaterialAction(mage, targetBlock, buildWith);

		// A bit hacky, but is very handy!
		if (targetMaterial == Material.STATIONARY_WATER || targetMaterial == Material.WATER)
		{
			for (byte i = 0; i < 9; i++) {
				action.addReplaceable(Material.STATIONARY_WATER, i);
				action.addReplaceable(Material.WATER, i);
			}
		}
		else if (targetMaterial == Material.STATIONARY_LAVA || targetMaterial == Material.LAVA)
		{
			for (byte i = 0; i < 9; i++) {
				action.addReplaceable(Material.STATIONARY_LAVA, i);
				action.addReplaceable(Material.LAVA, i);
			}
		}
		else if (targetMaterial == Material.SNOW) {
			for (byte i = 0; i < 8; i++) {
				action.addReplaceable(Material.SNOW, i);
			}
		}
		blockRecurse.recurse(targetBlock, action);
		mage.registerForUndo(action.getBlocks());
		controller.updateBlock(targetBlock);
		
		return SpellResult.CAST;
	}
}
