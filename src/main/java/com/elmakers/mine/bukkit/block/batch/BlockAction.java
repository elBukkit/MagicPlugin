package com.elmakers.mine.bukkit.block.batch;

import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.UndoList;

public interface BlockAction
{
	public SpellResult perform(Block block);
	public UndoList getBlocks();
}
