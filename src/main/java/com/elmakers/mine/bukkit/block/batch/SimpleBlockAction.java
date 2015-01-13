package com.elmakers.mine.bukkit.block.batch;

import com.elmakers.mine.bukkit.api.action.BlockAction;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.UndoList;
import org.bukkit.configuration.ConfigurationSection;

public class SimpleBlockAction implements BlockAction
{
	protected final MageController controller;
	protected final UndoList modified;
	
	public SimpleBlockAction(MageController controller, UndoList undoList)
	{
		modified = undoList;
		this.controller = controller;
	}

	public SpellResult perform(ConfigurationSection parameters, Block block)
	{
		if (controller != null)
		{
			controller.updateBlock(block);
		}
		if (modified != null)
		{
			modified.add(block);
		}
		return SpellResult.CAST;
	}

	public UndoList getBlocks()
	{
		return modified;
	}
}
