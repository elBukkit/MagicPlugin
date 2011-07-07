package com.elmakers.mine.bukkit.plugins.spells.builtin;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.elmakers.mine.bukkit.persistence.dao.BlockList;
import com.elmakers.mine.bukkit.plugins.spells.Spell;
import com.elmakers.mine.bukkit.plugins.spells.utilities.PluginProperties;
import com.elmakers.mine.bukkit.utilities.SimpleBlockAction;

public class FireSpell extends Spell
{
    public class FireAction extends SimpleBlockAction
    {
        public boolean perform(Block block)
        {
            if (block.getType() == Material.AIR || block.getType() == Material.FIRE)
            {
                return false;
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
            
            super.perform(block);
            block.setType(material);
            
            return true;
        }
    }
    
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

        FireAction action = new FireAction();

        if (radius <= 1)
		{
            action.perform(target);
		}
		else
		{
		    this.coverSurface(target.getLocation(), radius, action);
		}

		spells.addToUndoQueue(player, action.getBlocks());
		castMessage(player, "Burned " + action.getBlocks().size() + " blocks");
		
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
	public String getName()
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
