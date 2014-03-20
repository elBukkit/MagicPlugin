package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.blocks.BlockData;
import com.elmakers.mine.bukkit.blocks.BlockList;
import com.elmakers.mine.bukkit.blocks.MaterialBrush;
import com.elmakers.mine.bukkit.blocks.UndoQueue;
import com.elmakers.mine.bukkit.plugins.magic.BrushSpell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class TransmuteSpell extends BrushSpell
{
	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{	
		BlockList transmuteAction = null;

		/*
		 * Use target if targeting
		 */
		Block target = getTargetBlock();

		if (target != null)
		{
			UndoQueue undoQueue = mage.getUndoQueue();
			transmuteAction = undoQueue.getLast(target);
		}

		if (transmuteAction == null)
		{
			UndoQueue undoQueue = mage.getUndoQueue();
			transmuteAction = undoQueue.getLast();
		}

		if (transmuteAction == null)
		{
			return SpellResult.NO_TARGET;
		}

		MaterialBrush buildWith = getMaterialBrush();
		for (BlockData undoBlock : transmuteAction)
		{
			Block block = undoBlock.getBlock();
			buildWith.modify(block);
		}

		return SpellResult.CAST;
	}
}
