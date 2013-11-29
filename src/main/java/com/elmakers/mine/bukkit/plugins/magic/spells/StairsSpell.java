package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.util.Set;
import java.util.TreeSet;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.elmakers.mine.bukkit.dao.BlockList;
import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class StairsSpell extends Spell
{
	static final String DEFAULT_DESTRUCTIBLES = "1,3,10,11,12,13";

	private Set<Material> destructibleMaterials = new TreeSet<Material>();
	private int defaultDepth = 4;
	private int defaultWidth = 3;
	private int defaultHeight = 3;
	private int torchFrequency = 4;

	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		Block targetBlock = getTargetBlock();
		if (targetBlock == null) 
		{
			castMessage("No target");
			return SpellResult.NO_TARGET;
		}

		if (!hasBuildPermission(targetBlock)) {
			castMessage("You don't have permission to build here.");
			return SpellResult.INSUFFICIENT_PERMISSION;
		}
		createStairs(targetBlock);

		return SpellResult.SUCCESS;
	}

	protected void createStairs(Block targetBlock)
	{
		BlockFace vertDirection = BlockFace.UP;
		BlockFace horzDirection = getPlayerFacing();

		int depth = defaultDepth;
		int height = defaultHeight;
		int width = defaultWidth;

		BlockList tunneledBlocks = new BlockList();
		BlockList stairBlocks = new BlockList();
		Material fillMaterial = targetBlock.getType();

		BlockFace toTheLeft = goLeft(horzDirection);
		BlockFace toTheRight = goRight(horzDirection);
		Block bottomBlock = targetBlock;
		Block bottomLeftBlock = bottomBlock;
		for (int i = 0; i < width / 2; i ++)
		{
			bottomLeftBlock = bottomLeftBlock.getRelative(toTheLeft);
		}

		targetBlock = bottomLeftBlock;
		Material stairsMaterial = Material.COBBLESTONE_STAIRS;

		for (int d = 0; d < depth; d++)
		{
			bottomBlock = bottomLeftBlock;
			for (int w = 0; w < width; w++)
			{
				targetBlock = bottomBlock;
				for (int h = 0; h < height; h++)
				{
					if (isDestructible(targetBlock))
					{
						// Check to see if the torch will stick to the wall
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
						// Put torches on the left and right wall 
						boolean useTorch = 
								(
										torchFrequency > 0 
										&& 		(w == 0 || w == width - 1) 
										&& 		(h == 1)
										&& 		(d % torchFrequency == 0)
										&&		checkBlock.getType() != Material.AIR
										);
						boolean useStairs = (h == 0);
						if (useStairs)
						{
							stairBlocks.add(targetBlock);
							targetBlock.setType(stairsMaterial);
						}
						else
							if (useTorch)
							{
								tunneledBlocks.add(targetBlock);
								targetBlock.setType(Material.TORCH);
							}
							else
							{
								tunneledBlocks.add(targetBlock);
								targetBlock.setType(Material.AIR);
							}
						Block standingBlock = targetBlock.getRelative(BlockFace.DOWN);
						if (standingBlock.getType() == Material.AIR)
						{
							stairBlocks.add(standingBlock);
							standingBlock.setType(fillMaterial);
						}
					}
					targetBlock = targetBlock.getRelative(BlockFace.UP);
				}
				bottomBlock = bottomBlock.getRelative(toTheRight);
			}
			bottomLeftBlock = bottomLeftBlock.getRelative(horzDirection);
			bottomLeftBlock = bottomLeftBlock.getRelative(vertDirection);
		}

		spells.addToUndoQueue(player, tunneledBlocks);
		spells.addToUndoQueue(player, stairBlocks);
		castMessage("Tunneled through " + tunneledBlocks.size() + "blocks and created " + stairBlocks.size() + " stairs");
	}	

	protected void createSpiralStairs(Block targetBlock)
	{
		// TODO
	}

	public boolean isDestructible(Block block)
	{
		if (block.getType() == Material.AIR)
			return false;

		return destructibleMaterials.contains(block.getType());
	}

	@Override
	public void onLoad(ConfigurationNode properties)  
	{
		destructibleMaterials = properties.getMaterials("destructible", DEFAULT_DESTRUCTIBLES);
		defaultDepth = properties.getInteger("depth", defaultDepth);
		defaultWidth = properties.getInteger("width", defaultWidth);
		defaultHeight = properties.getInteger("height", defaultHeight);
		torchFrequency = properties.getInteger("torch_frequency", torchFrequency);
	}
}
