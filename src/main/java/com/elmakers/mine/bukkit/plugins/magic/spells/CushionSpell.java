package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.blocks.BlockList;
import com.elmakers.mine.bukkit.plugins.magic.BlockSpell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class CushionSpell extends BlockSpell
{
	private static final int DEFAULT_CUSHION_WIDTH = 3;
	private static final int DEFAULT_CUSHION_HEIGHT = 4;

	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		World world = getWorld();
		Block targetFace = getTargetBlock();
		if (targetFace == null)
		{
			castMessage("No target");
			return SpellResult.NO_TARGET;
		}
		if (!hasBuildPermission(targetFace)) {
			return SpellResult.INSUFFICIENT_PERMISSION;
		}
		
		int cushionWidth = parameters.getInteger("width", DEFAULT_CUSHION_WIDTH);
		int cushionHeight = parameters.getInteger("height", DEFAULT_CUSHION_HEIGHT);

		castMessage("Happy landings");

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
