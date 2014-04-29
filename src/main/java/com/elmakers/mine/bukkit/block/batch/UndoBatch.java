package com.elmakers.mine.bukkit.block.batch;

import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.api.block.BlockData;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.block.UndoList;

public class UndoBatch extends UndoableBatch {
	private static final BlockData[] template = new BlockData[0];
	private final BlockData[] undoBlocks;
	private int undoIndex = 0;
	private boolean finishedAttachables = false;

	private final Set<Material> attachables;
	private final Set<Material> attachablesWall;
	private final Set<Material> attachablesDouble;
	private final Set<Material> delayed;
	
	public UndoBatch(Mage mage, UndoList blockList) {
		this(mage, blockList, null);
	}
	
	public UndoBatch(Mage mage, UndoList blockList, UndoList redoList) {
		super(mage, redoList);
		
		this.undoBlocks = blockList.toArray(template);
		this.attachables = controller.getMaterialSet("attachable");
		this.attachablesWall = controller.getMaterialSet("attachable_wall");
		this.attachablesDouble = controller.getMaterialSet("attachable_double");
		this.delayed = controller.getMaterialSet("delayed");
		
		// It's a little weird to use UndoableBatch as a base class, but
		// A) We could theoretically support a "Redo" queue in the future
		// B) This nicely handles volume updates (dynmap, etc).
		// But by default we won't actually add this to the undo queue, that'd be confusing.
		this.bypassUndo = true;
	}

	public int size() {
		return undoList.size();
	}
	
	public int remaining() {
		return undoList.size() - undoIndex;
	}
	
	public int process(int maxBlocks) {
		int processedBlocks = 0;
		while (undoBlocks != null && undoIndex < undoBlocks.length && processedBlocks < maxBlocks) {
			BlockData blockData = undoBlocks[undoIndex];
			Block block = blockData.getBlock();
			if (!block.getChunk().isLoaded()) {
				block.getChunk().load();
				break;
			}
			Material material = block.getType();
			boolean isAttachable = attachables.contains(material) || attachablesWall.contains(material) 
					|| attachablesDouble.contains(material) || delayed.contains(material);
			if ((isAttachable && !finishedAttachables) || (!isAttachable && finishedAttachables)) {
				
				registerForUndo(blockData);
				
				if (!undoList.undo(blockData)) {
					break;
				}
			}
			undoIndex++;
			processedBlocks++;
		}
		if (undoBlocks == null || (undoIndex >= undoBlocks.length && finishedAttachables)) {
			finish();
		} else if (undoIndex >= undoBlocks.length) {
			finishedAttachables = true;
			undoIndex = 0;
		}
		
		return processedBlocks;
	}
}
