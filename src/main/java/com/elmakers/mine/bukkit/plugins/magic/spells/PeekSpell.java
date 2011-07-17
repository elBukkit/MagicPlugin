package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.dao.BlockList;
import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class PeekSpell extends Spell
{
	static final String		DEFAULT_PEEKABLES		= "1,2,3,10,11,12,13";

	private List<Material>	peekableMaterials		= new ArrayList<Material>();
	private int				defaultRadius			= 3;
	private int				maxRadius				= 32;
	private int				defaultSearchDistance	= 32;
	
	private Material        peekMaterial            = Material.GLASS;

	@Override
	public boolean onCast(ConfigurationNode parameters) 
	{
		targetThrough(Material.GLASS);
		Block target = getTargetBlock();
		if (target == null)
		{
			castMessage(player, "No target");
			return false;
		}
		if (defaultSearchDistance > 0 && getDistance(player, target) > defaultSearchDistance)
		{
			castMessage(player, "Can't peek that far away");
			return false;
		}

		int radius = parameters.getInt("radius", defaultRadius);
        if (radius > maxRadius && maxRadius > 0)
        {
            radius = maxRadius;
        }
		
		BlockList peekedBlocks = new BlockList();
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
						blastBlock(x, y, z, target, radius, peekedBlocks);
						blastBlock(diameterOffset - x, y, z, target, radius, peekedBlocks);
						blastBlock(x, diameterOffset - y, z, target, radius, peekedBlocks);
						blastBlock(x, y, diameterOffset - z, target, radius, peekedBlocks);
						blastBlock(diameterOffset - x, diameterOffset - y, z, target, radius, peekedBlocks);
						blastBlock(x, diameterOffset - y, diameterOffset - z, target, radius, peekedBlocks);
						blastBlock(diameterOffset - x, y, diameterOffset - z, target, radius, peekedBlocks);
						blastBlock(diameterOffset - x, diameterOffset - y, diameterOffset - z, target, radius, peekedBlocks);
					}
				}
			}
		}
		
		peekedBlocks.setTimeToLive(8000);
		spells.scheduleCleanup(peekedBlocks);

		castMessage(player, "Peeked through  " + peekedBlocks.size() + "blocks");

		return true;
	}

	public void setPeekMaterial(Material mat)
	{
	    this.peekMaterial = mat;
	}

	public int checkPosition(int x, int y, int z, int R)
	{
		return (x * x) + (y * y) + (z * z) - (R * R);
	}

	public void blastBlock(int dx, int dy, int dz, Block centerPoint, int radius, BlockList blocks)
	{
		int x = centerPoint.getX() + dx - radius;
		int y = centerPoint.getY() + dy - radius;
		int z = centerPoint.getZ() + dz - radius;
		Block block = player.getWorld().getBlockAt(x, y, z);
		if (!isPeekable(block))
		{
			return;
		}
		blocks.add(block);
		block.setType(peekMaterial);
	}

	public boolean isPeekable(Block block)
	{
		if (block.getType() == Material.AIR)
			return false;
		
		if (block.getType() == peekMaterial)
			return false;
		
		return peekableMaterials.contains(block.getType());
	}
	
	@Override
	public void onLoad(ConfigurationNode properties)  
	{
		peekableMaterials = properties.getMaterials("spells-peek-peekable", DEFAULT_PEEKABLES);
		defaultRadius = properties.getInteger("spells-peek-radius", defaultRadius);
		maxRadius = properties.getInteger("spells-peek-max-radius", maxRadius);
		defaultSearchDistance = properties.getInteger("spells-peek-search-distance", defaultSearchDistance);
	}
}
