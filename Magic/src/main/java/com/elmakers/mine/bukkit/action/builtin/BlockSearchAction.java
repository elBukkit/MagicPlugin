package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class BlockSearchAction extends CompoundAction
{
    private static final int MAX_SEARCH_DISTANCE = 255;
    private BlockFace direction;

    @Override
    public void processParameters(CastContext context, ConfigurationSection parameters)
    {
        super.processParameters(context, parameters);
        direction = BlockFace.UP;
        try {
            String directionString = parameters.getString("direction", "up").toUpperCase();
            if (directionString.equals("FORWARD")) {
                direction = context.getFacingDirection();
            } else {
                direction = BlockFace.valueOf(directionString);
            }
        }
        catch (Exception ex)
        {
            context.getLogger().warning("Invalid search direction: " + parameters.getString("direction"));
            direction = BlockFace.DOWN;
        }
    }

    @Override
    public SpellResult step(CastContext context)
    {
        Block attachBlock = context.getTargetBlock();
        Block targetBlock = attachBlock.getRelative(direction);
        int distance = 0;

        while (context.isTargetable(targetBlock) && distance <= MAX_SEARCH_DISTANCE)
        {
            actionContext.setTargetLocation(targetBlock.getLocation());
            actionContext.playEffects("search");
            distance++;
            attachBlock = targetBlock;
            targetBlock = attachBlock.getRelative(direction);
        }
        if (context.isTargetable(targetBlock))
        {
            return SpellResult.NO_TARGET;
        }

        actionContext.setTargetLocation(targetBlock.getLocation());
        context.getBrush().setTarget(attachBlock.getLocation(), targetBlock.getLocation());
        return startActions();
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }
}
