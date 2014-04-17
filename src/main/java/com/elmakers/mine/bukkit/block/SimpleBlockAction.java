package com.elmakers.mine.bukkit.block;

import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.plugins.magic.spell.SpellResult;

public class SimpleBlockAction implements BlockAction
{
	protected BlockList blocks = null;

	public SimpleBlockAction()
	{
		blocks = new BlockList();
	}

	public SpellResult perform(Block block)
	{
		blocks.add(block);
		return SpellResult.CAST;
	}

	public BlockList getBlocks()
	{
		return blocks;
	}

}
