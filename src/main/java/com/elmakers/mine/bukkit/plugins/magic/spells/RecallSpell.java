package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.entity.EntityDeathEvent;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellEventType;
import com.elmakers.mine.bukkit.utilities.PluginProperties;

public class RecallSpell extends Spell
{
	public Location location;
    public boolean isActive;
	private boolean autoDropOnDeath = true;
	private boolean autoDropIsInvisible = false;
	private boolean autoSpawn = true;
	private int disableDistance = 5;
	Material markerMaterial = Material.REDSTONE_TORCH_ON;
	
	@Override
	public boolean onCast(Map<String, Object> parameters)
	{
	    if (autoDropOnDeath)
        {
            spells.registerEvent(SpellEventType.PLAYER_DEATH, this);
        }
        
	    if (parameters.containsKey("type"))
	    {
	        String typeString = (String)parameters.get("type");
	        if (typeString.equals("spawn"))
	        {
	            castMessage(player, "Returning you home");
	            player.teleport(player.getWorld().getSpawnLocation());
	            return true; 
	        }
	    }
		
		if (getYRotation() > 80)
		{
			if (!isActive && autoSpawn)
			{
				castMessage(player, "Returning you home");
				player.teleport(player.getWorld().getSpawnLocation());
			}
			else
			{
				if (!isActive) return false;
				
				double distance = getDistance(player.getLocation(), location);

				if (distance < disableDistance && autoSpawn)
				{
					castMessage(player, "Returning you home");
					player.teleport(player.getWorld().getSpawnLocation());
				}
				else
				{
					castMessage(player, "Returning you to your marker");
					player.teleport(location);
				}
			}
			return true;
		}
		
		if (!isActive)
		{
			return placeMarker(getTargetBlock());
		}
		
		return placeMarker(getTargetBlock());
	}
	
	protected boolean removeMarker()
	{
		if (!isActive || location == null) return false;
		
		isActive = false;
		
		int x = (int)Math.floor(location.getX());
		int y = (int)Math.floor(location.getY());
		int z = (int)Math.floor(location.getZ());
		Block targetBlock = player.getWorld().getBlockAt(x, y, z);
		if (targetBlock != null && targetBlock.getType() == markerMaterial)
		{
			targetBlock.setType(Material.AIR);
		}		
		
		return true;
	}
	
	protected boolean placeMarker(Block target)
	{
		if (target == null)
		{
			castMessage(player, "No target");
			return false;
		}
		Block targetBlock = target.getFace(BlockFace.UP);
		if (targetBlock.getType() != Material.AIR)
		{
			targetBlock = getFaceBlock();
		}
		if (targetBlock.getType() != Material.AIR)
		{
			castMessage(player, "Can't place a marker there");
			return false;
		}
		
		if (removeMarker())
		{
			castMessage(player, "You move your recall marker");
		}
		else
		{
			castMessage(player, "You place a recall marker");
		}
		
		location = player.getLocation();
		location.setX(targetBlock.getX());
		location.setY(targetBlock.getY());
		location.setZ(targetBlock.getZ());
	
		player.setCompassTarget(location);
		
		targetBlock.setType(markerMaterial);
		isActive = true;
		
		return true;
	}

	@Override
	public void onLoad(PluginProperties properties)
	{
		autoDropOnDeath = properties.getBoolean("spells-recall-auto-resurrect", autoDropOnDeath);
		autoDropIsInvisible = properties.getBoolean("spells-recall-auto-resurrect-invisible", autoDropIsInvisible);
		autoSpawn = properties.getBoolean("spells-recall-auto-spawn", autoSpawn);
		markerMaterial = properties.getMaterial("spells-recall-marker", markerMaterial);
	}

	@Override
	public void onPlayerDeath(EntityDeathEvent event)
	{
		if (autoDropOnDeath)
		{
			if (!isActive)
			{
			    sendMessage(player, "Use recall to return to where you died");
				placeMarker(getPlayerBlock());
			}
		}
	}

}
