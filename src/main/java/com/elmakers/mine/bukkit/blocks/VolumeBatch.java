package com.elmakers.mine.bukkit.blocks;

import org.bukkit.util.BlockVector;

import com.elmakers.mine.bukkit.plugins.magic.MagicController;

public abstract class VolumeBatch implements BlockBatch {
	protected final MagicController controller;
	private String worldName;
	private boolean finished = false;

	private Integer minx = null;
	private Integer miny = null;
	private Integer minz = null;
	private Integer maxx = null;
	private Integer maxy = null;
	private Integer maxz = null;
	
	public VolumeBatch(MagicController controller, String worldName) {
		this.controller = controller;
		this.worldName = worldName;
	}
	
	protected void updateBlock(BlockData data) {
		BlockVector location = data.getPosition();
		updateBlock(worldName, location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}
	
	protected void updateBlock(int x, int y, int z) {
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

	
	protected void updateBlock(String worldName, int x, int y, int z) {
		this.worldName = worldName;
		updateBlock(x, y, z);
	}
	
	protected void finish() {
		if (!finished) {
			if (worldName != null && minx !=null && miny != null && minz != null && maxx !=null && maxy != null && maxz != null) {
				controller.updateVolume(worldName, minx, miny, minz, maxx, maxy, maxz);
			}
			finished = true;
		}
	}
	
	public boolean isFinished() {
		return finished;
	}
}
