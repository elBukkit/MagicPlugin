package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.blocks.BlockList;
import com.elmakers.mine.bukkit.blocks.MaterialBrush;
import com.elmakers.mine.bukkit.plugins.magic.BrushSpell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.Target;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class PlaceSpell extends BrushSpell 
{
	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		Target attachToBlock = getTarget();
		if (!attachToBlock.isBlock()) return SpellResult.NO_TARGET;
		Block placeBlock = getLastBlock();

		MaterialBrush buildWith = getMaterialBrush();
		buildWith.setTarget(attachToBlock.getLocation(), placeBlock.getLocation());
		buildWith.setTarget(attachToBlock.getLocation());

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
