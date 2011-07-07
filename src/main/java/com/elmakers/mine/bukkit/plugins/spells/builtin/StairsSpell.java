package com.elmakers.mine.bukkit.plugins.spells.builtin;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.elmakers.mine.bukkit.persistence.dao.BlockList;
import com.elmakers.mine.bukkit.plugins.spells.Spell;
import com.elmakers.mine.bukkit.plugins.spells.utilities.PluginProperties;

public class StairsSpell extends Spell
{
	static final String DEFAULT_DESTRUCTIBLES = "1,3,10,11,12,13";
	
	private List<Material> destructibleMaterials = new ArrayList<Material>();
	private int defaultDepth = 4;
	private int defaultWidth = 3;
	private int defaultHeight = 3;
	private int torchFrequency = 4;
	
	@Override
	public boolean onCast(String[] parameters)
	{
		Block targetBlock = getTargetBlock();
		if (targetBlock == null) 
		{
			castMessage(player, "No target");
			return false;
		}
		
		createStairs(targetBlock);
		
		return true;
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
			bottomLeftBlock = bottomLeftBlock.getFace(toTheLeft);
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
							checkBlock = targetBlock.getFace(toTheLeft);
						}
						else
						{
							checkBlock = targetBlock.getFace(toTheRight);
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
						Block standingBlock = targetBlock.getFace(BlockFace.DOWN);
						if (standingBlock.getType() == Material.AIR)
						{
							stairBlocks.add(standingBlock);
							standingBlock.setType(fillMaterial);
						}
					}
					targetBlock = targetBlock.getFace(BlockFace.UP);
				}
				bottomBlock = bottomBlock.getFace(toTheRight);
			}
			bottomLeftBlock = bottomLeftBlock.getFace(horzDirection);
			bottomLeftBlock = bottomLeftBlock.getFace(vertDirection);
		}

		spells.addToUndoQueue(player, tunneledBlocks);
		spells.addToUndoQueue(player, stairBlocks);
		castMessage(player, "Tunneled through " + tunneledBlocks.size() + "blocks and created " + stairBlocks.size() + " stairs");
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
	public String getName()
	{
		return "stairs";
	}

	@Override
	public String getCategory()
	{
		return "wip";
	}

	@Override
	public String getDescription()
	{
		return "Construct some stairs";
	}
	
	@Override
	public void onLoad(PluginProperties properties)
	{
		destructibleMaterials = properties.getMaterials("spells-stairs-destructible", DEFAULT_DESTRUCTIBLES);
		defaultDepth = properties.getInteger("spells-stairs-depth", defaultDepth);
		defaultWidth = properties.getInteger("spells-stairs-width", defaultWidth);
		defaultHeight = properties.getInteger("spells-stairs-height", defaultHeight);
		torchFrequency = properties.getInteger("spells-stairs-torch-frequency", torchFrequency);
	}

	@Override
	public Material getMaterial()
	{
		return Material.COBBLESTONE_STAIRS;
	}

}
