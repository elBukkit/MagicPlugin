package com.elmakers.mine.bukkit.block;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.plugins.magic.Mage;
import com.elmakers.mine.bukkit.plugins.magic.spell.BlockSpell;

public class RegenerateBatch extends VolumeBatch {
	private final BlockList regeneratedBlocks = new BlockList();
	private final BlockList restoredBlocks = new BlockList();
	private final World world;
	private final Mage mage;
	private final BlockSpell spell;

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
	
	private int restoringIndex = 0;
	
	private BoundingBox bounds = new BoundingBox();
	
	private enum RegenerateState {
		SAVING, REGENERATING, RESTORING
	};
	
	private RegenerateState state;
	
	public RegenerateBatch(BlockSpell spell, Location p1, Location p2) {
		super(spell.getMage().getController(), p1.getWorld().getName());
		this.mage = spell.getMage();
		this.world = this.mage.getPlayer().getWorld();
		this.spell = spell;
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

	public int size() {
		return (absx * absz) * 16 * 16 * 256;
	}
	
	public int remaining() {
		return (absx - ix) * (absz - iz) * 16 * 16 * 256;
	}
	
	public boolean checkDimension(int maxDimension) {
		// Convert to block coords
		return !(maxDimension > 0 && (absx * 16 > maxDimension || absz * 16 > maxDimension));
	}
	
	public int process(int maxBlocks) {
		int processedBlocks = 0;
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
					if (!spell.hasBuildPermission(block)) {
						spell.sendMessage(spell.getMessage("insufficient_permissions"));
						finish();
						return processedBlocks;
					}
					if (!bounds.contains(block.getLocation().toVector())) {
						restoredBlocks.add(block);
					} else {
						regeneratedBlocks.add(block);
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
				Bukkit.getLogger().info("Saved " + restoredBlocks.size() 
						+ " blocks for restorationg, " + regeneratedBlocks.size() + " for regeneration and undo queue");
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
				state = RegenerateState.RESTORING;
			}
			break;
		case RESTORING:
			while (processedBlocks < maxBlocks && restoringIndex < restoredBlocks.size()) {
				restoredBlocks.get(restoringIndex).restore();
				restoringIndex++;
				processedBlocks++;
			}
			if (restoringIndex >= restoredBlocks.size()) {
				finish();
			}
			break;
		}
		
		return processedBlocks;
	}
	
	@Override
	public void finish() {
		if (!finished) {
			super.finish();

			spell.registerForUndo(regeneratedBlocks);
			String message = spell.getMessage("cast_finish");
			spell.sendMessage(message);
		}
	}
	
	public int getXSize() {
		return absx;
	}
	
	public int getZSize() {
		return absz;
	}
}
