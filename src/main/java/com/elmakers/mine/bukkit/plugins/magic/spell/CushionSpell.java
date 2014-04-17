package com.elmakers.mine.bukkit.plugins.magic.spell;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.block.BlockList;
import com.elmakers.mine.bukkit.plugins.magic.BlockSpell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;

public class CushionSpell extends BlockSpell
{
	private static final int DEFAULT_CUSHION_WIDTH = 3;
	private static final int DEFAULT_CUSHION_HEIGHT = 4;

	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		World world = getWorld();
		Block targetFace = getTargetBlock();
		if (targetFace == null)
		{
			return SpellResult.NO_TARGET;
		}
		if (!hasBuildPermission(targetFace)) {
			return SpellResult.INSUFFICIENT_PERMISSION;
		}
		
		int cushionWidth = parameters.getInt("width", DEFAULT_CUSHION_WIDTH);
		int cushionHeight = parameters.getInt("height", DEFAULT_CUSHION_HEIGHT);

		BlockList cushionBlocks = new BlockList();
		cushionBlocks.setTimeToLive(7000);
		controller.disablePhysics(8000);

		int bubbleStart = -cushionWidth  / 2;
		int bubbleEnd = cushionWidth  / 2;

		for (int dx = bubbleStart; dx < bubbleEnd; dx++)
		{
			for (int dz = bubbleStart ; dz < bubbleEnd; dz++)
			{
				for (int dy = 0; dy < cushionHeight; dy++)
				{
					int x = targetFace.getX() + dx;
					int y = targetFace.getY() + dy;
					int z = targetFace.getZ() + dz;
					Block block = world.getBlockAt(x, y, z);
					if (block.getType() == Material.AIR)
					{
						cushionBlocks.add(block);
						block.setType(Material.STATIONARY_WATER);
					}
				}
			}
		}

		registerForUndo(cushionBlocks);
		return SpellResult.CAST;
	}
}
