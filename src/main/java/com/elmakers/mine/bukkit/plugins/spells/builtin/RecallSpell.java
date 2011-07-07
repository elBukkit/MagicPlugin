package com.elmakers.mine.bukkit.plugins.spells.builtin;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;

import com.elmakers.mine.bukkit.plugins.spells.Spell;
import com.elmakers.mine.bukkit.plugins.spells.utilities.PluginProperties;

public class RecallSpell extends Spell
{
	public class PlayerMarker
	{
		public Location location;
		public boolean isActive;
		
		public PlayerMarker(Location location)
		{
			this.location = location;
			isActive = true;
		}
	}
	
	private boolean autoDropOnDeath = true;
	private boolean autoDropIsInvisible = false;
	private boolean autoSpawn = true;
	private int disableDistance = 5;
	private HashMap<String, PlayerMarker> markers = new HashMap<String, PlayerMarker>();
	Material markerMaterial = Material.REDSTONE_TORCH_ON;
	
	public RecallSpell()
	{
		addVariant("spawn", Material.WOOD_DOOR, getCategory(), "Return to your home town", "spawn");
	}
	
	@Override
	public boolean onCast(String[] parameters)
	{
		if (parameters.length > 0 && parameters[0].equalsIgnoreCase("spawn"))
		{
			castMessage(player, "Returning you home");
			player.teleport(player.getWorld().getSpawnLocation());
			return true;
		}
		
		PlayerMarker marker = markers.get(player.getName());
		
		if (getYRotation() > 80)
		{
			if (marker == null || !marker.isActive && otherSpellHasPermission("spawn") && autoSpawn)
			{
				castMessage(player, "Returning you home");
				player.teleport(player.getWorld().getSpawnLocation());
			}
			else
			{
				if (marker == null || !marker.isActive) return false;
				
				double distance = getDistance(player.getLocation(), marker.location);

				if (distance < disableDistance && otherSpellHasPermission("spawn") && autoSpawn)
				{
					castMessage(player, "Returning you home");
					player.teleport(player.getWorld().getSpawnLocation());
				}
				else
				{
					castMessage(player, "Returning you to your marker");
					player.teleport(marker.location);
				}
			}
			return true;
		}
		
		if (marker == null || !marker.isActive)
		{
			return placeMarker(getTargetBlock());
		}
		
		return placeMarker(getTargetBlock());
	}
	
	protected boolean removeMarker(PlayerMarker marker)
	{
		if (marker == null || !marker.isActive) return false;
		
		marker.isActive = false;
		
		int x = (int)Math.floor(marker.location.getX());
		int y = (int)Math.floor(marker.location.getY());
		int z = (int)Math.floor(marker.location.getZ());
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
		
		if (removeMarker(markers.get(player.getName())))
		{
			castMessage(player, "You move your recall marker");
		}
		else
		{
			castMessage(player, "You place a recall marker");
		}
		
		Location location = player.getLocation();
		location.setX(targetBlock.getX());
		location.setY(targetBlock.getY());
		location.setZ(targetBlock.getZ());
		PlayerMarker marker = new PlayerMarker(location);
		markers.put(player.getName(), marker);
	
		player.setCompassTarget(location);
		
		targetBlock.setType(markerMaterial);
		return true;
	}

	@Override
	public String getName()
	{
		return "recall";
	}

	@Override
	public String getCategory()
	{
		return "exploration";
	}

	@Override
	public String getDescription()
	{
		return "Marks locations and returns you to them";
	}

	@Override
	public Material getMaterial()
	{
		return Material.COMPASS;
	}

	@Override
	public void onLoad(PluginProperties properties)
	{
		autoDropOnDeath = properties.getBoolean("spells-recall-auto-resurrect", autoDropOnDeath);
		autoDropIsInvisible = properties.getBoolean("spells-recall-auto-resurrect-invisible", autoDropIsInvisible);
		autoSpawn = properties.getBoolean("spells-recall-auto-spawn", autoSpawn);
		markerMaterial = properties.getMaterial("spells-recall-marker", markerMaterial);

		if (autoDropOnDeath)
		{
			//spells.registerEvent(SpellEventType.PLAYER_DEATH, this);
		}
	}

	@Override
	public void onPlayerDeath(Player player, EntityDeathEvent event)
	{
		if (autoDropOnDeath && hasSpellPermission(player))
		{
			PlayerMarker marker = markers.get(player.getName());
			if (marker == null || !marker.isActive)
			{
				placeMarker(getPlayerBlock());
			}
		}
	}

}
