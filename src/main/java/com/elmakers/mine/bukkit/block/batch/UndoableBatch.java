package com.elmakers.mine.bukkit.block.batch;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.api.block.BlockBatch;
import com.elmakers.mine.bukkit.api.block.BlockData;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.block.UndoList;

public abstract class UndoableBatch implements BlockBatch {
	protected static int VOLUME_UPDATE_THRESHOLD = 32;
	
	protected final MageController controller;
	protected final UndoList undoList;
	protected final Mage mage;
	protected boolean bypassUndo = false; 
	protected boolean finished = false;
	
	public UndoableBatch(Mage mage) {
		this(mage, null);
	}
	
	public UndoableBatch(Mage mage, UndoList undoList) {
		this.controller = mage.getController();
		this.mage = mage;
		this.undoList = undoList == null ?  new UndoList(controller.getPlugin()) : undoList;
	}
	
	public void registerForUndo(BlockData block) {
		undoList.add(block);
	}
	
	public void registerForUndo(Block block) {
		undoList.add(block);
	}
	
	public void registerForUndo(Entity entity) {
		undoList.add(entity);
	}
	
	public void finish() {
		if (!finished) {
			finished = true;
			if (!bypassUndo) {
				mage.registerForUndo(undoList);
			}
			if (undoList.size() > VOLUME_UPDATE_THRESHOLD) {
				controller.update(undoList);
			} else {
				for (BlockData blockData : undoList) {
					controller.updateBlock(blockData.getWorldName(), blockData.getPosition().getBlockX(), blockData.getPosition().getBlockY(), blockData.getPosition().getBlockZ());
				}
			}
		}
	}
	
	public void setBypassUndo(boolean bypassUndo) {
		this.bypassUndo = bypassUndo;
	}
	
	@Override
	public boolean isFinished() {
		return finished;
	}
}
