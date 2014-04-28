package com.elmakers.mine.bukkit.spell.builtin;

import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.BlockList;
import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.spell.BrushSpell;
import com.elmakers.mine.bukkit.utility.Target;

public class PlaceSpell extends BrushSpell 
{
	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		Target attachToBlock = getTarget();
		if (!attachToBlock.isValid()) return SpellResult.NO_TARGET;
		Block placeBlock = getPreviousBlock();

		MaterialBrush buildWith = getMaterialBrush();
		buildWith.setTarget(attachToBlock.getLocation(), placeBlock.getLocation());

		if (!hasBuildPermission(placeBlock)) {
			return SpellResult.INSUFFICIENT_PERMISSION;
		}
		BlockList placedBlocks = new BlockList();
		placedBlocks.add(placeBlock);
		buildWith.modify(placeBlock);

		registerForUndo(placedBlocks);
		controller.updateBlock(placeBlock);

		return SpellResult.CAST;
	}
}
