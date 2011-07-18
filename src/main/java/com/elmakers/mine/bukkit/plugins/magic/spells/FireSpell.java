package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.elmakers.mine.bukkit.dao.BlockList;
import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.utilities.SimpleBlockAction;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

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
                block = block.getRelative(BlockFace.UP);
            }
            
            super.perform(block);
            block.setType(material);
            
            return true;
        }
    }
	
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
				block = block.getRelative(BlockFace.DOWN);
			}	
		}
		else
		{
			while (depth < verticalSearchDistance && block.getType() != Material.AIR)
			{
				depth++;
				block = block.getRelative(BlockFace.UP);
			}
			block = block.getRelative(BlockFace.DOWN);
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
			block = block.getRelative(BlockFace.UP);
		}
		
		burnedBlocks.add(block);
		block.setType(material);
	}

	public int checkPosition(int x, int z, int R)
	{
		return (x * x) +  (z * z) - (R * R);
	}
	
	@Override
	public void onLoad(ConfigurationNode properties)  
	{
		defaultRadius = properties.getInteger("radius", defaultRadius);
		verticalSearchDistance = properties.getInteger("vertical_search_distance", verticalSearchDistance);
	}
	
	private int				defaultRadius			= 4;
	private int				verticalSearchDistance	= 8;
}
