package com.elmakers.mine.bukkit.block;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import com.elmakers.mine.bukkit.plugins.magic.BlockSpell;
import com.elmakers.mine.bukkit.plugins.magic.Mage;

public class RegenerateBatch extends VolumeBatch {
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
	
	public RegenerateBatch(BlockSpell spell, Location p1, Location p2) {
		super(spell.getMage().getController(), p1.getWorld().getName());
		this.mage = spell.getMage();
		this.world = this.mage.getPlayer().getWorld();
		this.spell = spell;
		
		int deltax = p2.getBlock().getChunk().getX() - p1.getChunk().getX();
		int deltaz = p2.getChunk().getZ() - p1.getChunk().getZ();

		absx = Math.abs(deltax) + 1;
		absz = Math.abs(deltaz) + 1;
		
		dx = (int)Math.signum(deltax);
		dz = (int)Math.signum(deltaz);

		x = p1.getChunk().getX();
		z = p1.getChunk().getZ();
	}

	public int size() {
		return 0;
	}
	
	public int remaining() {
		return (absx - x) * (absz - z) * 16 * 16;
	}
	
	public boolean checkDimension(int maxDimension) {
		// Convert to block coords
		return !(maxDimension > 0 && (absx * 16 > maxDimension || absz * 16 > maxDimension));
	}
	
	public int process(int maxBlocks) {
		// Kind of generous?
		int maxChunks = (int)Math.ceil((float)maxBlocks / (16 * 16));
		int processedChunks = 0;
		
		while (processedChunks <= maxChunks && ix < absx) {
			Chunk chunk = world.getChunkAt(x + ix * dx, z + iz * dz);
			if (!chunk.isLoaded()) {
				chunk.load();
				return processedChunks;
			}
			// we should check all of y... ?
			if (mage.hasBuildPermission(chunk.getBlock(0, 70, 0)) || !mage.hasBuildPermission(chunk.getBlock(8, 32, 8))
				|| !mage.hasBuildPermission(chunk.getBlock(15, 120, 15))) {
				world.regenerateChunk(chunk.getX(), chunk.getZ());
			}
			processedChunks++;
		
			iz++;
			if (iz >= absz) {
				iz = 0;
				ix++;
			}
		}
		
		if (ix >= absx) 
		{
			finish();
			
		}
		
		return processedChunks * 16 * 16;
	}
	
	@Override
	public void finish() {
		if (!finished) {
			super.finish();
			
			String message = spell.getMessage("cast_finish");
			message = message.replace("$count", Integer.toString(getXSize() * getZSize()));
			spell.castMessage(message);
		}
	}
	
	public int getXSize() {
		return absx;
	}
	
	public int getZSize() {
		return absz;
	}
}
