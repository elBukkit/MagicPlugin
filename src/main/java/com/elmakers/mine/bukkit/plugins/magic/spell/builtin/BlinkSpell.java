package com.elmakers.mine.bukkit.plugins.magic.spell.builtin;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.plugins.magic.spell.SpellResult;
import com.elmakers.mine.bukkit.plugins.magic.spell.TargetingSpell;

public class BlinkSpell extends TargetingSpell
{
	private int verticalSearchDistance = 255;
	private static int DEFAULT_PASSTHROUGH_RANGE = 4;

	protected SpellResult ascend()
	{
		Location targetLocation = getLocation();
		for (int i = 0; i < 2; i++) {
			if (!allowPassThrough(targetLocation.getBlock().getType())) return SpellResult.NO_TARGET;
			targetLocation.setY(targetLocation.getY() + 1);
		}
		Location location = findPlaceToStand(targetLocation, true);
		if (location != null) 
		{
			setTarget(location);
			getPlayer().teleport(location);
			return SpellResult.CAST;
		}
		return SpellResult.NO_TARGET;
	}

	protected SpellResult descend()
	{
		Location targetLocation = getLocation();
		for (int i = 0; i < 2; i++) {
			if (!allowPassThrough(targetLocation.getBlock().getType())) return SpellResult.NO_TARGET;
			targetLocation.setY(targetLocation.getY() - 1);
		}
		Location location = findPlaceToStand(targetLocation, false);
		if (location != null) 
		{
			setTarget(location);
			getPlayer().teleport(location);
			return SpellResult.CAST;
		}
		return SpellResult.NO_TARGET;
	}

	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		String elevateType = parameters.getString("type", "");
		
		Player player = getPlayer();
		if (player == null) {
			return SpellResult.PLAYER_REQUIRED;
		}

		boolean autoAscend = parameters.getBoolean("allow_ascend", true);
		boolean autoDescend = parameters.getBoolean("allow_descend", true);
		boolean autoPassthrough = parameters.getBoolean("allow_passthrough", true);

		if (elevateType.equals("descend") || (isLookingDown() && autoDescend))
		{
			return descend();
		}
		else if (elevateType.equals("ascend") || (isLookingUp() && autoAscend))
		{
			return ascend();
		}
		
		if (autoPassthrough)
		{
			Block firstBlock = getNextBlock();
			if (firstBlock == null) return SpellResult.NO_TARGET;
			
			if (!allowPassThrough(firstBlock.getType())) 
			{
				return SpellResult.NO_TARGET;
			}
			if (firstBlock != null && firstBlock.getType() != Material.AIR && !isWater(firstBlock.getType()))
			{
				int passthroughRange = (int)Math.floor(mage.getRangeMultiplier() * parameters.getInt("passthrough_range", DEFAULT_PASSTHROUGH_RANGE));
				setMaxRange(passthroughRange);
				offsetTarget(0, -1, 0);
				setReverseTargeting(true);
				setTargetSpaceRequired();
				targetThrough(Material.AIR);
			}
		}

		Block target = getTargetBlock();
		Block face = getPreviousBlock();

		if (target == null) 
		{
			return SpellResult.NO_TARGET;
		}

		World world = getWorld();

		Block destination = face;
		int distanceUp = 0;
		int distanceDown = 0;

		if (isReverseTargeting())
		{
			destination = target;
		}
		
		// Don't drop the player too far, and make sure there is somewhere to stand - unless they are flying
		if (!player.isFlying()) {
			Block groundBlock = destination.getRelative(BlockFace.DOWN);
			while (distanceDown < verticalSearchDistance && !isOkToStandOn(groundBlock.getType()))
			{
				destination = groundBlock;
				groundBlock = destination.getRelative(BlockFace.DOWN);
				distanceDown++;
			}
		}

		// Also check for a ledge above the target
		Block ledge = null;
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
			return SpellResult.NO_TARGET;
		}
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
		player.teleport(targetLocation);
		return SpellResult.CAST;
	}
}
