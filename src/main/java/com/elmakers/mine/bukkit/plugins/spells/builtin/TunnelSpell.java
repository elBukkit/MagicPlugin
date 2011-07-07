package com.elmakers.mine.bukkit.plugins.spells.builtin;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.elmakers.mine.bukkit.persistence.dao.BlockList;
import com.elmakers.mine.bukkit.plugins.spells.Spell;
import com.elmakers.mine.bukkit.plugins.spells.utilities.PluginProperties;

public class TunnelSpell extends Spell
{
	static final String DEFAULT_DESTRUCTIBLES = "1,3,10,11,12,13,87,88";
	
	private List<Material> destructibleMaterials = new ArrayList<Material>();
	private int defaultDepth = 8;
	private int defaultWidth = 3;
	private int defaultHeight = 3;
	private int defaultSearchDistance = 32;
	private int torchFrequency = 4;
	
	@Override
	public boolean onCast(String[] parameters)
	{
		Block playerBlock = getPlayerBlock();
		if (playerBlock == null) 
		{
			// no spot found to tunnel
			player.sendMessage("You need to be standing on something");
			return false;
		}
		
		BlockFace direction = getPlayerFacing();
		Block searchBlock = playerBlock.getFace(BlockFace.UP).getFace(BlockFace.UP);
		
		int searchDistance = 0;
		while (searchBlock.getType() == Material.AIR && searchDistance < defaultSearchDistance)
		{
			searchBlock = searchBlock.getFace(direction);
			searchDistance++;
		}
		
		int depth = defaultDepth;
		int height = defaultHeight;
		int width = defaultWidth;
		
		BlockList tunneledBlocks = new BlockList();
		
		BlockFace toTheLeft = goLeft(direction);
		BlockFace toTheRight = goRight(direction);
		Block bottomBlock = searchBlock.getFace(BlockFace.DOWN);
		Block bottomLeftBlock = bottomBlock;
		for (int i = 0; i < width / 2; i ++)
		{
			bottomLeftBlock = bottomLeftBlock.getFace(toTheLeft);
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
					if (isDestructible(targetBlock))
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
								checkBlock = targetBlock.getFace(toTheLeft);
							}
							else
							{
								checkBlock = targetBlock.getFace(toTheRight);
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
					targetBlock = targetBlock.getFace(BlockFace.UP);
				}
				bottomBlock = bottomBlock.getFace(toTheRight);
			}
			bottomLeftBlock = bottomLeftBlock.getFace(direction);
		}

		spells.addToUndoQueue(player, tunneledBlocks);
		castMessage(player, "Tunneled through " + tunneledBlocks.size() + "blocks");
		
		return true;
	}
	
	public boolean isDestructible(Block block)
	{
		if (block.getType() == Material.AIR)
			return false;

		return destructibleMaterials.contains(block.getType());
	}

	@Override
	public void onLoad(PluginProperties properties)
	{
		destructibleMaterials = properties.getMaterials("spells-tunnel-destructible", DEFAULT_DESTRUCTIBLES);
		defaultDepth = properties.getInteger("spells-tunnel-depth", defaultDepth);
		defaultWidth = properties.getInteger("spells-tunnel-width", defaultWidth);
		defaultHeight = properties.getInteger("spells-tunnel-height", defaultHeight);
		defaultSearchDistance = properties.getInteger("spells-tunnel-search-distance", defaultSearchDistance);
		torchFrequency = properties.getInteger("spells-tunnel-torch-frequency", torchFrequency);
	}
	
	@Override
	public String getName()
	{
		return "tunnel";
	}

	@Override
	public String getCategory()
	{
		return "mining";
	}

	@Override
	public String getDescription()
	{
		return "Create a tunnel for mining";
	}

	@Override
	public Material getMaterial()
	{
		return Material.STONE_PICKAXE;
	}

}
