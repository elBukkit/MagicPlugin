package com.elmakers.mine.bukkit.plugins.magic.blocks;

public interface BlockBatch {
	// Return the number of blocks processed. The batch is assumed to be complete
	// if it returns 0.
	public int process(int maxBlocks);
	
	public boolean isFinished();
}
