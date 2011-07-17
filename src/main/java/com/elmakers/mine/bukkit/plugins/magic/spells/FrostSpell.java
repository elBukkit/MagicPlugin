package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.dao.BlockList;
import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.Target;
import com.elmakers.mine.bukkit.utilities.SimpleBlockAction;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class FrostSpell extends Spell
{
	private int				defaultRadius			= 2;
	private int				verticalSearchDistance	= 8;
	private int             timeToLive = 60000;
	
	public class FrostAction extends SimpleBlockAction
    {
        public boolean perform(Block block)
        {
            if (block.getType() == Material.AIR || block.getType() == Material.SNOW)
            {
                return false;
            }
            Material material = Material.SNOW;
            if (block.getType() == Material.WATER || block.getType() == Material.STATIONARY_WATER)
            {
                material = Material.ICE;
            }
            else if (block.getType() == Material.LAVA)
            {
                material = Material.COBBLESTONE;
            }
            else if (block.getType() == Material.STATIONARY_LAVA)
            {
                material = Material.OBSIDIAN;
            }
            else if (block.getType() == Material.FIRE)
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
	 
	@Override
	public boolean onCast(ConfigurationNode parameters) 
	{
	    setMaxRange(16, false);
	    Target target = getTarget();

        if (target == null)
        {
            castMessage(player, "No target");
            return false;
        }
        if (target.isEntity())
        {
            Entity targetEntity = target.getEntity();
            if (targetEntity instanceof LivingEntity)
            {
                LivingEntity li = (LivingEntity)targetEntity;
                if (li instanceof Player)
                {
                    li.damage(1);
                }
                else
                {
                    li.damage(10);
                }
            }
        }

        if (!target.hasTarget())
        {
            castMessage(player, "No target");
            return false;
        }
		
        int radius = parameters.getInt("radius", defaultRadius);
        FrostAction action = new FrostAction();

        if (radius <= 1)
        {
            action.perform(target.getBlock());
        }
        else
        {
            this.coverSurface(target.getLocation(), radius, action);
        }


        BlockList frozenBlocks = action.getBlocks();
        frozenBlocks.setTimeToLive(timeToLive);
        spells.scheduleCleanup(frozenBlocks);
        castMessage(player, "Frosted " + action.getBlocks().size() + " blocks");
        
        return true;
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
		timeToLive = properties.getInt("duration", timeToLive);
	}
}
