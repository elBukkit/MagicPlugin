package com.elmakers.mine.bukkit.block;

import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.api.spell.SpellResult;

public interface BlockAction
{
	public SpellResult perform(Block block);
	public BlockList getBlocks();
}
