package com.elmakers.mine.bukkit.spells;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.BlockVector;

import com.elmakers.mine.bukkit.magic.Spell;
import com.elmakers.mine.bukkit.persistence.dao.BlockList;

public class PortalSpell extends Spell
{
	private int				defaultSearchDistance	= 32;
	
	public PortalSpell(NetherManager nether)
	{
		this.nether = nether;
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
			castMessage(player, "Can't create a portal that far away");
			return false;
		}
		
		Material blockType = target.getType();
		Block portalBase = target.getFace(BlockFace.UP);
		blockType = portalBase.getType();
		if (blockType != Material.AIR)
		{
			portalBase = getFaceBlock();
		}
		
		blockType = portalBase.getType();
		if (blockType != Material.AIR)
		{
			castMessage(player, "Can't create a portal there");
			return false;
		
		}
		
		BlockList portalBlocks = new BlockList();
		portalBlocks.setTimeToLive(10000);

		/*
		for (int z = 0; z < 2; z++)
		{
			for (int y = 0; y < 4; y++)
			{
				setBlock(portalBlocks, portalBase, 0, y, z, Material.PORTAL);
			}
		}
		*/
		
		
		// Temp...
		BoundingBox container = new BoundingBox(portalBase.getX() - 3, portalBase.getY() + 1, portalBase.getZ() - 3,
				portalBase.getX() + 2, portalBase.getY() + 5, portalBase.getZ() + 2);
		
		BlockVector min = container.getMin();
		BlockVector max = container.getMax();
		World world = portalBase.getWorld();
		
		for (int x = min.getBlockX(); x < max.getBlockX(); x++)
		{
			for (int y = min.getBlockY(); y < max.getBlockY(); y++)
			{
				for (int z = min.getBlockZ(); z < max.getBlockZ(); z++)
				{
					Block block = world.getBlockAt(x, y, z);
					portalBlocks.add(block);
				}
			}
		}
	
		nether.createTemporaryPortal(player, portalBase);
		
		return true;
	}
		
	protected void setBlock(BlockList blocks, Block baseBlock, int x, int y, int z, Material material)
	{
		Block block = baseBlock.getRelative(x, y, z);
		if (block.getType() == Material.AIR)
		{
			blocks.add(block);
			block.setType(material);
		}		
	}

	@Override
	protected String getName()
	{
		return "portal";
	}

	@Override
	public String getCategory()
	{
		return "nether";
	}

	@Override
	public String getDescription()
	{
		return "Create a temporary portal";
	}

	@Override
	public Material getMaterial()
	{
		return Material.PORTAL;
	}
	
	protected NetherManager nether;

}
