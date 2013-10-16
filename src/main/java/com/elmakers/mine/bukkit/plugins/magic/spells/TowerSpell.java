package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Material;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.dao.BlockList;
import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class TowerSpell extends Spell {

	@Override
	public boolean onCast(ConfigurationNode parameters) 
	{
		Block target = getTargetBlock();
		if (target == null) 
		{
			castMessage(player, "No target");
			return false;
		}
		int MAX_HEIGHT = 255;
		int height = 16;
		int maxHeight = 255;
		int material = 20;
		int midX = target.getX();
		int midY = target.getY();
		int midZ = target.getZ();
		
		// Check for roof
		for (int i = height; i < maxHeight; i++)
		{
			int y = midY + i;
			if (y > MAX_HEIGHT)
			{
				maxHeight = MAX_HEIGHT - midY;
				height = height > maxHeight ? maxHeight : height;
				break;
			}
			Block block = getBlockAt(midX, y, midZ);
			if (block.getType() != Material.AIR)
			{
				castMessage(player, "Found ceiling of " + block.getType().name().toLowerCase());
				height = i;
				break;
			}
		}
		
		int blocksCreated = 0;
		BlockList towerBlocks = new BlockList();
		for (int i = 0; i < height; i++)
		{
			midY++;
			for (int dx = -1; dx <= 1; dx++)
			{
				for (int dz = -1; dz <= 1; dz++)
				{
					int x = midX + dx;
					int y = midY;
					int z = midZ + dz;
					// Leave the middle empty
					if (dx != 0 || dz != 0)
					{
						blocksCreated++;
						Block block = getBlockAt(x, y, z);
						towerBlocks.add(block);
						block.setTypeId(material);
					}					
				}
			}
		}
		spells.addToUndoQueue(player, towerBlocks);
		castMessage(player, "Made tower " + height + " high with " + blocksCreated + " blocks");
		return true;
	}
}
