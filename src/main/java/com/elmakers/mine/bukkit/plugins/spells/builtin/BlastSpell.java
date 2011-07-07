package com.elmakers.mine.bukkit.plugins.spells.builtin;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.persistence.dao.BlockList;
import com.elmakers.mine.bukkit.plugins.spells.Spell;
import com.elmakers.mine.bukkit.plugins.spells.utilities.PluginProperties;

public class BlastSpell extends Spell
{
	static final String		DEFAULT_DESTRUCTIBLES	= "1,2,3,4,10,11,12,13,87,88";

	private List<Material>	destructibleMaterials	= new ArrayList<Material>();
	private int				defaultRadius			= 4;
	private int				maxRadius				= 32;
	private int				defaultSearchDistance	= 32;
	private int				torchFrequency			= 4;

	public BlastSpell()
	{
	    addVariant("superblast", Material.SLIME_BALL, getCategory(), "Mine out a very large area", "16");
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
			castMessage(player, "Can't blast that far away");
			return false;
		}

		int radius = defaultRadius;
		if (parameters.length > 0)
		{
			try
			{
				radius = Integer.parseInt(parameters[0]);
				if (radius > maxRadius && maxRadius > 0)
				{
					radius = maxRadius;
				}
			} 
			catch(NumberFormatException ex)
			{
				radius = defaultRadius;
			}
		}
		
		BlockList blastedBlocks = new BlockList();
		int diameter = radius * 2;
		int midX = (diameter - 1) / 2;
		int midY = (diameter - 1) / 2;
		int midZ = (diameter - 1) / 2;
		int diameterOffset = diameter - 1;

		for (int x = 0; x < radius; ++x)
		{
			for (int y = 0; y < radius; ++y)
			{
				for (int z = 0; z < radius; ++z)
				{
					if (checkPosition(x - midX, y - midY, z - midZ, radius) <= 0)
					{
						blastBlock(x, y, z, target, radius, blastedBlocks);
						blastBlock(diameterOffset - x, y, z, target, radius, blastedBlocks);
						blastBlock(x, diameterOffset - y, z, target, radius, blastedBlocks);
						blastBlock(x, y, diameterOffset - z, target, radius, blastedBlocks);
						blastBlock(diameterOffset - x, diameterOffset - y, z, target, radius, blastedBlocks);
						blastBlock(x, diameterOffset - y, diameterOffset - z, target, radius, blastedBlocks);
						blastBlock(diameterOffset - x, y, diameterOffset - z, target, radius, blastedBlocks);
						blastBlock(diameterOffset - x, diameterOffset - y, diameterOffset - z, target, radius, blastedBlocks);
					}
				}
			}
		}

		spells.addToUndoQueue(player, blastedBlocks);
		castMessage(player, "Blasted " + blastedBlocks.size() + "blocks");

		return true;
	}

	public int checkPosition(int x, int y, int z, int R)
	{
		return (x * x) + (y * y) + (z * z) - (R * R);
	}

	public void blastBlock(int dx, int dy, int dz, Block centerPoint, int radius, BlockList blastedBlocks)
	{
		int x = centerPoint.getX() + dx - radius;
		int y = centerPoint.getY() + dy - radius;
		int z = centerPoint.getZ() + dz - radius;
		Block block = player.getWorld().getBlockAt(x, y, z);
		if (!isDestructible(block))
		{
			return;
		}
		blastedBlocks.add(block);
		block.setType(Material.AIR);
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
		return "blast";
	}

	@Override
	public String getCategory()
	{
		return "mining";
	}

	@Override
	public String getDescription()
	{
		return "Mine out a large area at your target";
	}

	@Override
	public void onLoad(PluginProperties properties)
	{
		destructibleMaterials =  properties.getMaterials("spells-blast-destroy", DEFAULT_DESTRUCTIBLES);
		defaultRadius = properties.getInteger("spells-blast-radius", defaultRadius);
		maxRadius = properties.getInteger("spells-blast-max-radius", maxRadius);
		defaultSearchDistance = properties.getInteger("spells-blast-search-distance", defaultSearchDistance);
		torchFrequency = properties.getInteger("spells-blast-torch-frequency", torchFrequency);
	}

	@Override
	public Material getMaterial()
	{
			return Material.SULPHUR;
	}
}
