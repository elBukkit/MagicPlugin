package com.elmakers.mine.bukkit.plugins.magic.blocks;

import java.util.ArrayList;

import com.elmakers.mine.bukkit.dao.BlockData;
import com.elmakers.mine.bukkit.dao.BlockList;
import com.elmakers.mine.bukkit.plugins.magic.Spells;

public class UndoBatch implements BlockBatch {
	private final BlockList blockList;
	private int blockIndex = 0;
	private final Spells spells;
	
	public UndoBatch(Spells spells, BlockList blockList) {
		this.blockList = blockList;
		this.spells = spells;
	}
	
	public int process(int maxBlocks) {
		int processedBlocks = 0;
		boolean updated = false; // Only update map once for efficiency.
		ArrayList<BlockData> undoList = blockList.getBlockList();
		while (undoList != null && blockIndex < undoList.size()) {
			BlockData blockData = undoList.get(blockIndex);
			if (!blockData.undo()) {
				return processedBlocks;
			}
			if (!updated) {
				updated = true;
				spells.updateBlock(blockData.getBlock());
			}
			blockIndex++;
			processedBlocks++;
		}
		
		return processedBlocks;
	}
	
	public boolean isFinished() {
		ArrayList<BlockData> undoList = blockList.getBlockList();
		return undoList == null || blockIndex >= undoList.size();
	}
}
