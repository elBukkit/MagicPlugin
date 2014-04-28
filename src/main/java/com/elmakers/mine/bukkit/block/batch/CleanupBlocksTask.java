package com.elmakers.mine.bukkit.block.batch;

import com.elmakers.mine.bukkit.block.BlockList;
import com.elmakers.mine.bukkit.api.magic.Mage;

public class CleanupBlocksTask implements Runnable
{
	protected Mage mage;
	protected BlockList undoBlocks;

	public CleanupBlocksTask(Mage mage, BlockList cleanup)
	{
		this.undoBlocks = cleanup;
		this.mage = mage;
	}

	public void run()
	{
		if (undoBlocks.undo(mage)) {
			mage.getUndoQueue().removeScheduledCleanup(undoBlocks);
		} else {
			// TODO: Retry limit?
			undoBlocks.scheduleCleanup(mage);
		}
	}
}
