package com.elmakers.mine.bukkit.plugins.magic.blocks;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.dao.BlockList;
import com.elmakers.mine.bukkit.plugins.magic.Mage;
import com.elmakers.mine.bukkit.plugins.magic.Spell;

public class FillBatch extends VolumeBatch {
	private final BlockList filledBlocks = new BlockList();
	private final Material material;
	private final byte data;
	private final World world;
	private final Spell spell;
	private final Mage playerSpells;
	private String playerName;

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
	
	public FillBatch(Spell spell, Location p1, Location p2, Material material, byte data) {
		super(spell.getPlayerSpells().getController(), p1.getWorld().getName());
		this.material = material;
		this.data = data;
		this.spell = spell;
		this.playerSpells = spell.getPlayerSpells();
		this.playerName = this.playerSpells.getPlayer().getName();
		this.world = this.playerSpells.getPlayer().getWorld();
		
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
			
			if (!block.getChunk().isLoaded()) {
				block.getChunk().load();
				return processedBlocks;
			}
			processedBlocks++;

			if (playerSpells.hasBuildPermission(block) && !playerSpells.isIndestructible(block)) {
				spells.updateBlock(world.getName(), x, y, z);
				filledBlocks.add(block);
				block.setType(material);
				block.setData(data);
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
	protected void finish() {
		super.finish();
		
		spells.addToUndoQueue(playerName, filledBlocks);
		spell.castMessage("Filled " + getXSize() + "x" +  getYSize() + "x" +  getZSize() + " area with " + material.name().toLowerCase());
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
}
