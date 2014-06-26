package com.elmakers.mine.bukkit.spell.builtin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.TargetingSpell;

public class BlinkSpell extends TargetingSpell
{
	private int verticalSearchDistance = 255;
	private static int DEFAULT_PASSTHROUGH_RANGE = 4;

	protected SpellResult ascend(Entity entity)
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
            delayTeleport(entity, location);
			return SpellResult.CAST;
		}
		return SpellResult.NO_TARGET;
	}

	protected SpellResult descend(Entity entity)
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
            delayTeleport(entity, location);
			return SpellResult.CAST;
		}
		return SpellResult.NO_TARGET;
	}

	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		String elevateType = parameters.getString("type", "");
		
		Entity entity = mage.getLivingEntity();
        if (entity == null) {
			return SpellResult.ENTITY_REQUIRED;
		}

		boolean autoAscend = parameters.getBoolean("allow_ascend", true);
		boolean autoDescend = parameters.getBoolean("allow_descend", true);
		boolean autoPassthrough = parameters.getBoolean("allow_passthrough", true);

        boolean isPassthrough = false;

		if (elevateType.equals("descend") || (isLookingDown() && autoDescend))
		{
			return descend(entity);
		}
		else if (elevateType.equals("ascend") || (isLookingUp() && autoAscend))
		{
			return ascend(entity);
		}
		
		if (autoPassthrough && !isLookingUp() && !isLookingDown())
		{
			Block firstBlock = getInteractBlock();
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
				setTargetSpaceRequired();
                setTargetMinOffset(1);
                isPassthrough = true;
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

		if (isPassthrough)
		{
			destination = target;
		}
		
		// Don't drop the player too far, and make sure there is somewhere to stand - unless they are flying
		if (!(entity instanceof Player && ((Player)entity).isFlying())) {
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
		if (!isPassthrough && (!face.equals(target.getRelative(BlockFace.DOWN)) || autoAscend))
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
			entity.getLocation().getYaw(),
            entity.getLocation().getPitch()
		);
		setTarget(targetLocation);
        delayTeleport(entity, targetLocation);
		return SpellResult.CAST;
	}

    /**
     * Delay tp by one tick, mainly for effects.
     *
     * @param entity
     * @param location
     */
    protected void delayTeleport(final Entity entity, final Location location) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(controller.getPlugin(), new Runnable() {
            @Override
            public void run() {
                entity.teleport(location);
            }
        }, 1);
    }
}
