package com.elmakers.mine.bukkit.block;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.plugins.magic.BrushSpell;
import com.elmakers.mine.bukkit.plugins.magic.Mage;

public class FillBatch extends VolumeBatch {
	private final BlockList filledBlocks = new BlockList();
	private final MaterialBrush brush;
	private final World world;
	private final Mage mage;
	private final BrushSpell spell;

	private final int absx;
	private final int absy;
	private final int absz;
	private final int dx;
	private final int dy;
	private final int dz;
	private final int x;
	private final int y;
	private final int z;
	private int ix = 0;
	private int iy = 0;
	private int iz = 0;
	
	private boolean spawnFallingBlocks = false;
	private Vector fallingBlockVelocity = null;
	
	public FillBatch(BrushSpell spell, Location p1, Location p2, MaterialBrush brush) {
		super(spell.getMage().getController(), p1.getWorld().getName());
		this.spell = spell;
		this.brush = brush;
		this.mage = spell.getMage();
		this.world = p1.getWorld();
		
		int deltax = p2.getBlockX() - p1.getBlockX();
		int deltay = p2.getBlockY() - p1.getBlockY();
		int deltaz = p2.getBlockZ() - p1.getBlockZ();

		absx = Math.abs(deltax) + 1;
		absy = Math.abs(deltay) + 1;
		absz = Math.abs(deltaz) + 1;
		
		dx = (int)Math.signum(deltax);
		dy = (int)Math.signum(deltay);
		dz = (int)Math.signum(deltaz);

		x = p1.getBlockX();
		y = p1.getBlockY();
		z = p1.getBlockZ();
	}

	public int size() {
		return filledBlocks.size();
	}
	
	public int remaining() {
		return (absx - x) * (absy - y) * (absz - z);
	}
	
	public boolean checkDimension(int maxDimension) {
		return !(maxDimension > 0 && (absx > maxDimension || absy > maxDimension || absz > maxDimension));
	}
	
	public boolean checkVolume(int maxVolume) {
		return !(maxVolume > 0 && absx * absy * absz > maxVolume);
	}
	
	@SuppressWarnings("deprecation")
	public int process(int maxBlocks) {
		int processedBlocks = 0;
		
		while (processedBlocks <= maxBlocks && ix < absx) {
			Block block = world.getBlockAt(x + ix * dx, y + iy * dy, z + iz * dz);
			brush.update(mage, block.getLocation());
			if (!block.getChunk().isLoaded()) {
				block.getChunk().load();
				return processedBlocks;
			}
			if (!brush.isReady()) {
				brush.prepare();
				return processedBlocks;
			}
			processedBlocks++;

			if (mage.hasBuildPermission(block) && !mage.isIndestructible(block)) {
				Material previousMaterial = block.getType();
				byte previousData = block.getData();
				
				if (brush.isDifferent(block)) {
					updateBlock(world.getName(), x, y, z);
					filledBlocks.add(block);
					brush.modify(block);
					
					if (spawnFallingBlocks) {
						FallingBlock falling = block.getWorld().spawnFallingBlock(block.getLocation(), previousMaterial, previousData);
						falling.setDropItem(false);
						if (fallingBlockVelocity != null) {
							falling.setVelocity(fallingBlockVelocity);
						}
					}
				}
			}
			
			iy++;
			if (iy >= absy) {
				iy = 0;
				iz++;
				if (iz >= absz) {
					iz = 0;
					ix++;
				}
			}
		}
		
		if (ix >= absx) 
		{
			finish();
			
		}
		
		return processedBlocks;
	}
	
	@Override
	public void finish() {
		if (!finished) {
			super.finish();
			
			spell.registerForUndo(filledBlocks);
			String message = spell.getMessage("cast_finish");
			message = message.replace("$count", Integer.toString(filledBlocks.size()));
			spell.castMessage(message);
		}
	}
	
	public int getXSize() {
		return absx;
	}
	
	public int getYSize() {
		return absy;
	}
	
	public int getZSize() {
		return absz;
	}
	
	public void setTimeToLive(int timeToLive) {
		this.filledBlocks.setTimeToLive(timeToLive);
	}
}
