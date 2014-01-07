package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.EffectUtils;
import com.elmakers.mine.bukkit.utilities.ParticleType;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class BlinkSpell extends Spell
{
	private boolean autoAscend = true;
	private boolean autoDescend = true;
	private boolean autoPassthrough = true;
	private int verticalSearchDistance = 255;

	protected boolean ascend()
	{
		Location location = findPlaceToStand(player.getLocation(), true);
		if (location != null) 
		{
			castMessage("You ascend");
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
			castMessage("You descend");
			player.teleport(location);
			return true;
		}
		return false;
	}

	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		Location playerLocation = player.getEyeLocation();
		String elevateType = parameters.getString("type", "");
		if (elevateType.equals("descend"))
		{
			if (!descend())
			{
				castMessage("Nowhere to go down");
				return SpellResult.NO_TARGET;
			}
			EffectUtils.playEffect(playerLocation, ParticleType.PORTAL, 1, 16);
			playerLocation.getWorld().playSound(playerLocation, Sound.ENDERMAN_TELEPORT, 1.0f, 1.5f);
			return SpellResult.SUCCESS;
		}
		else if (elevateType.equals("ascend"))
		{
			if (!ascend())
			{
				castMessage("Nowhere to go up");
				return SpellResult.NO_TARGET;
			}
			EffectUtils.playEffect(playerLocation, ParticleType.PORTAL, 1, 16);
			playerLocation.getWorld().playSound(playerLocation, Sound.ENDERMAN_TELEPORT, 1.0f, 1.5f);
			return SpellResult.SUCCESS;
		}

		if (getYRotation() < -80 && autoDescend)
		{
			if (descend())
			{
				EffectUtils.playEffect(playerLocation, ParticleType.PORTAL, 1, 16);
				playerLocation.getWorld().playSound(playerLocation, Sound.ENDERMAN_TELEPORT, 1.0f, 1.5f);
				return SpellResult.SUCCESS;
			}
		}

		if (getYRotation() > 80 && autoAscend)
		{
			if (ascend())
			{
				EffectUtils.playEffect(playerLocation, ParticleType.PORTAL, 1, 16);
				playerLocation.getWorld().playSound(playerLocation, Sound.ENDERMAN_TELEPORT, 1.0f, 1.5f);
				return SpellResult.SUCCESS;
			}
		}

		if (autoPassthrough)
		{
			Block firstBlock = getNextBlock();
			if (firstBlock != null && firstBlock.getType() != Material.AIR && !isWater(firstBlock.getType()))
			{
				setReverseTargeting(true);
				setTargetHeightRequired(2);
				targetThrough(Material.AIR);
			}
			else
			{
				targetThrough(Material.GLASS);
				targetThrough(Material.THIN_GLASS);
				targetThrough(Material.STAINED_GLASS);
				targetThrough(Material.STAINED_GLASS_PANE);
				targetThrough(Material.WATER);
				targetThrough(Material.STATIONARY_WATER);
			}
		} else {
			targetThrough(Material.GLASS);
			targetThrough(Material.THIN_GLASS);
			targetThrough(Material.STAINED_GLASS);
			targetThrough(Material.STAINED_GLASS_PANE);
			targetThrough(Material.WATER);
			targetThrough(Material.STATIONARY_WATER);
		}

		Block target = getTargetBlock();
		Block face = getLastBlock();

		if (target == null) 
		{
			castMessage("Nowhere to blink to");
			return SpellResult.NO_TARGET;
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
		Block groundBlock = destination.getRelative(BlockFace.DOWN);
		while (distanceDown < verticalSearchDistance && !isOkToStandOn(groundBlock.getType()))
		{
			destination = groundBlock;
			groundBlock = destination.getRelative(BlockFace.DOWN);
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
				oneUp = ledge.getRelative(BlockFace.UP);
				twoUp = oneUp.getRelative(BlockFace.UP);
				inFront = inFront.getRelative(BlockFace.UP);
				ledge = ledge.getRelative(BlockFace.UP);
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

		Block oneUp = destination.getRelative(BlockFace.UP);
		Block twoUp = oneUp.getRelative(BlockFace.UP);
		if (!isOkToStandIn(oneUp.getType()) || !isOkToStandIn(twoUp.getType()))
		{
			castMessage("You can't fit in there!");
			return SpellResult.NO_TARGET;
		}
		castMessage("Blink!");
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
		EffectUtils.playEffect(playerLocation, ParticleType.PORTAL, 1, 16);
		playerLocation.getWorld().playSound(playerLocation, Sound.ENDERMAN_TELEPORT, 1.0f, 1.5f);
		EffectUtils.playEffect(targetLocation, ParticleType.PORTAL, 1, 16);
		playerLocation.getWorld().playSound(targetLocation, Sound.ENDERMAN_TELEPORT, 1.0f, 1.5f);
		return SpellResult.SUCCESS;
	}

	@Override
	public void onLoadTemplate(ConfigurationNode properties)  
	{
		autoAscend = properties.getBoolean("allow_ascend", autoAscend);
		autoDescend = properties.getBoolean("allow_descend", autoDescend);
		autoPassthrough = properties.getBoolean("allow_passthrough", autoPassthrough);
	}
}
