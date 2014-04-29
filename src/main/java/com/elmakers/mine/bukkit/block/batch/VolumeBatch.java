package com.elmakers.mine.bukkit.block.batch;

import org.bukkit.block.Block;
import org.bukkit.util.BlockVector;

import com.elmakers.mine.bukkit.api.block.BlockBatch;
import com.elmakers.mine.bukkit.api.block.BlockData;
import com.elmakers.mine.bukkit.api.magic.MageController;

public abstract class VolumeBatch implements BlockBatch {
	protected final MageController controller;
	private String worldName;
	
	protected boolean finished = false;

	private Integer minx = null;
	private Integer miny = null;
	private Integer minz = null;
	private Integer maxx = null;
	private Integer maxy = null;
	private Integer maxz = null;
	
	public VolumeBatch(MageController controller) {
		this.controller = controller;
		this.worldName = null;
	}
	
	protected void updateBlock(BlockData data) {
		BlockVector location = data.getPosition();
		updateBlock(data.getWorldName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}
	
	protected void updateBlock(Block block) {
		updateBlock(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
	}
	
	protected void updateBlock(String worldName, int x, int y, int z) {
		if (this.worldName != null && !this.worldName.equals(worldName)) {
			return;
		}
		if (this.worldName == null) this.worldName = worldName;
		if (minx == null) {
			minx = x;
			miny = y;
			minz = z;
			maxx = x;
			maxy = y;
			maxz = z;
		} else {
			minx = Math.min(x, minx);
			miny = Math.min(z, miny);
			minz = Math.min(z, minz);
			maxx = Math.max(x, maxx);
			maxy = Math.max(z, maxy);
			maxz = Math.max(z, maxz);
		}
	}
	
	public void finish() {
		if (!finished) {
			if (worldName != null && minx != null && miny != null && minz != null && maxx !=null && maxy != null && maxz != null) {
				controller.updateVolume(worldName, minx, miny, minz, maxx, maxy, maxz);
			}
			finished = true;
		}
	}
	
	public boolean isFinished() {
		return finished;
	}
}
