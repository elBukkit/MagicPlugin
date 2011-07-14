package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.utilities.PluginProperties;

public class BlinkSpell extends Spell
{
	private int maxRange = 0;
	private boolean autoAscend = true;
	private boolean autoDescend = true;
	private boolean autoPassthrough = true;
	private int verticalSearchDistance = 255;
	
	protected boolean ascend()
	{
		Location location = findPlaceToStand(player.getLocation(), true);
		if (location != null) 
		{
			castMessage(player, "You ascend");
			player.teleport(location);
			return true;
		}
		return false;
	}
	
	protected boolean descend()
	{
		Location location = findPlaceToStand(player.getLocation(), false);
		if (location != null) 
		{
			castMessage(player, "You descend");
			player.teleport(location);
			return true;
		}
		return false;
	}
	
	@Override
	public boolean onCast(String[] parameters)
	{
	    setMaxRange(255, false);
	    
		if (parameters.length > 0)
		{
			if (parameters[0].equalsIgnoreCase("ascend"))
			{
				if (!ascend())
				{
					castMessage(player, "Nowhere to go up");
					return false;
				}
				return true;
			}
			
			if (parameters[0].equalsIgnoreCase("descend"))
			{
				if (!descend())
				{
					castMessage(player, "Nowhere to go down");
					return false;
				}
				return true;
			}
			
			try
			{
			    int range = Integer.parseInt(parameters[0]);
			    setMaxRange(range, true);
			    autoAscend = false;
			    autoDescend = false;
			    autoPassthrough = false;
			}
			catch (NumberFormatException ex)
			{
			    return false;
			}
		}
		
		// No parameters
		
		// Auto ascend + descend
		
		if (getYRotation() < -80 && autoDescend)
		{
			if (descend())
			{
				return true;
			}
		}
		
		if (getYRotation() > 80 && autoAscend)
		{
			if (ascend())
			{
				return true;
			}
		}
		
		if (autoPassthrough)
		{
			Block firstBlock = getNextBlock();
			if (firstBlock.getType() != Material.AIR && !isWater(firstBlock.getType()))
			{
				setReverseTargeting(true);
				setTargetHeightRequired(2);
				targetThrough(Material.AIR);
			}
			else
			{
				targetThrough(Material.GLASS);
			}
		}
		
		Block target = getTargetBlock();
		Block face = getLastBlock();
		
		if (target == null) 
		{
			castMessage(player, "Nowhere to blink to");
			return false;
		}
		if (maxRange > 0 && getDistance(player,target) > maxRange) 
		{
			castMessage(player, "Can't blink that far");
			return false;
		}
		
		World world = player.getWorld();
		
		// Don't drop the player too far, and make sure there is somewhere to stand
    	Block destination = face;
    	int distanceUp = 0;
    	int distanceDown = 0;
    	if (isReverseTargeting())
    	{
    		destination = target;
    	}
    	Block groundBlock = destination.getFace(BlockFace.DOWN);
    	while (distanceDown < verticalSearchDistance && !isOkToStandOn(groundBlock.getType()))
    	{
    		destination = groundBlock;
    		groundBlock = destination.getFace(BlockFace.DOWN);
    		distanceDown++;
    	}
    	
    	Block ledge = null;
    	// Also check for a ledge above the target
    	if (!isReverseTargeting())
    	{
    		ledge = target;
    		Block inFront = face;
    		Block oneUp = null;
    		Block twoUp = null;
    		
        	do
        	{
        		oneUp = ledge.getFace(BlockFace.UP);
        		twoUp = oneUp.getFace(BlockFace.UP);
        		inFront = inFront.getFace(BlockFace.UP);
        		ledge = ledge.getFace(BlockFace.UP);
        		distanceUp++;
        	}
        	while
        	(
        			distanceUp < verticalSearchDistance
        	&&		isOkToStandIn(inFront.getType())
        	&&	(
        				!isOkToStandOn(groundBlock.getType())
        		||		!isOkToStandIn(oneUp.getType())
        		||		!isOkToStandIn(twoUp.getType())
        		)
        	);
        	
    	}
    	
    	if (ledge != null && distanceUp < distanceDown)
    	{
    		destination = ledge;
    	}
    	
		Block oneUp = destination.getFace(BlockFace.UP);
		Block twoUp = oneUp.getFace(BlockFace.UP);
		if (!isOkToStandIn(oneUp.getType()) || !isOkToStandIn(twoUp.getType()))
		{
			castMessage(player, "You can't fit in there!");
			return false;
		}
		castMessage(player, "Blink!");
		Location targetLocation = new Location
		(
			world,
			destination.getX() + 0.5,
			destination.getY(),
			destination.getZ() + 0.5,
			player.getLocation().getYaw(),
			player.getLocation().getPitch()
		);
		player.teleport(targetLocation);
		
		return true;
	}

	@Override
	public void onLoad(PluginProperties properties)
	{
		maxRange = properties.getInteger("spells-blink-range", maxRange);
		autoAscend = properties.getBoolean("spells-blink-auto-ascend", autoAscend);
		autoDescend = properties.getBoolean("spells-blink-aauto-decend", autoDescend);
		autoPassthrough = properties.getBoolean("spells-blink-auto-passthrough", autoPassthrough);
	}
}
