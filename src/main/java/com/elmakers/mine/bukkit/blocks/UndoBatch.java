package com.elmakers.mine.bukkit.blocks;

import java.util.ArrayList;

import com.elmakers.mine.bukkit.plugins.magic.MagicController;

public class UndoBatch extends VolumeBatch {
	private final BlockList blockList;
	private int blockIndex = 0;
	
	public UndoBatch(MagicController spells, BlockList blockList) {
		super(spells, blockList.getWorldName());
		this.blockList = blockList;
	}
	
	public int process(int maxBlocks) {
		int processedBlocks = 0;
		ArrayList<BlockData> undoList = blockList.getBlockList();
		while (undoList != null && blockIndex < undoList.size()) {
			BlockData blockData = undoList.get(blockIndex);
			if (!blockData.undo()) {
				break;
			}
			updateBlock(blockData);
			blockIndex++;
			processedBlocks++;
		}
		if (undoList == null || blockIndex >= undoList.size()) {
			finish();
		}
		
		return processedBlocks;
	}
}
