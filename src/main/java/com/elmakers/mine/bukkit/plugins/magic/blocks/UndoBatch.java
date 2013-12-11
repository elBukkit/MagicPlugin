package com.elmakers.mine.bukkit.plugins.magic.blocks;

import java.util.ArrayList;

import com.elmakers.mine.bukkit.dao.BlockData;
import com.elmakers.mine.bukkit.dao.BlockList;

public class UndoBatch implements BlockBatch {
	private final BlockList blockList;
	private int blockIndex = 0;
	
	public UndoBatch(BlockList blockList) {
		this.blockList = blockList;
	}
	
	public int process(int maxBlocks) {
		int processedBlocks = 0;
		ArrayList<BlockData> undoList = blockList.getBlockList();
		while (undoList != null && blockIndex < undoList.size()) {
			BlockData blockData = undoList.get(blockIndex);
			if (!blockData.undo()) {
				return processedBlocks;
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
