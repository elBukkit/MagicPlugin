package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;

public class BlockSearchAction extends CompoundAction
{
	private static final int MAX_SEARCH_DISTANCE = 255;
    private BlockFace direction;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        direction = BlockFace.UP;
        try {
            direction = BlockFace.valueOf(parameters.getString("direction", "up").toUpperCase());
        }
        catch (Exception ex)
        {
            context.getLogger().info("Invalid search direction: " + parameters.getString("direction"));
            direction = BlockFace.DOWN;
        }
    }

    @Override
    public SpellResult perform(CastContext context)
    {
		Block attachBlock = context.getTargetBlock();
		BlockFace direction = BlockFace.UP;
		Block targetBlock = attachBlock.getRelative(direction);
		int distance = 0;

		while (context.isTargetable(targetBlock.getType()) && distance <= MAX_SEARCH_DISTANCE)
		{
			distance++;
			attachBlock = targetBlock;
			targetBlock = attachBlock.getRelative(direction);
		}
		if (context.isTargetable(targetBlock.getType()))
		{
			return SpellResult.NO_TARGET;
		}

        createActionContext(context);
        actionContext.setTargetLocation(targetBlock.getLocation());
        context.getBrush().setTarget(attachBlock.getLocation(), targetBlock.getLocation());
		return super.perform(actionContext);
	}

    @Override
    public boolean requiresTarget() {
        return true;
    }
}
