package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.entity.EntityDeathEvent;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellEventType;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

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
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		if (autoDropOnDeath)
		{
			spells.registerEvent(SpellEventType.PLAYER_DEATH, this);
		}

		String typeString = parameters.getString("type", "");
		if (typeString.equals("spawn"))
		{
			castMessage("Returning you home");
			player.teleport(findPlaceToStand(player.getWorld().getSpawnLocation(), true));
			return SpellResult.SUCCESS; 
		}

		if (getYRotation() > 80)
		{
			if (!isActive && autoSpawn)
			{
				castMessage("Returning you home");
				player.teleport(findPlaceToStand(player.getWorld().getSpawnLocation(), true));
			}
			else
			{
				if (!isActive) return SpellResult.NO_TARGET;

				double distance = getDistance(player.getLocation(), location);

				if (distance < disableDistance && autoSpawn)
				{
					castMessage("Returning you home");
					player.teleport(findPlaceToStand(player.getWorld().getSpawnLocation(), true));
				}
				else
				{
					castMessage("Returning you to your marker");
					player.teleport(location);
				}
			}
			return SpellResult.SUCCESS;
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

	protected SpellResult placeMarker(Block target)
	{
		if (target == null)
		{
			castMessage("No target");
			return SpellResult.NO_TARGET;
		}
		Block targetBlock = target.getRelative(BlockFace.UP);
		if (targetBlock.getType() != Material.AIR)
		{
			targetBlock = getFaceBlock();
		}
		if (targetBlock.getType() != Material.AIR)
		{
			castMessage("Can't place a marker there");
			return SpellResult.NO_TARGET;
		}

		if (removeMarker())
		{
			castMessage("You move your recall marker");
		}
		else
		{
			castMessage("You place a recall marker");
		}

		location = player.getLocation();
		location.setX(targetBlock.getX());
		location.setY(targetBlock.getY());
		location.setZ(targetBlock.getZ());

		player.setCompassTarget(location);

		targetBlock.setType(markerMaterial);
		isActive = true;

		return SpellResult.SUCCESS;
	}

	@Override
	public void onLoad(ConfigurationNode properties)  
	{
		autoDropOnDeath = properties.getBoolean("auto_resurrect", autoDropOnDeath);
		autoDropIsInvisible = properties.getBoolean("auto_resurrect_invisible", autoDropIsInvisible);
		autoSpawn = properties.getBoolean("allow_spawn", autoSpawn);
		markerMaterial = properties.getMaterial("recall_marker", markerMaterial);
	}

	@Override
	public void onPlayerDeath(EntityDeathEvent event)
	{
		if (autoDropOnDeath)
		{
			if (!isActive)
			{
				sendMessage("Use recall to return to where you died");
				placeMarker(getPlayerBlock());
			}
		}
	}

}
