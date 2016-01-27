package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.RandomUtils;

import de.slikey.effectlib.util.VectorUtils;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class ChangeContextAction extends CompoundAction {
    private Vector sourceOffset;
    private Vector relativeSourceOffset;
    private Vector targetOffset;
    private Vector relativeTargetOffset;
    private boolean targetSelf;
    private boolean targetEntityLocation;
    private boolean sourceAtTarget;
    private Double sourcePitch;
    private Vector randomSourceOffset;
    private Vector randomTargetOffset;
    private Double targetDirectionSpeed;
    private Double sourceDirectionSpeed;
    private Vector sourceDirection;
    private Vector targetDirection;
    private Vector sourceDirectionOffset;
    private Vector targetDirectionOffset;
    private String targetLocation;
    private String sourceLocation;
    private boolean persistTarget;
    private boolean attachBlock;
    private int snapTargetToSize;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        targetEntityLocation = parameters.getBoolean("target_entity", false);
        targetSelf = parameters.getBoolean("target_caster", false);
        sourceAtTarget = parameters.getBoolean("source_at_target", false);
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
        targetDirectionOffset = ConfigurationUtils.getVector(parameters, "source_direction_offset");
        persistTarget = parameters.getBoolean("persist_target", false);
        attachBlock = parameters.getBoolean("target_attachment", false);
        snapTargetToSize = parameters.getInt("target_snap", 0);
        targetLocation = parameters.getString("target_location");
        sourceLocation = parameters.getString("source_location");
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

    @Override
    public SpellResult step(CastContext context) {
        Entity sourceEntity = context.getEntity();
        Location sourceLocation = context.getEyeLocation().clone();
        if (this.sourceLocation != null) {
            Vector newSource = ConfigurationUtils.toVector(this.sourceLocation);
            if (newSource != null) {
                sourceLocation.setX(newSource.getX());
                sourceLocation.setY(newSource.getY());
                sourceLocation.setZ(newSource.getZ());
            }
        }
        Entity targetEntity = context.getTargetEntity();
        Location targetLocation = context.getTargetLocation();
        if (targetLocation != null) {
            targetLocation = targetLocation.clone();
            if (this.targetLocation != null) {
                Vector newTarget = ConfigurationUtils.toVector(this.targetLocation);
                if (newTarget != null) {
                    targetLocation.setX(newTarget.getX());
                    targetLocation.setY(newTarget.getY());
                    targetLocation.setZ(newTarget.getZ());
                }
            }
        }
        Vector direction = context.getDirection().normalize();
        if (sourceLocation == null)
        {
            return SpellResult.LOCATION_REQUIRED;
        }
        if (targetSelf)
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
            Block previousBlock = context.getPreviousBlock();
            if (previousBlock != null) {
                Location current = targetLocation;
                targetLocation = previousBlock.getLocation();
                context.getBrush().setTarget(current, targetLocation);
            }
        }
        if (sourcePitch != null)
        {
            sourceLocation.setPitch((float)(double)sourcePitch);
        }
        if (sourceOffset != null)
        {
            sourceLocation = sourceLocation.add(sourceOffset);
        }
        if (relativeSourceOffset != null)
        {
            Vector offset = VectorUtils.rotateVector(relativeSourceOffset, sourceLocation);
            sourceLocation.add(offset);
        }
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
        if (relativeTargetOffset != null & targetLocation != null)
        {
            Vector offset = VectorUtils.rotateVector(relativeTargetOffset, targetLocation);
            targetLocation.add(offset);
        }
        if (randomSourceOffset != null)
        {
            sourceLocation = RandomUtils.randomizeLocation(sourceLocation, randomSourceOffset);
        }
        if (randomTargetOffset != null && targetLocation != null)
        {
            targetLocation = RandomUtils.randomizeLocation(targetLocation, randomTargetOffset);
        }
        if (targetDirection != null && targetLocation != null)
        {
            targetLocation.setDirection(targetDirection);
        }
        if (sourceDirection != null)
        {
            sourceLocation.setDirection(sourceDirection);
            direction = sourceDirection.clone();
        }
        if (targetDirectionOffset != null && targetLocation != null)
        {
            targetLocation.setDirection(targetLocation.getDirection().add(targetDirectionOffset));
        }
        if (sourceDirectionOffset != null)
        {
            sourceLocation.setDirection(direction.add(sourceDirectionOffset));
        }
        if (sourceDirectionSpeed != null)
        {
            sourceLocation = sourceLocation.add(direction.clone().multiply(sourceDirectionSpeed));
        }
        if (targetDirectionSpeed != null && targetLocation != null)
        {
            targetLocation = targetLocation.add(direction.clone().multiply(targetDirectionSpeed));
        }
        if (sourceAtTarget && targetLocation != null)
        {
            sourceLocation.setX(targetLocation.getX());
            sourceLocation.setY(targetLocation.getY());
            sourceLocation.setZ(targetLocation.getZ());
            sourceLocation.setWorld(targetLocation.getWorld());
        }
        if (persistTarget)
        {
            context.setTargetLocation(targetLocation);
        }
        if (sourceLocation != null) {
            context.getMage().sendDebugMessage(ChatColor.GREEN + " Set new source location to " +
                    ChatColor.GRAY + sourceLocation.getBlockX() + ChatColor.DARK_GRAY + "," +
                    ChatColor.GRAY + sourceLocation.getBlockY() + ChatColor.DARK_GRAY + "," +
                    ChatColor.GRAY + sourceLocation.getBlockZ() + ChatColor.DARK_GRAY, 6);
        }
        if (targetLocation != null) {
            context.getMage().sendDebugMessage(ChatColor.DARK_GREEN + " Set new target location to " +
                    ChatColor.GRAY + targetLocation.getBlockX() + ChatColor.DARK_GRAY + "," +
                    ChatColor.GRAY + targetLocation.getBlockY() + ChatColor.DARK_GRAY + "," +
                    ChatColor.GRAY + targetLocation.getBlockZ() + ChatColor.DARK_GRAY, 6);
        }
        createActionContext(context, sourceEntity, sourceLocation, targetEntity, targetLocation);
        return startActions();
    }
}
