package com.elmakers.mine.bukkit.block;

import java.util.ArrayList;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.plugins.magic.MagicController;

public class UndoBatch extends VolumeBatch {
	private final BlockList blockList;
	private int blockIndex = 0;
	private boolean finishedAttachables = false;

	private final Set<Material> attachables;
	private final Set<Material> attachablesWall;
	private final Set<Material> attachablesDouble;
	private final Set<Material> delayed;
	
	public UndoBatch(MagicController controller, BlockList blockList) {
		super(controller, blockList.getWorldName());
		this.blockList = blockList;
		this.attachables = controller.getMaterialSet("attachable");
		this.attachablesWall = controller.getMaterialSet("attachable_wall");
		this.attachablesDouble = controller.getMaterialSet("attachable_double");
		this.delayed = controller.getMaterialSet("delayed");
	}

	public int size() {
		return blockList.size();
	}
	
	public int remaining() {
		return blockList.size() - blockIndex;
	}
	
	public int process(int maxBlocks) {
		int processedBlocks = 0;
		ArrayList<BlockData> undoList = blockList.getBlockList();
		while (undoList != null && blockIndex < undoList.size() && processedBlocks < maxBlocks) {
			BlockData blockData = undoList.get(blockIndex);
			Block block = blockData.getBlock();
			if (!block.getChunk().isLoaded()) {
				block.getChunk().load();
				break;
			}
			Material material = block.getType();
			boolean isAttachable = attachables.contains(material) || attachablesWall.contains(material) 
					|| attachablesDouble.contains(material) || delayed.contains(material);
			if ((isAttachable && !finishedAttachables) || (!isAttachable && finishedAttachables)) {
				if (!blockList.undo(blockData)) {
					break;
				}
				updateBlock(blockData);
			}
			blockIndex++;
			processedBlocks++;
		}
		if (undoList == null || (blockIndex >= undoList.size() && finishedAttachables)) {
			finish();
		} else if (blockIndex >= undoList.size()) {
			finishedAttachables = true;
			blockIndex = 0;
		}
		
		return processedBlocks;
	}
}
