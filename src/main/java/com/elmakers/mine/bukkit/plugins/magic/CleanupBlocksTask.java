package com.elmakers.mine.bukkit.plugins.magic;

import com.elmakers.mine.bukkit.dao.BlockList;

public class CleanupBlocksTask implements Runnable
{
	protected BlockList undoBlocks;
	protected Spells spells;

	public CleanupBlocksTask(Spells spells, BlockList cleanup)
	{
		this.undoBlocks = cleanup;
		this.spells = spells;
	}

	public void run()
	{
		this.undoBlocks.undo(spells);
	}
}
