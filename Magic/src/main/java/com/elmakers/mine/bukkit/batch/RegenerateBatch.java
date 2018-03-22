package com.elmakers.mine.bukkit.batch;

import com.elmakers.mine.bukkit.spell.UndoableSpell;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import javax.annotation.Nonnull;

import com.elmakers.mine.bukkit.api.block.BlockData;
import com.elmakers.mine.bukkit.block.BoundingBox;
import com.elmakers.mine.bukkit.block.UndoList;

public class RegenerateBatch extends SpellBatch {
	private static final BlockData[] template = new BlockData[0];
	
	private final UndoList restoredBlocks;
	private final World world;
	private final UndoableSpell undoableSpell;

	// These are chunk coords!
	private final int absx;
	private final int absz;
	private final int dx;
	private final int dz;
	private final int x;
	private final int z;
	private int ix = 0;
	private int iz = 0;
	
	private int blockY = 0;
	private int blockX = 0;
	private int blockZ = 0;
	
	private BlockData[] restoreBlocks;
	private int restoringIndex = 0;
	private boolean expand = false;

    private final @Nonnull BoundingBox bounds;

	private enum RegenerateState {
		SAVING, REGENERATING, RESTORING
	};
	
	private RegenerateState state;
	
	public RegenerateBatch(UndoableSpell spell, Location p1, Location p2) {
		super(spell);
		this.undoableSpell = spell;
        this.restoredBlocks = new UndoList(mage, spell.getName());
        this.restoredBlocks.setSpell(spell);
		this.restoredBlocks.setBatch(this);
		this.world = this.mage.getLocation().getWorld();
		this.state = RegenerateState.SAVING;
		
		int deltax = p2.getBlock().getChunk().getX() - p1.getChunk().getX();
		int deltaz = p2.getChunk().getZ() - p1.getChunk().getZ();

		absx = Math.abs(deltax) + 1;
		absz = Math.abs(deltaz) + 1;
		
		dx = (int)Math.signum(deltax);
		dz = (int)Math.signum(deltaz);

		x = p1.getChunk().getX();
		z = p1.getChunk().getZ();
		
		bounds = new BoundingBox(p1.toVector(), p2.toVector());
	}

	@Override
    public int size() {
		return (absx * absz) * 16 * 16 * 256;
	}
	
	@Override
    public int remaining() {
		return (absx - ix) * (absz - iz) * 16 * 16 * 256;
	}
	
	public boolean checkDimension(int maxDimension) {
		// Convert to block coords
		return !(maxDimension > 0 && (absx * 16 > maxDimension || absz * 16 > maxDimension));
	}
	
	@Override
    public int process(int maxBlocks) {
		int processedBlocks = 0;
        if (state == RegenerateState.SAVING && expand && !undoableSpell.isUndoable())
        {
            state = RegenerateState.REGENERATING;
        }
		switch (state)
		{
		case SAVING:
			while (processedBlocks <= maxBlocks && ix < absx) {
				while (processedBlocks <= maxBlocks && blockY < 256) {
					Chunk chunk = world.getChunkAt(x + ix * dx, z + iz * dz);
					if (!chunk.isLoaded()) {
						chunk.load();
						return processedBlocks;
					}
					Block block = chunk.getBlock(blockX, blockY, blockZ);
					if (!undoableSpell.hasBuildPermission(block) || !undoableSpell.hasBreakPermission(block)) {
						undoableSpell.sendMessage(undoableSpell.getMessage("insufficient_permission"));
						finish();
						return processedBlocks;
					}
					if (!expand && !bounds.contains(block.getLocation().toVector())) {
						restoredBlocks.add(block);
					} else {
						registerForUndo(block);
					}
					processedBlocks++;
					
					blockX++;
					if (blockX > 15) {
						blockX = 0;
						blockZ++;
						if (blockZ > 15) {
							blockZ = 0;
							blockY++;
						}
					}
				}

				if (blockY >= 256) {
					blockX = 0;
					blockZ = 0;
					blockY = 0;
					iz++;
					if (iz >= absz) {
						iz = 0;
						ix++;
					}
				}
			}

			if (ix >= absx) 
			{
				state = RegenerateState.REGENERATING;
				ix = 0;
				iz = 0;
			}
			break;
		case REGENERATING:
			while (processedBlocks <= maxBlocks && ix < absx) {
				Chunk chunk = world.getChunkAt(x + ix * dx, z + iz * dz);
				if (!chunk.isLoaded()) {
					chunk.load();
					return processedBlocks;
				}
				// Note that we've already done permissions checks for every block in this chunk.
				processedBlocks += 256 * 16 * 16;
				world.regenerateChunk(chunk.getX(), chunk.getZ());
			
				iz++;
				if (iz >= absz) {
					iz = 0;
					ix++;
				}
			}
			
			if (ix >= absx) 
			{
				restoreBlocks = restoredBlocks.toArray(template);
                if (expand && !undoableSpell.isUndoable()) {
                    finish();
                } else {
                    state = RegenerateState.RESTORING;
                }
			}
			break;
		case RESTORING:
			while (restoreBlocks != null && processedBlocks < maxBlocks && restoringIndex < restoreBlocks.length) {
				restoreBlocks[restoringIndex].restore();
				restoringIndex++;
				processedBlocks++;
			}
			if (restoreBlocks == null || restoringIndex >= restoredBlocks.size()) {
				finish();
			}
			break;
		}
		
		return processedBlocks;
	}
	
	public int getXSize() {
		return absx;
	}
	
	public int getZSize() {
		return absz;
	}

	public void setExpand(boolean expand) {
		this.expand = expand;
	}
	
	@Override
	public void finish() {
		if (!finished) {
			UndoList modified = undoableSpell.getUndoList();
			modified.prune();
			restoredBlocks.setBatch(null);
			super.finish();
		}
	}
}
