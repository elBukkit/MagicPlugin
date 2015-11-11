package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseTeleportAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class TeleportAction extends BaseTeleportAction
{
	private static int DEFAULT_PASSTHROUGH_RANGE = 4;
    private boolean autoPassthrough = true;
    private int passthroughRange;
    private int ledgeSearchDistance = 2;
	private boolean safe = true;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        Mage mage = context.getMage();
        ledgeSearchDistance = parameters.getInt("ledge_range", 2);
        autoPassthrough = parameters.getBoolean("allow_passthrough", true);
		safe = parameters.getBoolean("safe", true);
        passthroughRange = (int)Math.floor(mage.getRangeMultiplier() * parameters.getInt("passthrough_range", DEFAULT_PASSTHROUGH_RANGE));
    }

    @Override
    public SpellResult perform(CastContext context)
    {
		Entity entity = context.getEntity();
        if (entity == null) {
			return SpellResult.ENTITY_REQUIRED;
		}

        boolean isPassthrough = false;
		if (autoPassthrough)
		{
            Block firstBlock = context.getInteractBlock();
			if (firstBlock == null) return SpellResult.NO_TARGET;

			if (!context.allowPassThrough(firstBlock.getType()))
			{
				return SpellResult.NO_TARGET;
			}
			if (firstBlock != null && !context.isPassthrough(firstBlock.getType())) {
                context.retarget(passthroughRange, 0, passthroughRange, 0, false, -1, true, 1);
                isPassthrough = true;
				context.getMage().sendDebugMessage(ChatColor.BLUE + "Teleporting passthrough engaged", 11);
			}
		}

		Block target = context.getTargetBlock();
		Block face = context.getPreviousBlock();

		if (target == null)
		{
			return SpellResult.NO_TARGET;
		}

        if (face == null)
        {
            face = target;
        }

		World world = context.getWorld();
		Block destination = face;
        int verticalSearchDistance = context.getVerticalSearchDistance();

		if (isPassthrough || destination == null)
		{
			destination = target;
		}

		// Don't drop the player too far, and make sure there is somewhere to stand - unless they are flying
		if (!(entity instanceof Player && ((Player)entity).isFlying())) {
            Location safeLocation = context.findPlaceToStand(destination.getLocation(), verticalSearchDistance, false);
			if (safeLocation != null)
            {
                destination = safeLocation.getBlock();
				context.getMage().sendDebugMessage(ChatColor.BLUE + "Teleporting destination changed to safe location", 11);
			}
		}

		// Also check for a ledge above the target
		Block ledge = null;
		if (!isPassthrough && (!face.equals(target.getRelative(BlockFace.DOWN)) || autoPassthrough))
		{
			int distanceUp = 0;
			ledge = target;
			Block inFront = face;
			Block oneUp = ledge.getRelative(BlockFace.UP);
			Block twoUp = oneUp.getRelative(BlockFace.UP);
			Block faceOneUp = face.getRelative(BlockFace.UP);
			Block faceTwoUp = faceOneUp.getRelative(BlockFace.UP);
			
			if (!autoPassthrough && (!context.isTransparent(oneUp.getType()) || !context.isTransparent(twoUp.getType())
                    || !context.isTransparent(face.getType()) || !context.isTransparent(faceOneUp.getType())
                    || !context.isTransparent(faceTwoUp.getType())))
            {
				ledge = null;
			}
            else
            {
				// Check for ability to pass through the face block
				while
				(
						(autoPassthrough ||
                            (   context.isTransparent(face.getType())
                            &&  context.isTransparent(faceOneUp.getType())
                            &&  context.isTransparent(faceTwoUp.getType())
                            )
                        )
					&&	distanceUp < ledgeSearchDistance
					&&	context.isOkToStandIn(inFront.getType())
					&&	(
								!context.isOkToStandOn(ledge.getType())
						||		!context.isOkToStandIn(oneUp.getType())
						||		!context.isOkToStandIn(twoUp.getType())
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

            if (distanceUp >= ledgeSearchDistance) {
                ledge = null;
            }
		} else {
			ledge = null;
		}

		if (ledge != null && context.isOkToStandOn(ledge.getType()))
		{
			destination = ledge.getRelative(BlockFace.UP);
			context.getMage().sendDebugMessage(ChatColor.BLUE + "Teleporting hit ledge", 11);
		}

		Block oneUp = destination.getRelative(BlockFace.UP);
		if (!context.isOkToStandIn(destination.getType()) || !context.isOkToStandIn(oneUp.getType()))
		{
			context.getMage().sendDebugMessage(ChatColor.RED + "Teleporting entity failed, can't stand in " +
					ChatColor.DARK_RED + destination.getType() +
					ChatColor.RED + " or " +
					ChatColor.DARK_RED + oneUp.getType()
					, 11);
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

		context.getMage().sendDebugMessage(ChatColor.AQUA + "Teleporting entity " +
						ChatColor.DARK_AQUA + entity.getType() +
						ChatColor.AQUA + " to " +
						ChatColor.BLUE + targetLocation.getBlockX() + ChatColor.GRAY + "," +
						ChatColor.BLUE + targetLocation.getBlockY() + ChatColor.GRAY + "," +
						ChatColor.BLUE + targetLocation.getBlockZ()
				, 11);

        context.teleport(entity, targetLocation, verticalSearchDistance, safe);
		return SpellResult.CAST;
	}

    @Override
    public boolean isUndoable() {
        return true;
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }
}
