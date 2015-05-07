package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

public class MoveBlockAction extends BaseSpellAction
{
    private Vector offset;
    private boolean setTarget;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        offset = ConfigurationUtils.getVector(parameters, "offset");
        setTarget = parameters.getBoolean("set_target", false);
    }

	@Override
    public SpellResult perform(CastContext context)
	{
		Block targetBlock = context.getTargetBlock();
        if (!context.isDestructible(targetBlock))
        {
            return SpellResult.NO_TARGET;
        }

        if (!context.hasBreakPermission(targetBlock))
        {
            return SpellResult.INSUFFICIENT_PERMISSION;
        }

        double distanceSquared = offset.lengthSquared();
		Block moveToBlock = targetBlock;
        int distance = 0;
        Vector moveBlock = offset.clone().normalize();
        while (distance * distance < distanceSquared) {
            moveToBlock = moveToBlock.getLocation().add(moveBlock).getBlock();
            if (moveToBlock.getType() != Material.AIR) {
                return SpellResult.NO_TARGET;
            }
            distance++;
        }
		if (!context.hasBuildPermission(moveToBlock)) {
			return SpellResult.INSUFFICIENT_PERMISSION;
		}

        MaterialAndData blockState = new MaterialAndData(targetBlock);
        context.registerForUndo(targetBlock);
        context.registerForUndo(moveToBlock);

        if (setTarget) {
            Location sourceLocation = context.getTargetLocation();
            Location targetLocation = moveToBlock.getLocation();
            targetLocation.setDirection(sourceLocation.toVector().subtract(targetLocation.toVector()));
            context.setTargetLocation(targetLocation);
        } else {
            // Kind of hack for the way some current spells use FX
            // They play effects from the source to the target
            Location targetLocation = context.getTargetLocation();
            targetLocation.setDirection(moveToBlock.getLocation().toVector().subtract(targetLocation.toVector()));
            context.setTargetLocation(targetLocation);
        }

        targetBlock.setType(Material.AIR);
        blockState.modify(moveToBlock);
        MageController controller = context.getController();
        moveToBlock.setMetadata("breakable", new FixedMetadataValue(controller.getPlugin(), 1));
        moveToBlock.setMetadata("reflect_chance", new FixedMetadataValue(controller.getPlugin(), 1));

		return SpellResult.CAST;
	}

    @Override
    public boolean requiresTarget() {
        return true;
    }

    @Override
    public boolean isUndoable() {
        return true;
    }

    @Override
    public boolean requiresBreakPermission() {
        return true;
    }
}
