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
		Target target = getTarget();
		if (!target.isBlock()) return SpellResult.NO_TARGET;
		Block attachBlock = getLastBlock();

		MaterialBrush buildWith = getMaterialBrush();
		buildWith.setTarget(attachBlock.getLocation());

		if (!hasBuildPermission(attachBlock)) {
			return SpellResult.INSUFFICIENT_PERMISSION;
		}
		BlockList placedBlocks = new BlockList();
		placedBlocks.add(attachBlock);
		buildWith.modify(attachBlock);

		registerForUndo(placedBlocks);
		controller.updateBlock(attachBlock);

		return SpellResult.CAST;
	}
}
