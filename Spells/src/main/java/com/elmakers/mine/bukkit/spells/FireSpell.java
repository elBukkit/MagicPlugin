package com.elmakers.mine.bukkit.spells;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.elmakers.mine.bukkit.magic.Spell;
import com.elmakers.mine.bukkit.persistence.dao.BlockList;

public class FireSpell extends Spell
{
	public FireSpell()
	{
		addVariant("inferno", Material.FIRE, "combat", "Burn a wide area", "6");
	}
	
	@Override
	public boolean onCast(String[] parameters)
	{
		Block target = getTargetBlock();
		if (target == null) 
		{
			castMessage(player, "No target");
			return false;
		}
		
		if (defaultSearchDistance > 0 && getDistance(player, target) > defaultSearchDistance)
		{
			castMessage(player, "Can't fire that far away");
			return false;
		}
		
		int radius = 1;
		for (int i = 0; i < parameters.length; i++)
		{
			// try radius;
			try
			{
				radius = Integer.parseInt(parameters[0]);
			}
			catch(NumberFormatException ex)
			{
			}
		}
		
		if (radius > maxRadius && maxRadius > 0)
		{
			radius = maxRadius;
		}
		
		BlockList burnedBlocks = new BlockList();
		int diameter = radius * 2;
		int midX = (diameter - 1) / 2;
		int midY = (diameter - 1) / 2;
		int midZ = (diameter - 1) / 2;
		int diameterOffset = diameter - 1;

		if (radius <= 1)
		{
			burnBlock(0, midY, 0, target, 0, burnedBlocks);
		}
		else
		{
			for (int x = 0; x < radius; ++x)
			{
				for (int z = 0; z < radius; ++z)
				{
					if (checkPosition(x - midX, z - midZ, radius) <= 0)
					{
						int y = midY;
						burnBlock(x, y, z, target, radius, burnedBlocks);
						
						burnBlock(diameterOffset - x, y, z, target, radius, burnedBlocks);
						burnBlock(x, diameterOffset - y, z, target, radius, burnedBlocks);
						burnBlock(x, y, diameterOffset - z, target, radius, burnedBlocks);
						burnBlock(diameterOffset - x, diameterOffset - y, z, target, radius, burnedBlocks);
						burnBlock(x, diameterOffset - y, diameterOffset - z, target, radius, burnedBlocks);
						burnBlock(diameterOffset - x, y, diameterOffset - z, target, radius, burnedBlocks);
						burnBlock(diameterOffset - x, diameterOffset - y, diameterOffset - z, target, radius, burnedBlocks);
					}
					
				}
			}
		}

		spells.addToUndoQueue(player, burnedBlocks);
		castMessage(player, "Burned " + burnedBlocks.size() + " blocks");
		
		return true;
	}
	
	public void burnBlock(int dx, int dy, int dz, Block centerPoint, int radius, BlockList burnedBlocks)
	{
		int x = centerPoint.getX() + dx - radius;
		int y = centerPoint.getY() + dy - radius;
		int z = centerPoint.getZ() + dz - radius;
		Block block = player.getWorld().getBlockAt(x, y, z);
		int depth = 0;
		
		if (block.getType() == Material.AIR)
		{
			while (depth < verticalSearchDistance && block.getType() == Material.AIR)
			{
				depth++;
				block = block.getFace(BlockFace.DOWN);
			}	
		}
		else
		{
			while (depth < verticalSearchDistance && block.getType() != Material.AIR)
			{
				depth++;
				block = block.getFace(BlockFace.UP);
			}
			block = block.getFace(BlockFace.DOWN);
		}

		if (block.getType() == Material.AIR || block.getType() == Material.FIRE)
		{
			return;
		}
		Material material = Material.FIRE;
		
		if (block.getType() == Material.WATER || block.getType() == Material.STATIONARY_WATER || block.getType() == Material.ICE || block.getType() == Material.SNOW)
		{
			material = Material.AIR;
		}
		else
		{
			block = block.getFace(BlockFace.UP);
		}
		
		burnedBlocks.add(block);
		block.setType(material);
	}

	public int checkPosition(int x, int z, int R)
	{
		return (x * x) +  (z * z) - (R * R);
	}

	@Override
	protected String getName()
	{
		return "fire";
	}

	@Override
	public String getCategory()
	{
		return "combat";
	}

	@Override
	public String getDescription()
	{
		return "Light fires from a distance";
	}

	@Override
	public Material getMaterial()
	{
		return Material.FLINT_AND_STEEL;
	}
	
	@Override
	public void onLoad(PluginProperties properties)
	{
		defaultRadius = properties.getInteger("spells-fire-radius", defaultRadius);
		maxRadius = properties.getInteger("spells-fire-max-radius", maxRadius);
		defaultSearchDistance = properties.getInteger("spells-fire-search-distance", defaultSearchDistance);
		verticalSearchDistance = properties.getInteger("spells-fire-vertical-search-distance", verticalSearchDistance);
	}
	
	private int				defaultRadius			= 4;
	private int				maxRadius				= 32;
	private int				defaultSearchDistance	= 32;
	private int				verticalSearchDistance	= 8;
}
