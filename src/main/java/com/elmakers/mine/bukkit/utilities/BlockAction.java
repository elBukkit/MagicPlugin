package com.elmakers.mine.bukkit.utilities;

import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.plugins.magic.blocks.BlockList;

public interface BlockAction
{
	public SpellResult perform(Block block);
	public BlockList getBlocks();
}
