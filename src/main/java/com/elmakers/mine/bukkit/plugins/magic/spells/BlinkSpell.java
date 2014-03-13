package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class BlinkSpell extends Spell
{
	private int verticalSearchDistance = 255;
	private static int DEFAULT_PASSTHROUGH_RANGE = 4;

	protected SpellResult ascend()
	{
		Location targetLocation = getLocation();
		targetLocation.setY(targetLocation.getY() + 2);
		Location location = findPlaceToStand(targetLocation, true);
		if (location != null) 
		{
			castMessage("You ascend");
			setTarget(location);
			getPlayer().teleport(location);
			return SpellResult.CAST;
		}
		mage.castMessage("Nowhere to go up");
		return SpellResult.NO_TARGET;
	}

	protected SpellResult descend()
	{
		Location targetLocation = getLocation();
		targetLocation.setY(targetLocation.getY() - 2);
		Location location = findPlaceToStand(targetLocation, false);
		if (location != null) 
		{
			castMessage("You descend");
			setTarget(location);
			getPlayer().teleport(location);
			return SpellResult.CAST;
		}
		mage.castMessage("Nowhere to go down");
		return SpellResult.NO_TARGET;
	}

	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		String elevateType = parameters.getString("type", "");

		boolean autoAscend = parameters.getBoolean("allow_ascend", true);
		boolean autoDescend = parameters.getBoolean("allow_descend", true);
		boolean autoPassthrough = parameters.getBoolean("allow_passthrough", true);

		if (elevateType.equals("descend") || (getYRotation() < -80 && autoDescend))
		{
			return descend();
		}
		else if (elevateType.equals("ascend") || (getYRotation() > 80 && autoAscend))
		{
			return ascend();
		}
		
		if (autoPassthrough)
		{
			Block firstBlock = getNextBlock();
			if (firstBlock != null && firstBlock.getType() != Material.AIR && !isWater(firstBlock.getType()))
			{
				setMaxRange(parameters.getInteger("passthrough_range", DEFAULT_PASSTHROUGH_RANGE));
				offsetTarget(0, -1, 0);
				setReverseTargeting(true);
				setTargetSpaceRequired();
				targetThrough(Material.AIR);
			}
		}

		Block target = getTargetBlock();
		Block face = getLastBlock();

		if (target == null) 
		{
			castMessage("Nowhere to blink to");
			return SpellResult.NO_TARGET;
		}

		World world = getWorld();

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
		if (!isReverseTargeting() && (!face.equals(target.getRelative(BlockFace.DOWN)) || autoAscend))
		{
			ledge = target;
			Block inFront = face;
			Block oneUp = ledge.getRelative(BlockFace.UP);
			Block twoUp = oneUp.getRelative(BlockFace.UP);
			Block faceOneUp = face.getRelative(BlockFace.UP);
			Block faceTwoUp = faceOneUp.getRelative(BlockFace.UP);
			
			if (!autoPassthrough && !autoAscend && (!isTransparent(oneUp.getType()) || !isTransparent(twoUp.getType()) || !isTransparent(face.getType()) || !isTransparent(faceOneUp.getType()) || !isTransparent(faceTwoUp.getType()))) {
				ledge = null;
			} else {
				// Check for ability to pass through the face block
				while
				(
						(autoPassthrough || autoAscend || 
						isTransparent(face.getType()) && isTransparent(faceOneUp.getType()) && isTransparent(faceTwoUp.getType()))
					&&		distanceUp < verticalSearchDistance
					&&		isOkToStandIn(inFront.getType())
					&&	(
								!isOkToStandOn(ledge.getType())
						||		!isOkToStandIn(oneUp.getType())
						||		!isOkToStandIn(twoUp.getType())
						)
				) 
				{
					faceOneUp = faceOneUp.getRelative(BlockFace.UP);
					faceTwoUp = faceOneUp.getRelative(BlockFace.UP);
					inFront = inFront.getRelative(BlockFace.UP);
					ledge = ledge.getRelative(BlockFace.UP);
					distanceUp++;
				}
			}
		} else {
			ledge = null;
		}

		if (ledge != null && distanceUp < distanceDown && isOkToStandOn(ledge.getType()))
		{
			destination = ledge.getRelative(BlockFace.UP);
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
		setTarget(targetLocation);
		getPlayer().teleport(targetLocation);
		return SpellResult.CAST;
	}
}
