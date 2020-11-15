package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.action.BaseTeleportAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.utility.TextUtils;

public class TeleportAction extends BaseTeleportAction
{
    private static int DEFAULT_PASSTHROUGH_RANGE = 4;
    private boolean autoPassthrough = true;
    private boolean useTargetLocation;
    private int passthroughRange;
    private int ledgeSearchDistance = 2;
    private boolean direct = false;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        Mage mage = context.getMage();
        ledgeSearchDistance = parameters.getInt("ledge_range", 2);
        autoPassthrough = parameters.getBoolean("allow_passthrough", false);
        useTargetLocation = parameters.getBoolean("use_target_location", false);
        passthroughRange = (int)Math.floor(mage.getRangeMultiplier() * parameters.getInt("passthrough_range", DEFAULT_PASSTHROUGH_RANGE));
        direct = parameters.getBoolean("direct", false);
    }

    @Override
    public SpellResult perform(CastContext context)
    {
        Entity entity = context.getEntity();
        if (entity == null) {
            return SpellResult.ENTITY_REQUIRED;
        }

        Location targetLocation;
        if (direct) {
            targetLocation = context.getTargetLocation();
            if (targetLocation == null) {
                return SpellResult.NO_TARGET;
            }
        } else {
            boolean isPassthrough = false;
            if (autoPassthrough)
            {
                Block firstBlock = context.getInteractBlock();
                if (firstBlock == null) return SpellResult.NO_TARGET;

                if (!context.allowPassThrough(firstBlock))
                {
                    return SpellResult.NO_TARGET;
                }
                if (!context.isPassthrough(firstBlock)) {
                    context.retarget(passthroughRange, 0, passthroughRange, 0, false, -1, true, 1);
                    isPassthrough = true;
                    context.getMage().sendDebugMessage(ChatColor.BLUE + "Teleporting passthrough engaged", 11);
                }
            }

            Block target;

            // This is a special chain to handle how this is invoked from a projectile, which sets itself as the source.
            LivingEntity sourceEntity = context.getLivingEntity();
            Location sourceLocation = sourceEntity == null ? context.getEyeLocation() : sourceEntity.getEyeLocation();
            Block source = sourceLocation.getBlock();
            Block face = context.getPreviousBlock();
            if (useTargetLocation) {
                Location location = context.getTargetLocation();
                target = location == null ? null : location.getBlock();
                face = null;
            } else {
                target = context.getTargetBlock();
            }

            if (target == null)
            {
                context.getMage().sendDebugMessage(ChatColor.RED + "Teleporting entity failed, no target block", 11);
                return SpellResult.NO_TARGET;
            }

            // Special-case to prevent passthrough of half-width blocks
            if (!autoPassthrough && target.getX() == source.getX() && target.getY() == source.getY() && target.getZ() == source.getZ())
            {
                context.getMage().sendDebugMessage(ChatColor.RED + "Teleporting entity failed, can't stand in half block at " + ChatColor.DARK_RED + target.getType(), 11);
                return SpellResult.NO_TARGET;
            }

            if (face == null)
            {
                face = target;
            }

            context.getMage().sendDebugMessage(ChatColor.GREEN + "Teleporting to block: " + TextUtils.printBlock(target) + ChatColor.GREEN + " face: " + TextUtils.printBlock(face), 12);

            World world = context.getWorld();
            Block destination = face;
            int verticalSearchDistance = context.getVerticalSearchDistance();

            if (isPassthrough)
            {
                destination = target;
            }

            // Also check for a ledge above the target
            Block ledge = null;
            int distanceUp = 0;
            if (!isPassthrough && (!face.equals(target.getRelative(BlockFace.DOWN)) || autoPassthrough))
            {
                ledge = target;
                Block inFront = face;
                Block oneUp = ledge.getRelative(BlockFace.UP);
                Block twoUp = oneUp.getRelative(BlockFace.UP);
                Block faceOneUp = face.getRelative(BlockFace.UP);
                Block faceTwoUp = faceOneUp.getRelative(BlockFace.UP);

                // Look up along the hit wall until
                if (!autoPassthrough && (!context.isTransparent(face) || !context.isTransparent(faceOneUp)
                        || !context.isTransparent(faceTwoUp) || context.isTransparent(ledge)))
                {
                    ledge = null;
                }
                else
                {
                    // Check for ability to pass through the face block
                    while
                    (
                            (autoPassthrough || (
                                    context.isTransparent(face)
                                &&  context.isTransparent(faceOneUp)
                                &&  context.isTransparent(faceTwoUp)
                            ))
                        &&    distanceUp < ledgeSearchDistance
                        &&    context.isOkToStandIn(inFront)
                        &&    (
                                    !context.isOkToStandOn(ledge)
                            ||        !context.isOkToStandIn(oneUp)
                            ||        !context.isOkToStandIn(twoUp)
                            )
                    )
                    {
                        faceOneUp = faceOneUp.getRelative(BlockFace.UP);
                        faceTwoUp = faceTwoUp.getRelative(BlockFace.UP);
                        inFront = inFront.getRelative(BlockFace.UP);
                        oneUp = oneUp.getRelative(BlockFace.UP);
                        twoUp = twoUp.getRelative(BlockFace.UP);
                        ledge = ledge.getRelative(BlockFace.UP);
                        distanceUp++;
                    }
                }

                if (distanceUp >= ledgeSearchDistance) {
                    ledge = null;
                }
            }

            if (ledge != null && context.isOkToStandOn(ledge))
            {
                // Check to see if the ground is closer to the target than the ledge
                Block floor = face;
                while (!context.isOkToStandOn(floor) && distanceUp >= 0) {
                    floor = floor.getRelative(BlockFace.DOWN);
                    distanceUp--;
                }
                if (distanceUp > 0) {
                    destination = floor.getRelative(BlockFace.UP);
                    context.getMage().sendDebugMessage(ChatColor.GREEN + "Teleporting found ledge at " + TextUtils.printBlock(ledge) + ChatColor.GREEN + " but ground is closer: " + TextUtils.printBlock(destination), 11);
                } else {
                    destination = ledge.getRelative(BlockFace.UP);
                    context.getMage().sendDebugMessage(ChatColor.GREEN + "Teleporting hit ledge at " + TextUtils.printBlock(destination), 11);
                }
            }

            // Don't drop the player too far, and make sure there is somewhere to stand - unless they are flying
            if (!(entity instanceof Player && ((Player)entity).isFlying()) && safe) {
                Location safeLocation = context.findPlaceToStand(destination.getLocation(), verticalSearchDistance, false);
                if (safeLocation != null)
                {
                    destination = safeLocation.getBlock();
                    context.getMage().sendDebugMessage(ChatColor.GREEN + "Teleporting destination changed to safe location", 11);
                }
            }

            Block oneUp = destination.getRelative(BlockFace.UP);
            if (!context.isOkToStandIn(destination) || !context.isOkToStandIn(oneUp))
            {
                context.getMage().sendDebugMessage(ChatColor.RED + "Teleporting entity failed, can't stand in "
                        + ChatColor.DARK_RED + destination.getType()
                        + ChatColor.RED + " or "
                        + ChatColor.DARK_RED + oneUp.getType(),
                        11);
                return SpellResult.NO_TARGET;
            }
            targetLocation = new Location(
                world,
                destination.getX() + 0.5,
                destination.getY(),
                destination.getZ() + 0.5,
                entity.getLocation().getYaw(),
                entity.getLocation().getPitch()
            );
        }

        context.getMage().sendDebugMessage(ChatColor.AQUA + "Teleporting entity "
                + ChatColor.DARK_AQUA + entity.getType()
                + ChatColor.AQUA + " to " + TextUtils.printLocation(targetLocation), 11);

        return teleport(context, entity, targetLocation);
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
