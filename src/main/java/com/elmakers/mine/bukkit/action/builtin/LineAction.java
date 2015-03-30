package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.action.CompoundAction;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.Collection;

public class LineAction extends CompoundAction
{
	private static final int DEFAULT_SIZE = 16;
    private boolean incrementData;
    private int size;
    private int startDistance;
    private boolean reverse;
    private boolean startAtTarget;
    private boolean requireBlock;

    private int destination;
    private int current;
    private Vector direction;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        incrementData = parameters.getBoolean("increment_data", false);
        size = parameters.getInt("size", DEFAULT_SIZE);
        startDistance = parameters.getInt("start", 2);
        startAtTarget = parameters.getBoolean("start_at_target", false);
        reverse = parameters.getBoolean("reverse", false);
        requireBlock = parameters.getBoolean("require_block", false);

        Mage mage = context.getMage();
        size = (int)(mage.getConstructionMultiplier() * (float)this.size);
    }

    @Override
    public void reset(CastContext context) {
        super.reset(context);
        createActionContext(context);
        current = 0;

        Location startLocation = context.getEyeLocation();
        Location targetLocation = context.getTargetLocation();

        if (startLocation == null || targetLocation == null) return;

        Vector targetLoc = new Vector(targetLocation.getX(), targetLocation.getY(), targetLocation.getZ());
        Vector playerLoc = startLocation.toVector();

        direction = targetLoc.clone();
        direction.subtract(playerLoc);
        direction.normalize();

        if (startAtTarget && !reverse) {
            destination = size;
        } else {
            if (startDistance > 0) {
                playerLoc.add(direction.clone().multiply(startDistance));
            }

            destination = (int) playerLoc.distance(targetLoc);
            if (destination <= 0) return;
            destination = Math.min(destination, size);

            if (reverse) {
                direction = direction.multiply(-1);
            } else {
                targetLocation.setX(playerLoc.getX());
                targetLocation.setY(playerLoc.getY());
                targetLocation.setZ(playerLoc.getZ());
                actionContext.setTargetLocation(targetLocation);
            }
        }
    }

	@SuppressWarnings("deprecation")
	@Override
    public SpellResult perform(CastContext context)
    {
        MaterialBrush brush = context.getBrush();
        SpellResult result = SpellResult.NO_ACTION;
        while (current < destination)
        {
            Block currentTarget = actionContext.getTargetBlock();
            if (incrementData) {
                short data = current > 15 ? 15 : (short)current;
                brush.setData(data);
            }

            if (requireBlock) {
                Block lowerBlock = currentTarget.getRelative(BlockFace.DOWN);
                if (lowerBlock.getType() == Material.AIR || lowerBlock.getType() == brush.getMaterial()) {
                    advance(context);
                    skippedActions(context);
                    continue;
                }
            }
            SpellResult actionResult = super.perform(actionContext);
            result = result.min(actionResult);
            if (actionResult == SpellResult.PENDING) {
                break;
            }
            context.playEffects("iterate");
            advance(context);
        }

		return result;
	}

    protected void advance(CastContext context) {
        current++;
        Location target = actionContext.getTargetLocation();
        target.add(direction);
        actionContext.setTargetLocation(target);
        super.reset(context);
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }

    @Override
    public void getParameterNames(Collection<String> parameters) {
        super.getParameterNames(parameters);
        parameters.add("size");
        parameters.add("increment_data");
        parameters.add("require_block");
        parameters.add("reverse");
        parameters.add("start");
    }

    @Override
    public void getParameterOptions(Collection<String> examples, String parameterKey) {
        if (parameterKey.equals("increment_data") || parameterKey.equals("reverse") || parameterKey.equals("require_block")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_BOOLEANS)));
        } else if (parameterKey.equals("size") || parameterKey.equals("start")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_SIZES)));
        } else {
            super.getParameterOptions(examples, parameterKey);
        }
    }

    @Override
    public int getActionCount() {
        return destination * actions.getActionCount();
    }
}
