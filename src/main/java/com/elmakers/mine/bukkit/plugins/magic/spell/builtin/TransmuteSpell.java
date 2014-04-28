package com.elmakers.mine.bukkit.plugins.magic.spell.builtin;

import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.block.BlockData;
import com.elmakers.mine.bukkit.api.block.BlockList;
import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.api.block.UndoQueue;
import com.elmakers.mine.bukkit.plugins.magic.spell.BrushSpell;

public class TransmuteSpell extends BrushSpell
{
	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
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
