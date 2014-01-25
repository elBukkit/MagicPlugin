package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Material;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.blocks.BlockData;
import com.elmakers.mine.bukkit.blocks.BlockList;
import com.elmakers.mine.bukkit.plugins.magic.BrushSpell;
import com.elmakers.mine.bukkit.plugins.magic.MaterialBrush;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.UndoQueue;
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
		boolean usedTarget = false;
		targetThrough(Material.GLASS);
		Block target = getTargetBlock();

		if (target != null)
		{
			UndoQueue undoQueue = mage.getUndoQueue();
			transmuteAction = undoQueue.getLast(target);
			usedTarget = transmuteAction != null;
		}

		if (transmuteAction == null)
		{
			UndoQueue undoQueue = mage.getUndoQueue();
			transmuteAction = undoQueue.getLast();
		}

		if (transmuteAction == null)
		{
			sendMessage("Nothing to transmute");
			return SpellResult.NO_TARGET;
		}

		MaterialBrush buildWith = getMaterialBrush();
		Material material = buildWith.getMaterial();
		
		for (BlockData undoBlock : transmuteAction)
		{
			Block block = undoBlock.getBlock();
			buildWith.modify(block);
		}

		if (usedTarget)
		{
			castMessage("You transmute your target structure to " + material.name().toLowerCase());
		}
		else
		{
			castMessage("You transmute your last structure to " + material.name().toLowerCase());
		}

		return SpellResult.SUCCESS;
	}
}
