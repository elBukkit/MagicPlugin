package com.elmakers.mine.bukkit.block;

public interface BlockBatch {
	// Return the number of blocks processed.
	public int process(int maxBlocks);
	
	public boolean isFinished();
	public void finish();
	
	public int size();
	public int remaining();
}
