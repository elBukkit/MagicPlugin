package com.elmakers.mine.bukkit.block.batch;

import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.BlockList;

public interface BlockAction
{
	public SpellResult perform(Block block);
	public BlockList getBlocks();
}
