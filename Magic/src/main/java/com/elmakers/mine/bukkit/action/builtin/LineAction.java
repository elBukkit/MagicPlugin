package com.elmakers.mine.bukkit.action.builtin;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.DefaultMaterials;
import com.elmakers.mine.bukkit.spell.BaseSpell;

public class LineAction extends CompoundAction
{
    private static final int DEFAULT_SIZE = 16;
    private boolean incrementData;
    private int size;
    private int startDistance;
    private boolean reverse;
    private boolean startAtTarget;
    private boolean requireBlock;
    private boolean reorient;

    private int destination;
    private int current;
    private Vector direction;

    @Override
    public void processParameters(CastContext context, ConfigurationSection parameters)
    {
        super.processParameters(context, parameters);
        incrementData = parameters.getBoolean("increment_data", false);
        size = parameters.getInt("size", DEFAULT_SIZE);
        startDistance = parameters.getInt("start", 2);
        startAtTarget = parameters.getBoolean("start_at_target", false);
        reverse = parameters.getBoolean("reverse", false);
        requireBlock = parameters.getBoolean("require_block", false);
        reorient = parameters.getBoolean("reorient", false);

        Mage mage = context.getMage();
        size = (int)(mage.getConstructionMultiplier() * this.size);
    }

    @Override
    public void reset(CastContext context) {
        super.reset(context);
        current = 0;
        destination = 0;

        Location startLocation = context.getEyeLocation();
        Location targetLocation = context.getTargetLocation();

        if (startLocation == null) return;
        Vector playerLoc = startLocation.toVector();
        Vector targetLoc = null;
        if (targetLocation == null) {
            targetLocation = startLocation.clone();
            direction = startLocation.getDirection();
        } else {
            targetLoc = new Vector(targetLocation.getX(), targetLocation.getY(), targetLocation.getZ());
            direction = targetLoc.clone();
            direction.subtract(playerLoc);
            direction.normalize();
        }

        // Need to fix all this when there is no target...
        // and add a reorient option

        destination = size;
        if (!startAtTarget || reverse) {
            if (startDistance > 0) {
                playerLoc.add(direction.clone().multiply(startDistance));
            }

            if (targetLoc != null) {
                destination = (int)playerLoc.distance(targetLoc);
            }
            if (destination <= 0) return;
            destination = Math.min(destination, size);

            if (reverse) {
                direction = direction.multiply(-1);
            } else {
                targetLocation.setX(playerLoc.getX());
                targetLocation.setY(playerLoc.getY());
                targetLocation.setZ(playerLoc.getZ());
            }
        }
        actionContext.setTargetLocation(targetLocation);
    }

    @Override
    public SpellResult step(CastContext context)
    {
        MaterialBrush brush = context.getBrush();
        Block currentTarget = actionContext.getTargetBlock();
        if (incrementData) {
            short data = current > 15 ? 15 : (short)current;
            brush.setData(data);
        }

        if (requireBlock) {
            Block lowerBlock = currentTarget.getRelative(BlockFace.DOWN);
            if (DefaultMaterials.isAir(lowerBlock.getType()) || lowerBlock.getType() == brush.getMaterial()) {
                next(context);
                skippedActions(context);
                return SpellResult.NO_TARGET;
            }
        }
        context.playEffects("iterate");
        return startActions();
    }

    @Override
    public boolean next(CastContext context) {
        current++;
        Location target = actionContext.getTargetLocation();
        if (reorient) {
            direction = context.getDirection();
        }
        target.add(direction);
        actionContext.setTargetLocation(target);
        return (current <= destination);
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("size");
        parameters.add("increment_data");
        parameters.add("require_block");
        parameters.add("reverse");
        parameters.add("start");
        parameters.add("reorient");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("increment_data") || parameterKey.equals("reverse")
            || parameterKey.equals("require_block") || parameterKey.equals("reorient")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_BOOLEANS));
        } else if (parameterKey.equals("size") || parameterKey.equals("start")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_SIZES));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }

    @Override
    public int getActionCount() {
        return destination * super.getActionCount();
    }
}
