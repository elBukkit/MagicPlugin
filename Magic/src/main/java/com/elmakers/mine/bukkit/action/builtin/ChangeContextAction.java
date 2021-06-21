package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.magic.SourceLocation;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.random.RandomUtils;

import de.slikey.effectlib.util.VectorUtils;

public class ChangeContextAction extends CompoundAction {
    private Vector sourceOffset;
    private Vector relativeSourceOffset;
    private Vector targetOffset;
    private Vector relativeTargetOffset;
    private boolean sourceIsCaster;
    private boolean targetCaster;
    private Boolean targetSelf;
    private boolean useParentTargetEntity;
    private boolean useParentSourceEntity;
    private boolean useParentTargetLocation;
    private boolean useParentSourceLocation;
    private boolean targetEntityLocation;
    private boolean sourceAtTarget;
    private boolean sourceIsTarget;
    private boolean sourceDirectionAtTarget;
    private boolean sourceDirectionIsTarget;
    private Double sourcePitch;
    private Vector randomSourceOffset;
    private Vector randomTargetOffset;
    private Double targetDirectionSpeed;
    private Double sourceDirectionSpeed;
    private Vector sourceDirection;
    private Vector targetDirection;
    private Vector sourceDirectionOffset;
    private Vector targetDirectionOffset;
    private float relativeSourceDirectionYawOffset;
    private float relativeSourceDirectionPitchOffset;
    private float relativeTargetDirectionYawOffset;
    private float relativeTargetDirectionPitchOffset;
    private float sourceYawOffset;
    private float sourcePitchOffset;
    private float targetYawOffset;
    private float targetPitchOffset;
    private String absoluteTargetLocation;
    private String absoluteSourceLocation;
    private SourceLocation sourceLocation;
    private SourceLocation targetLocation;
    private boolean persistTarget;
    private boolean attachBlock;
    private boolean persistCaster;
    private int snapTargetToSize;
    private int sourcePitchMin;
    private int sourcePitchMax;
    private boolean orientPitch;
    private boolean swapSourceAndTarget;
    private boolean sourceUseMovementDirection;
    private boolean targetUseMovementDirection;
    private boolean useTargetMage;
    private MaterialSet destructible;
    private MaterialSet indestructible;

    // Communication between source and target modifications
    protected Vector direction;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        useParentTargetEntity = parameters.getBoolean("use_parent_target_entity", false);
        useParentSourceEntity = parameters.getBoolean("use_parent_source_entity", false);
        useParentTargetLocation = parameters.getBoolean("use_parent_target_location", false);
        useParentSourceLocation = parameters.getBoolean("use_parent_source_location", false);
        useTargetMage = parameters.getBoolean("use_target_mage", false);
        targetEntityLocation = parameters.getBoolean("target_entity", false);
        targetCaster = parameters.getBoolean("target_caster", false);
        sourceIsCaster = parameters.getBoolean("source_is_caster", false);
        targetSelf = parameters.contains("target_self") ? parameters.getBoolean("target_self") : null;
        sourceAtTarget = parameters.getBoolean("source_at_target", false);
        sourceIsTarget = parameters.getBoolean("source_is_target", false);
        sourceUseMovementDirection = parameters.getBoolean("source_use_movement_direction", false);
        targetUseMovementDirection = parameters.getBoolean("target_use_movement_direction", false);
        sourceDirectionIsTarget = parameters.getBoolean("source_direction_is_target", false);
        sourceDirectionAtTarget = parameters.getBoolean("source_direction_at_target", false);
        sourcePitch = ConfigurationUtils.getDouble(parameters, "source_pitch", null);
        sourceOffset = ConfigurationUtils.getVector(parameters, "source_offset");
        relativeSourceOffset = ConfigurationUtils.getVector(parameters, "relative_source_offset");
        targetOffset = ConfigurationUtils.getVector(parameters, "target_offset");
        relativeTargetOffset = ConfigurationUtils.getVector(parameters, "relative_target_offset");
        randomTargetOffset = ConfigurationUtils.getVector(parameters, "random_target_offset");
        randomSourceOffset = ConfigurationUtils.getVector(parameters, "random_source_offset");
        sourceDirection = ConfigurationUtils.getVector(parameters, "source_direction");
        targetDirection = ConfigurationUtils.getVector(parameters, "target_direction");
        sourceDirectionOffset = ConfigurationUtils.getVector(parameters, "source_direction_offset");
        targetDirectionOffset = ConfigurationUtils.getVector(parameters, "target_direction_offset");
        relativeSourceDirectionYawOffset = (float)parameters.getDouble("source_relative_direction_yaw_offset", 0);
        relativeSourceDirectionPitchOffset = (float)parameters.getDouble("source_relative_direction_pitch_offset", 0);
        relativeTargetDirectionYawOffset = (float)parameters.getDouble("target_relative_direction_yaw_offset", 0);
        relativeTargetDirectionPitchOffset = (float)parameters.getDouble("target_relative_direction_pitch_offset", 0);
        persistTarget = parameters.getBoolean("persist_target", false);
        attachBlock = parameters.getBoolean("target_attachment", false);
        persistCaster = parameters.getBoolean("persist_caster", false);
        snapTargetToSize = parameters.getInt("target_snap", 0);
        sourcePitchMin = parameters.getInt("source_pitch_min", 90);
        sourcePitchMax = parameters.getInt("source_pitch_max", -90);
        orientPitch = parameters.getBoolean("orient_pitch", true);
        sourceYawOffset = (float)parameters.getDouble("source_yaw_offset", 0);
        sourcePitchOffset = (float)parameters.getDouble("source_pitch_offset", 0);
        targetYawOffset = (float)parameters.getDouble("target_yaw_offset", 0);
        targetPitchOffset = (float)parameters.getDouble("target_pitch_offset", 0);
        swapSourceAndTarget = parameters.getBoolean("swap_source_and_target", false);
        String destructibleKey = parameters.getString("destructible_materials");
        if (destructibleKey != null && !destructibleKey.isEmpty()) {
            destructible = context.getController().getMaterialSetManager().fromConfig(destructibleKey);
        }
        String indestructibleKey = parameters.getString("indestructible_materials");
        if (indestructibleKey != null && !indestructibleKey.isEmpty()) {
            indestructible = context.getController().getMaterialSetManager().fromConfig(indestructibleKey);
        }

        absoluteTargetLocation = parameters.getString("target_location");
        absoluteSourceLocation = parameters.getString("source_location");

        // See if these are entity-specific locations;
        targetLocation = SourceLocation.tryCreate(absoluteTargetLocation, false);
        if (targetLocation != null) {
            absoluteTargetLocation = null;
        }
        sourceLocation = SourceLocation.tryCreate(absoluteSourceLocation, true);
        if (sourceLocation != null) {
            absoluteSourceLocation = null;
        }

        if (parameters.contains("target_direction_speed"))
        {
            targetDirectionSpeed = parameters.getDouble("target_direction_speed");
        }
        else
        {
            targetDirectionSpeed = null;
        }
        if (parameters.contains("source_direction_speed"))
        {
            sourceDirectionSpeed = parameters.getDouble("source_direction_speed");
        }
        else
        {
            sourceDirectionSpeed = null;
        }
    }

    protected Location modifySource(Location sourceLocation, CastContext context, Entity targetEntity) {
        boolean updateDirection = false;
        if (sourceDirectionIsTarget && targetEntity != null && sourceLocation != null)
        {
            sourceLocation.setDirection(targetEntity.getLocation().getDirection());
        }
        if (sourceUseMovementDirection && sourceLocation != null) {
            sourceLocation.setDirection(context.getMage().getVelocity());
        }
        if (sourceDirection != null && sourceLocation != null)
        {
            sourceLocation.setDirection(sourceDirection);
            updateDirection = true;
        }
        if (sourcePitch != null && sourceLocation != null)
        {
            sourceLocation.setPitch((float)(double)sourcePitch);
            updateDirection = true;
        }
        if (sourceYawOffset != 0 && sourceLocation != null)
        {
            sourceLocation.setYaw(sourceLocation.getYaw() + sourceYawOffset);
            updateDirection = true;
        }
        if (sourcePitchOffset != 0 && sourceLocation != null)
        {
            sourceLocation.setPitch(sourceLocation.getPitch() + sourcePitchOffset);
            updateDirection = true;
        }
        if (sourceLocation != null && sourceLocation.getPitch() > sourcePitchMin)
        {
            sourceLocation.setPitch(sourcePitchMin);
            updateDirection = true;
        }
        else if (sourceLocation != null && sourceLocation.getPitch() < sourcePitchMax)
        {
            sourceLocation.setPitch(sourcePitchMax);
            updateDirection = true;
        }
        if (sourceLocation != null && sourceOffset != null)
        {
            sourceLocation = sourceLocation.add(sourceOffset);
        }
        if (relativeSourceOffset != null && sourceLocation != null)
        {
            Location relativeSource;
            if (persistCaster) {
                relativeSource = context.getMage().getEyeLocation();
            } else {
                relativeSource = sourceLocation;
            }

            if (relativeSource != null) {
                if (!orientPitch) {
                    relativeSource.setPitch(0);
                }

                //If persistCaster is true, it makes the vector relative to the caster and not what the sourceLocation may
                Vector offset = VectorUtils.rotateVector(relativeSourceOffset, relativeSource);
                sourceLocation.add(offset);
            }
        }
        if (randomSourceOffset != null && sourceLocation != null)
        {
            sourceLocation = RandomUtils.randomizeLocation(sourceLocation, randomSourceOffset);
        }

        // Given a yaw and pitch, this adjusts the source direction after rotating it.
        if ((relativeSourceDirectionYawOffset != 0  || relativeSourceDirectionPitchOffset != 0) && sourceLocation != null)
        {
            Vector relativeDirection = sourceLocation.getDirection();
            relativeDirection = VectorUtils.rotateVector(relativeDirection, relativeSourceDirectionYawOffset, relativeSourceDirectionPitchOffset);
            sourceLocation.setDirection(relativeDirection);
            updateDirection = true;
        }

        // This direction is used in modifyTarget, so update it if we've changed it in any way
        if (updateDirection && sourceLocation != null) {
            direction = sourceLocation.getDirection();
        }
        if (sourceDirectionOffset != null && sourceLocation != null && direction != null)
        {
            sourceLocation.setDirection(direction.add(sourceDirectionOffset));
        }
        if (sourceDirectionSpeed != null && sourceLocation != null && direction != null)
        {
            sourceLocation = sourceLocation.add(direction.clone().multiply(sourceDirectionSpeed));
        }
        return sourceLocation;
    }

    protected Location modifyTarget(Location targetLocation, CastContext context, Entity targetEntity) {
        if (snapTargetToSize > 0 && targetLocation != null)
        {
            // This is kind of specific to how Towny does things... :\
            int x = targetLocation.getBlockX();
            int z = targetLocation.getBlockZ();
            int xresult = x / snapTargetToSize;
            int zresult = z / snapTargetToSize;
            boolean xneedfix = x % snapTargetToSize != 0;
            boolean zneedfix = z % snapTargetToSize != 0;
            targetLocation.setX(snapTargetToSize * (xresult - (x < 0 && xneedfix ? 1 : 0)));
            targetLocation.setZ(snapTargetToSize * (zresult - (z < 0 && zneedfix ? 1 : 0)));
        }
        if (targetOffset != null && targetLocation != null)
        {
            targetLocation = targetLocation.add(targetOffset);
        }
        if (relativeTargetOffset != null && targetLocation != null)
        {
            Location relativeTarget;

            if (persistCaster) {
                relativeTarget = context.getMage().getEyeLocation();
            } else {
                relativeTarget = targetLocation;
            }

            if (!orientPitch) {
                relativeTarget.setPitch(0);
            }

            Vector offset = VectorUtils.rotateVector(relativeTargetOffset, relativeTarget);
            targetLocation.add(offset);
        }
        if (randomTargetOffset != null && targetLocation != null)
        {
            targetLocation = RandomUtils.randomizeLocation(targetLocation, randomTargetOffset);
        }
        if (targetUseMovementDirection && targetLocation != null) {
            Mage targetMage = context.getController().getRegisteredMage(targetEntity);
            if (targetMage != null) {
                targetLocation.setDirection(targetMage.getVelocity());
            } else if (targetEntity != null) {
                targetLocation.setDirection(targetEntity.getVelocity());
            }
        }

        if (targetDirection != null && targetLocation != null)
        {
            targetLocation.setDirection(targetDirection);
        }
        if (targetLocation != null && targetYawOffset != 0)
        {
            targetLocation.setYaw(targetLocation.getYaw() + targetYawOffset);
        }
        if (targetLocation != null && targetPitchOffset != 0)
        {
            targetLocation.setPitch(targetLocation.getPitch() + targetPitchOffset);
        }

        // Given a yaw and pitch, this adjusts the target direction after rotating it.
        if (targetLocation != null && (relativeTargetDirectionYawOffset != 0  || relativeTargetDirectionPitchOffset != 0))
        {
            Vector relativeDirection = targetLocation.getDirection();
            relativeDirection = VectorUtils.rotateVector(relativeDirection, relativeTargetDirectionYawOffset, relativeTargetDirectionPitchOffset);
            targetLocation.setDirection(relativeDirection);
        }

        if (targetDirectionOffset != null && targetLocation != null)
        {
            targetLocation.setDirection(targetLocation.getDirection().add(targetDirectionOffset));
        }
        if (targetDirectionSpeed != null && targetLocation != null)
        {
            targetLocation = targetLocation.add(direction.clone().multiply(targetDirectionSpeed));
        }
        return targetLocation;
    }

    @Override
    public SpellResult step(CastContext context) {
        Mage sourceMage = null;
        Entity sourceEntity = context.getEntity();
        Location sourceLocation = context.getEyeLocation();
        Entity targetEntity = context.getTargetEntity();
        Location targetLocation = context.getTargetLocation();
        if (useTargetMage) {
            sourceMage = context.getController().getMage(targetEntity);
        }
        if (sourceLocation != null) {
            sourceLocation = sourceLocation.clone();
        }
        if (this.absoluteSourceLocation != null && !this.absoluteSourceLocation.isEmpty()) {
            Vector newSource = ConfigurationUtils.toVector(this.absoluteSourceLocation);
            if (newSource != null) {
                if (sourceLocation == null) {
                    World world = context.getWorld();
                    if (world == null) {
                        return SpellResult.WORLD_REQUIRED;
                    }
                    sourceLocation = new Location(world, newSource.getX(), newSource.getY(), newSource.getZ());
                } else {
                    sourceLocation.setX(newSource.getX());
                    sourceLocation.setY(newSource.getY());
                    sourceLocation.setZ(newSource.getZ());
                }
            } else {
                context.getLogger().warning("Invalid source location in spell " + context.getSpell().getKey() + ": " + this.absoluteSourceLocation);
            }
        }
        Block targetBlock = null;
        Block previousBlock = null;
        if (targetLocation != null) {
            targetLocation = targetLocation.clone();
            if (this.absoluteTargetLocation != null && !this.absoluteTargetLocation.isEmpty()) {
                Vector newTarget = ConfigurationUtils.toVector(this.absoluteTargetLocation);
                if (newTarget != null) {
                    targetLocation.setX(newTarget.getX());
                    targetLocation.setY(newTarget.getY());
                    targetLocation.setZ(newTarget.getZ());
                } else {
                    context.getLogger().warning("Invalid target location in spell " + context.getSpell().getKey() + ": " + this.absoluteTargetLocation);
                }
            }
        }
        if (swapSourceAndTarget)
        {
            Entity swapEntity = targetEntity;
            targetEntity = sourceEntity;
            sourceEntity = swapEntity;
            Location swapLocation = targetLocation;
            targetLocation = sourceLocation;
            sourceLocation = swapLocation;
        }

        if (useParentTargetEntity) {
            CastContext parent = context.getParent();
            Entity parentTargetEntity = null;
            while (parentTargetEntity == null && parent != null) {
                parentTargetEntity = parent.getTargetEntity();
                parent = parent.getParent();
            }
            targetEntity = parentTargetEntity;
        }

        if (useParentSourceEntity) {
            CastContext parent = context.getParent();
            Entity parentSourceEntity = null;
            while (parentSourceEntity == null && parent != null) {
                parentSourceEntity = parent.getEntity();
                parent = parent.getParent();
            }
            sourceEntity = parentSourceEntity;
        }

        if (useParentTargetLocation) {
            CastContext parent = context.getParent();
            Location parentTargetLocation = null;
            while (parentTargetLocation == null && parent != null) {
                parentTargetLocation = parent.getTargetLocation();
                parent = parent.getParent();
            }
            targetLocation = parentTargetLocation;
        }

        if (useParentSourceLocation) {
            CastContext parent = context.getParent();
            Location parentSourceLocation = null;
            while (parentSourceLocation == null && parent != null) {
                parentSourceLocation = parent.getLocation();
                parent = parent.getParent();
            }
            sourceLocation = parentSourceLocation;
        }

        direction = context.getDirection();
        if (direction != null) {
            direction.normalize();
        }
        if (targetCaster)
        {
            targetEntity = sourceEntity;
            targetLocation = sourceLocation;
        }
        else if (targetEntityLocation && targetEntity != null)
        {
            targetLocation = targetEntity.getLocation();
        }
        if (attachBlock)
        {
            previousBlock = context.getPreviousBlock();
            if (previousBlock != null) {
                Location current = targetLocation;
                targetBlock = previousBlock;
                context.getBrush().setTarget(current, current);
                previousBlock = context.getPreviousPreviousBlock();
            }
        }

        // Apply offsets and other transformations
        sourceLocation = modifySource(sourceLocation, context, targetEntity);
        targetLocation = modifyTarget(targetLocation, context, targetEntity);

        if (sourceDirectionAtTarget && targetLocation != null && sourceLocation != null)
        {
            sourceLocation.setDirection(targetLocation.toVector().subtract(sourceLocation.toVector()));
        }
        if (sourceAtTarget && targetLocation != null && sourceLocation != null)
        {
            sourceLocation.setX(targetLocation.getX());
            sourceLocation.setY(targetLocation.getY());
            sourceLocation.setZ(targetLocation.getZ());
            sourceLocation.setWorld(targetLocation.getWorld());
            sourceLocation = modifySource(sourceLocation, context, targetEntity);
        }
        if (persistTarget)
        {
            context.setTargetLocation(targetLocation);
        }
        if (sourceLocation != null) {
            context.getMage().sendDebugMessage(ChatColor.GREEN + " Set new source location to "
                    + ChatColor.GRAY + sourceLocation.getBlockX() + ChatColor.DARK_GRAY + ","
                    + ChatColor.GRAY + sourceLocation.getBlockY() + ChatColor.DARK_GRAY + ","
                    + ChatColor.GRAY + sourceLocation.getBlockZ() + ChatColor.DARK_GRAY, 6);
        }
        if (targetLocation != null) {
            context.getMage().sendDebugMessage(ChatColor.DARK_GREEN + " Set new target location to "
                    + ChatColor.GRAY + targetLocation.getBlockX() + ChatColor.DARK_GRAY + ","
                    + ChatColor.GRAY + targetLocation.getBlockY() + ChatColor.DARK_GRAY + ","
                    + ChatColor.GRAY + targetLocation.getBlockZ() + ChatColor.DARK_GRAY, 6);
        }
        if (sourceIsTarget)
        {
            sourceEntity = targetEntity;
        }
        if (sourceIsCaster) {
            sourceEntity = context.getMage().getEntity();
            sourceLocation = null;
        }
        createActionContext(context, sourceMage, sourceEntity, sourceLocation, targetEntity, targetLocation);
        if (targetSelf != null)
        {
            actionContext.setTargetsCaster(targetSelf);
        }
        if (targetBlock != null) {
            actionContext.setTargetBlock(targetBlock);
        }
        if (previousBlock != null) {
            actionContext.setPreviousBlock(previousBlock);
        }
        if (this.sourceLocation != null) {
            actionContext.setLocation(this.sourceLocation.getLocation(actionContext));
        }
        if (this.targetLocation != null) {
            actionContext.setTargetLocation(this.targetLocation.getLocation(actionContext));
        }
        if (destructible != null) {
            actionContext.setDestructible(destructible);
        }
        if (indestructible != null) {
            actionContext.setIndestructible(indestructible);
        }
        return startActions();
    }
}
