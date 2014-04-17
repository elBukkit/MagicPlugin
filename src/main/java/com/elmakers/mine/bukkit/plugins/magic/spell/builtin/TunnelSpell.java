package com.elmakers.mine.bukkit.plugins.magic.spell.builtin;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.block.BlockList;
import com.elmakers.mine.bukkit.plugins.magic.spell.BlockSpell;
import com.elmakers.mine.bukkit.plugins.magic.spell.SpellResult;

public class TunnelSpell extends BlockSpell
{
	private int defaultDepth = 8;
	private int defaultWidth = 3;
	private int defaultHeight = 3;
	private int defaultSearchDistance = 32;
	private int torchFrequency = 4;

	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		defaultDepth = parameters.getInt("depth", defaultDepth);
		defaultWidth = parameters.getInt("width", defaultWidth);
		defaultHeight = parameters.getInt("height", defaultHeight);
		defaultSearchDistance = parameters.getInt("search_distance", defaultSearchDistance);
		torchFrequency = parameters.getInt("torch_frequency", torchFrequency);

		Block playerBlock = getPlayerBlock();
		if (playerBlock == null) 
		{
			return SpellResult.NO_TARGET;
		}
		if (!hasBuildPermission(playerBlock)) {
			return SpellResult.INSUFFICIENT_PERMISSION;
		}

		BlockFace direction = getPlayerFacing();
		Block searchBlock = playerBlock.getRelative(BlockFace.UP).getRelative(BlockFace.UP);

		int searchDistance = 0;
		while (searchBlock.getType() == Material.AIR && searchDistance < defaultSearchDistance)
		{
			searchBlock = searchBlock.getRelative(direction);
			searchDistance++;
		}

		int depth = defaultDepth;
		int height = defaultHeight;
		int width = defaultWidth;

		BlockList tunneledBlocks = new BlockList();

		BlockFace toTheLeft = goLeft(direction);
		BlockFace toTheRight = goRight(direction);
		Block bottomBlock = searchBlock.getRelative(BlockFace.DOWN);
		Block bottomLeftBlock = bottomBlock;
		for (int i = 0; i < width / 2; i ++)
		{
			bottomLeftBlock = bottomLeftBlock.getRelative(toTheLeft);
		}

		Block targetBlock = bottomLeftBlock;

		for (int d = 0; d < depth; d++)
		{
			bottomBlock = bottomLeftBlock;
			for (int w = 0; w < width; w++)
			{
				targetBlock = bottomBlock;
				for (int h = 0; h < height; h++)
				{
					if (isDestructible(targetBlock) && hasBuildPermission(targetBlock))
					{
						// Put torches on the left and right wall 
						/*
						boolean useTorch = 
						(
								torchFrequency > 0 
						&& 		(w == 0 || w == width - 1) 
						&& 		(h == 1)
						&& 		(d % torchFrequency == 0)
						);
						 */
						boolean useTorch = false; // TODO!
						tunneledBlocks.add(targetBlock);
						if (useTorch)
						{
							// First check to see if the torch will stick to the wall
							// TODO: Check for glass, other non-sticky types.
							Block checkBlock = null;
							if (w == 0)
							{
								checkBlock = targetBlock.getRelative(toTheLeft);
							}
							else
							{
								checkBlock = targetBlock.getRelative(toTheRight);
							}
							if (checkBlock.getType() == Material.AIR)
							{
								targetBlock.setType(Material.AIR);
							}
							else
							{
								targetBlock.setType(Material.TORCH);
							}
						}
						else
						{
							targetBlock.setType(Material.AIR);
						}
					}
					targetBlock = targetBlock.getRelative(BlockFace.UP);
				}
				bottomBlock = bottomBlock.getRelative(toTheRight);
			}
			bottomLeftBlock = bottomLeftBlock.getRelative(direction);
		}

		registerForUndo(tunneledBlocks);

		return SpellResult.CAST;
	}
}
