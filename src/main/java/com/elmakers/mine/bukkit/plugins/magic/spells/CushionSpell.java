package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.dao.BlockList;
import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class CushionSpell extends Spell
{
	private int cushionWidth = 3;
	private int cushionHeight = 4;

	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		World world = player.getWorld();
		Block targetFace = getTargetBlock();
		if (targetFace == null)
		{
			castMessage("No target");
			return SpellResult.NO_TARGET;
		}
		if (!hasBuildPermission(targetFace)) {
			castMessage("You don't have permission to build here.");
			return SpellResult.INSUFFICIENT_PERMISSION;
		}

		castMessage("Happy landings");

		BlockList cushionBlocks = new BlockList();
		cushionBlocks.setTimeToLive(7000);
		spells.disablePhysics(8000);

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

		spells.scheduleCleanup(cushionBlocks);
		return SpellResult.SUCCESS;
	}

	@Override
	public void onLoad(ConfigurationNode properties)  
	{
		cushionWidth = properties.getInteger("width", cushionWidth);
		cushionHeight = properties.getInteger("height", cushionHeight);
	}
}
