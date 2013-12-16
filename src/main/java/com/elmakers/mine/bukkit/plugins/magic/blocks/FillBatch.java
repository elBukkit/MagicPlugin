package com.elmakers.mine.bukkit.plugins.magic.blocks;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.dao.BlockList;
import com.elmakers.mine.bukkit.plugins.magic.PlayerSpells;
import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.Spells;

public class FillBatch implements BlockBatch {
	private final BlockList filledBlocks = new BlockList();
	private final Material material;
	private final byte data;
	private final PlayerSpells playerSpells;
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
	private boolean finished = false;
	
	public FillBatch(Spell spell, Location p1, Location p2, Material material, byte data) {
		this.material = material;
		this.data = data;
		this.playerSpells = spell.getPlayerSpells();
		this.playerName = this.playerSpells.getPlayer().getName();
		
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
		Spells spells = playerSpells.getMaster();
		Player player = playerSpells.getPlayer();
		World world = player.getWorld();
		
		while (processedBlocks <= maxBlocks && ix < absx) {
			Block block = world.getBlockAt(x + ix * dx, y + iy * dy, z + iz * dz);
			
			if (!block.getChunk().isLoaded()) {
				block.getChunk().load();
				return processedBlocks;
			}
			processedBlocks++;

			if (!playerSpells.hasBuildPermission(block)) continue;
			
			filledBlocks.add(block);
			block.setType(material);
			block.setData(data);
			
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
		
		if (!finished && ix >= absx) 
		{
			finished = true;
			spells.addToUndoQueue(playerName, filledBlocks);
		}
		
		return processedBlocks;
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
	
	public boolean isFinished() {
		return finished;
	}
}
