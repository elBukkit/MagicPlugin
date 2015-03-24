package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.RandomUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.util.Random;

public class ChangeContextAction extends CompoundAction {
    private Vector sourceOffset;
    private Vector targetOffset;
    private boolean targetSelf;
    private Vector randomSourceOffset;
    private Vector randomTargetOffset;
    private Double targetDirectionSpeed;
    private Double sourceDirectionSpeed;
    private Vector sourceDirection;
    private Vector targetDirection;
    private Vector sourceDirectionOffset;
    private Vector targetDirectionOffset;
    private boolean persistTarget;
    private boolean attachBlock;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        targetSelf = parameters.getBoolean("target_caster");
        targetOffset = ConfigurationUtils.getVector(parameters, "target_offset");
        sourceOffset = ConfigurationUtils.getVector(parameters, "source_offset");
        targetOffset = ConfigurationUtils.getVector(parameters, "random_target_offset");
        sourceOffset = ConfigurationUtils.getVector(parameters, "random_source_offset");
        randomTargetOffset = ConfigurationUtils.getVector(parameters, "random_target_offset");
        randomSourceOffset = ConfigurationUtils.getVector(parameters, "random_source_offset");
        sourceDirection = ConfigurationUtils.getVector(parameters, "source_direction");
        targetDirection = ConfigurationUtils.getVector(parameters, "target_direction");
        targetDirection = ConfigurationUtils.getVector(parameters, "target_direction");
        sourceDirectionOffset = ConfigurationUtils.getVector(parameters, "source_direction_offset");
        targetDirectionOffset = ConfigurationUtils.getVector(parameters, "source_direction_offset");
        persistTarget = parameters.getBoolean("persist_target", false);
        attachBlock = parameters.getBoolean("target_attachment", false);
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
    public SpellResult perform(CastContext context) {
        Entity sourceEntity = context.getEntity();
        Location sourceLocation = context.getEyeLocation().clone();
        Entity targetEntity = context.getTargetEntity();
        Location targetLocation = context.getTargetLocation().clone();
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
        if (attachBlock)
        {
            Block previousBlock = context.getPreviousBlock();
            if (previousBlock != null) {
                Location current = targetLocation;
                targetLocation = previousBlock.getLocation();
                context.getBrush().setTarget(current, targetLocation);
            }
        }
        if (sourceOffset != null)
        {
            sourceLocation = sourceLocation.add(sourceOffset);
        }
        if (targetOffset != null)
        {
            targetLocation = targetLocation.add(targetOffset);
        }
        if (randomSourceOffset != null)
        {
            sourceLocation = RandomUtils.randomizeLocation(sourceLocation, randomSourceOffset);
        }
        if (randomTargetOffset != null)
        {
            targetLocation = RandomUtils.randomizeLocation(targetLocation, randomTargetOffset);
        }
        if (targetDirection != null)
        {
            targetLocation.setDirection(targetDirection);
        }
        if (sourceDirection != null)
        {
            sourceLocation.setDirection(sourceDirection);
            direction = sourceDirection.clone();
        }
        if (targetDirectionOffset != null)
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
        if (targetDirectionSpeed != null)
        {
            targetLocation = targetLocation.add(direction.clone().multiply(targetDirectionSpeed));
        }
        if (persistTarget)
        {
            context.setTargetLocation(targetLocation);
        }
        CastContext newContext = createContext(context, sourceEntity, sourceLocation, targetEntity, targetLocation);
        return performActions(newContext);
    }
}
