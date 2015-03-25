package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class TeleportAction extends BaseSpellAction
{
	private static int DEFAULT_PASSTHROUGH_RANGE = 4;
    private boolean autoPassthrough = true;
    private int passthroughRange;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        Mage mage = context.getMage();
        autoPassthrough = parameters.getBoolean("allow_passthrough", true);
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
			if (firstBlock != null && !context.isTransparent(firstBlock.getType())) {
                context.retarget(passthroughRange, 0, passthroughRange, 0, false, new Vector(0, -1, 0), true, 1);
                isPassthrough = true;
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
		int distanceUp = 0;
		int distanceDown = 0;
        int verticalSearchDistance = context.getVerticalSearchDistance();

		if (isPassthrough || destination == null)
		{
			destination = target;
		}
		
		// Don't drop the player too far, and make sure there is somewhere to stand - unless they are flying
		if (!(entity instanceof Player && ((Player)entity).isFlying())) {
            Location safeLocation = context.findPlaceToStand(destination.getLocation(), false);
			if (safeLocation != null)
            {
                destination = safeLocation.getBlock();
            }
		}

		// Also check for a ledge above the target
		Block ledge = null;
		if (!isPassthrough && (!face.equals(target.getRelative(BlockFace.DOWN)) || autoPassthrough))
		{
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
					&&	distanceUp < verticalSearchDistance
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
		} else {
			ledge = null;
		}

		if (ledge != null && distanceUp < distanceDown && context.isOkToStandOn(ledge.getType()))
		{
			destination = ledge.getRelative(BlockFace.UP);
		}

		Block oneUp = destination.getRelative(BlockFace.UP);
		Block twoUp = oneUp.getRelative(BlockFace.UP);
		if (!context.isOkToStandIn(oneUp.getType()) || !context.isOkToStandIn(twoUp.getType()))
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
        context.teleport(entity, targetLocation);
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
