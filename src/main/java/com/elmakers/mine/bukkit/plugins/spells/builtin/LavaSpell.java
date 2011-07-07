package com.elmakers.mine.bukkit.plugins.spells.builtin;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.persistence.dao.BlockList;
import com.elmakers.mine.bukkit.plugins.spells.Spell;
import com.elmakers.mine.bukkit.plugins.spells.utilities.PluginProperties;

public class LavaSpell extends Spell
{

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
		
		int lavaBlocks = (int)getDistance(player, target);
		if (lavaBlocks <= 0) return false;
		
		Vector targetLoc = new Vector(target.getX(), target.getY(), target.getZ());
		Vector playerLoc = new Vector(player.getLocation().getX(), player.getLocation().getY() + 1, player.getLocation().getZ());
		
		// Create aim vector - this should probably replace Spell.getAimVector, which seems broken!
		Vector aim = targetLoc;
		aim.subtract(playerLoc);
		aim.normalize();
		targetLoc = playerLoc;
		
		// Move out a bit for safety!
		targetLoc.add(aim);
		targetLoc.add(aim);
		
		BlockList burnedBlocks = new BlockList();
		for (int i = 0; i < lavaBlocks; i++)
		{
			Block currentTarget = target.getWorld().getBlockAt(targetLoc.getBlockX(), targetLoc.getBlockY(), targetLoc.getBlockZ());
			if (currentTarget.getType() == Material.AIR)
			{
				burnedBlocks.add(currentTarget);
				Material mat = i > 15 ? Material.STATIONARY_LAVA : Material.LAVA;
				byte data = i > 15 ? 15 : (byte)i;
				
				currentTarget.setType(mat);
				currentTarget.setData(data);
			}
			targetLoc.add(aim);
		}
		
		if (burnedBlocks.size() > 0)
		{
			burnedBlocks.setTimeToLive(2);
			spells.addToUndoQueue(player, burnedBlocks);
		}
		
		castMessage(player, "Blasted " + burnedBlocks.size() + " lava blocks");
		
		return true;
	}

	@Override
	public String getName()
	{
		return "lava";
	}

	@Override
	public String getCategory()
	{
		return "combat";
	}

	@Override
	public String getDescription()
	{
		return "Fire a stream of lava";
	}

	@Override
	public Material getMaterial()
	{
		return Material.LAVA;
	}

	@Override
	public void onLoad(PluginProperties properties)
	{
		defaultRadius = properties.getInteger("spells-lava-radius", defaultRadius);
		maxRadius = properties.getInteger("spells-lava-max-radius", maxRadius);
		defaultSearchDistance = properties.getInteger("spells-lava-search-distance", defaultSearchDistance);
	}
	
	private int				defaultRadius			= 1;
	private int				maxRadius				= 6;
	private int				defaultSearchDistance	= 64;
}
