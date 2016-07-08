package com.elmakers.mine.bukkit.spell.builtin;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BlockSpell;

public class TowerSpell extends BlockSpell {

	private int blocksCreated;
	
	@SuppressWarnings("deprecation")
	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		blocksCreated = 0;
		Block target = getTargetBlock();
		if (target == null) 
		{
			return SpellResult.NO_TARGET;
		}
		if (!hasBuildPermission(target)) {
			return SpellResult.INSUFFICIENT_PERMISSION;
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
			Block block = target.getWorld().getBlockAt(midX, y, midZ);
			if (block.getType() != Material.AIR)
			{
				height = i;
				break;
			}
		}

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
						Block block = target.getWorld().getBlockAt(x, y, z);
						if (isDestructible(block) && hasBuildPermission(block)) {
							blocksCreated++;
							registerForUndo(block);
							block.setTypeId(material);
						}
					}					
				}
			}
		}
		registerForUndo();
		return SpellResult.CAST;
	}
	
	@Override
	public String getMessage(String messageKey, String def) {
		String message = super.getMessage(messageKey, def);
		return message.replace("$count", Integer.toString(blocksCreated));
	}
}
