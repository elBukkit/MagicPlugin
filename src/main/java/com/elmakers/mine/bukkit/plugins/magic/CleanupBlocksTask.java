package com.elmakers.mine.bukkit.plugins.magic;

import com.elmakers.mine.bukkit.dao.BlockList;

public class CleanupBlocksTask implements Runnable
{
	protected BlockList undoBlocks;

	public CleanupBlocksTask(BlockList cleanup)
	{
		this.undoBlocks = cleanup;
	}

	public void run()
	{
		this.undoBlocks.undo();
	}
}
