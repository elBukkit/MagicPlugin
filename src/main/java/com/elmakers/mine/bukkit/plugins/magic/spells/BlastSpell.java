package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.dao.BlockList;
import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class BlastSpell extends Spell
{
	static final String		DEFAULT_DESTRUCTIBLES	= "1,2,3,4,10,11,12,13,87,88";

	private List<Material>	destructibleMaterials	= new ArrayList<Material>();
	private int				defaultRadius			= 4;
	
	@Override
	public boolean onCast(ConfigurationNode parameters) 
	{
		Block target = getTargetBlock();
		if (target == null)
		{
			castMessage(player, "No target");
			return false;
		}

		int radius = parameters.getInt("radius", defaultRadius);

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
	public void onLoad(ConfigurationNode properties)  
	{
		destructibleMaterials =  properties.getMaterials("destructible", DEFAULT_DESTRUCTIBLES);
		defaultRadius = properties.getInteger("radius", defaultRadius);
	}
}
