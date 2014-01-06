package com.elmakers.mine.bukkit.plugins.magic.blocks;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.dao.BlockList;
import com.elmakers.mine.bukkit.plugins.magic.PlayerSpells;
import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.Spells;

public class ConstructBatch implements BlockBatch {
	
	private int             timeToLive              = 0;
	private final Set<Material>	indestructible;
	private final BlockList constructedBlocks = new BlockList();
	private final Location 		center;
	private final int radius;
	private final Material material;
	private final byte data;
	private boolean checkDestructible = true;
	private final ConstructionType type;
	private final boolean fill;
	private final PlayerSpells playerSpells;
	private final Spell spell;
	private final Spells spells;
	private final String playerName;
	
	private boolean finished = false;
	private int x = 0;
	private int y = 0;
	private int z = 0;
	
	public ConstructBatch(Spell spell, Location center, ConstructionType type, int radius, boolean fill, Material material, byte data, Set<Material> indestructible) {
		this.indestructible = indestructible;
		this.center = center;
		this.radius = radius;
		this.material = material;
		this.data = data;
		this.type = type;
		this.fill = fill;
		this.playerSpells = spell.getPlayerSpells();
		this.spells = playerSpells.getMaster();
		this.spell = spell;
		this.playerName = this.playerSpells.getPlayer().getName();
	}
	
	public int process(int maxBlocks) {
		int processedBlocks = 0;
		boolean updated = false; // Only update map once for efficiency.
		
		while (processedBlocks <= maxBlocks && x <= radius) {
			if (!fillBlock(x, y, z, !updated)) {
				return processedBlocks;
			}
			updated = true;
			
			y++;
			if (y > radius) {
				y = 0;
				z++;
				if (z > radius) {
					z = 0;
					x++;
				}
			}
			processedBlocks++;
		}
		
		if (!finished && x > radius) 
		{
			finished = true;
			if (timeToLive == 0)
			{
				spells.addToUndoQueue(playerName, constructedBlocks);
			}
			else
			{
				constructedBlocks.setTimeToLive(timeToLive);
				spells.scheduleCleanup(playerName, constructedBlocks);
			}
			spell.castMessage("Constructed " + constructedBlocks.size() + " blocks");
		}
		
		return processedBlocks;
	}

	public boolean fillBlock(int x, int y, int z, boolean update)
	{
		boolean fillBlock = false;
		switch(type) {
			case SPHERE:
				int maxDistanceSquared = radius * radius;
				float mx = (float)x - 0.5f;
				float my = (float)y - 0.5f;
				float mz = (float)z - 0.5f;
				
				int distanceSquared = (int)((mx * mx) + (my * my) + (mz * mz));
				if (fill)
				{
					fillBlock = distanceSquared <= maxDistanceSquared;
				} 
				else 
				{
					mx++;
					my++;
					mz++;
					int outerDistanceSquared = (int)((mx * mx) + (my * my) + (mz * mz));
					fillBlock = maxDistanceSquared >= distanceSquared && maxDistanceSquared <= outerDistanceSquared;
				}
				//spells.getLog().info("(" + x + "," + y + "," + z + ") : " + fillBlock + " = " + distanceSquared + " : " + maxDistanceSquared);
				break;
			case PYRAMID:
				int elevation = radius - y;
				if (fill) {
					fillBlock = (x <= elevation) && (z <= elevation);
				} else {
					fillBlock = (x == elevation && z <= elevation) || (z == elevation && x <= elevation);
				}
				break;
			default: 
				fillBlock = fill ? true : (x == radius || y == radius || z == radius);
				break;
		}
		boolean success = true;
		if (fillBlock)
		{
			success = success && constructBlock(x, y, z, update);
			success = success && constructBlock(-x, y, z, false);
			success = success && constructBlock(x, -y, z, false);
			success = success && constructBlock(x, y, -z, false);
			success = success && constructBlock(-x, -y, z, false);
			success = success && constructBlock(x, -y, -z, false);
			success = success && constructBlock(-x, y, -z, false);
			success = success && constructBlock(-x, -y, -z, false);
		}
		return success;
	}

	public int getDistanceSquared(int x, int y, int z)
	{
		return x * x + y * y + z * z;
	}

	@SuppressWarnings("deprecation")
	public boolean constructBlock(int dx, int dy, int dz, boolean update)
	{
		int x = center.getBlockX() + dx;
		int y = center.getBlockY() + dy;
		int z = center.getBlockZ() + dz;
		if (y < 0 || y > 255) return true;
		
		if (update) {
			spells.updateBlock(center.getWorld().getName(), x, y, z);
		}
		
		Block block = center.getWorld().getBlockAt(x, y, z);
		if (!block.getChunk().isLoaded()) {
			block.getChunk().load();
			return false;
		}
		if (checkDestructible && !isDestructible(block))
		{
			return true;
		}
		if (!playerSpells.hasBuildPermission(block)) 
		{
			return true;
		}
		constructedBlocks.add(block);
		block.setType(material);
		block.setData(data);
		return true;
	}

	protected boolean isDestructible(Block block)
	{
		if (indestructible.isEmpty()) {
			return playerSpells.getMaster().getDestructibleMaterials().contains(block.getType());
		}
		return !indestructible.contains(block.getType());
	}
	
	public void setTimeToLive(int timeToLive) {
		this.timeToLive = timeToLive;
	}
	
	public void setCheckDestructible(boolean check) {
		this.checkDestructible = check;
	}
	
	public boolean isFinished() {
		return finished;
	}
}
