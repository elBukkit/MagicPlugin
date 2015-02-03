package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.api.action.BlockAction;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.CompoundAction;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;

public class CoverAction extends CompoundAction implements BlockAction
{
	private static final int DEFAULT_RADIUS	= 2;

	@Override
	public SpellResult perform(ConfigurationSection parameters, Block block) {
		if (!hasBuildPermission(block))
		{
			return SpellResult.INSUFFICIENT_PERMISSION;
		}

		Mage mage = getMage();
		int radius = parameters.getInt("radius", DEFAULT_RADIUS);
		radius = (int)(mage.getRadiusMultiplier() * radius);
		block = findSpaceAbove(block);

		if (radius < 1)
		{
			return perform(parameters, findBlockUnder(block).getLocation());
		}

		SpellResult result = SpellResult.NO_ACTION;
		int y = block.getY();
		for (int dx = -radius; dx < radius; ++dx)
		{
			for (int dz = -radius; dz < radius; ++dz)
			{
				if (isInCircle(dx, dz, radius))
				{
					int x = block.getX() + dx;
					int z = block.getZ() + dz;
					Block targetBlock = getWorld().getBlockAt(x, y, z);
					targetBlock = findBlockUnder(targetBlock);
					Block coveringBlock = targetBlock.getRelative(BlockFace.UP);
					if (!isTransparent(targetBlock.getType()) && isTransparent(coveringBlock.getType()))
					{
						actions.perform(parameters, targetBlock.getLocation());
					}
				}
			}
		}

		return result;
	}

	protected boolean isInCircle(int x, int z, int R)
	{
		return ((x * x) +  (z * z) - (R * R)) <= 0;
	}
}
