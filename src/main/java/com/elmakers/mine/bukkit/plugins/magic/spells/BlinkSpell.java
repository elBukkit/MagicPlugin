package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.elmakers.mine.bukkit.effects.EffectUtils;
import com.elmakers.mine.bukkit.effects.ParticleType;
import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class BlinkSpell extends Spell
{
	private int verticalSearchDistance = 255;

	protected SpellResult ascend()
	{
		Location targetLocation = getPlayer().getLocation();
		targetLocation.setY(targetLocation.getY() + 2);
		Location location = findPlaceToStand(targetLocation, true);
		if (location != null) 
		{
			castMessage("You ascend");
			getPlayer().teleport(location);
			
			Location playerLocation = mage.getLocation();
			EffectUtils.playEffect(playerLocation, ParticleType.PORTAL, 1, 16);
			playerLocation.getWorld().playSound(playerLocation, Sound.ENDERMAN_TELEPORT, 1.0f, 1.5f);
			
			EffectUtils.playEffect(location, ParticleType.PORTAL, 1, 16);
			location.getWorld().playSound(playerLocation, Sound.ENDERMAN_TELEPORT, 1.0f, 1.5f);
			
			return SpellResult.SUCCESS;
		}
		mage.castMessage("Nowhere to go up");
		return SpellResult.NO_TARGET;
	}

	protected SpellResult descend()
	{
		Location targetLocation = getPlayer().getLocation();
		targetLocation.setY(targetLocation.getY() - 2);
		Location location = findPlaceToStand(targetLocation, false);
		if (location != null) 
		{
			castMessage("You descend");
			getPlayer().teleport(location);
			
			Location playerLocation = mage.getLocation();
			EffectUtils.playEffect(playerLocation, ParticleType.PORTAL, 1, 16);
			playerLocation.getWorld().playSound(playerLocation, Sound.ENDERMAN_TELEPORT, 1.0f, 1.5f);
			
			EffectUtils.playEffect(location, ParticleType.PORTAL, 1, 16);
			location.getWorld().playSound(playerLocation, Sound.ENDERMAN_TELEPORT, 1.0f, 1.5f);
			
			return SpellResult.SUCCESS;
		}
		mage.castMessage("Nowhere to go down");
		return SpellResult.NO_TARGET;
	}

	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		Location playerLocation = getPlayer().getEyeLocation();
		String elevateType = parameters.getString("type", "");

		boolean autoAscend = parameters.getBoolean("allow_ascend", true);
		boolean autoDescend = parameters.getBoolean("allow_descend", true);
		boolean autoPassthrough = parameters.getBoolean("allow_passthrough", true);

		if (elevateType.equals("descend") || (getYRotation() < -80 && autoDescend))
		{
			return descend();
		}
		else if (elevateType.equals("ascend") || getYRotation() > 80 && autoAscend)
		{
			return ascend();
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

		World world = getPlayer().getWorld();

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
			getPlayer().getLocation().getYaw(),
			getPlayer().getLocation().getPitch()
		);
		getPlayer().teleport(targetLocation);
		EffectUtils.playEffect(playerLocation, ParticleType.PORTAL, 1, 16);
		playerLocation.getWorld().playSound(playerLocation, Sound.ENDERMAN_TELEPORT, 1.0f, 1.5f);
		EffectUtils.playEffect(targetLocation, ParticleType.PORTAL, 1, 16);
		playerLocation.getWorld().playSound(targetLocation, Sound.ENDERMAN_TELEPORT, 1.0f, 1.5f);
		return SpellResult.SUCCESS;
	}
}
