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
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

@Deprecated
public class MoveBlockAction extends BaseSpellAction
{
    private Vector offset;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        offset = ConfigurationUtils.getVector(parameters, "offset");
    }

	@Override
    public SpellResult perform(CastContext context)
	{
		Block targetBlock = context.getTargetBlock();
        if (!context.isDestructible(targetBlock))
        {
            return SpellResult.NO_TARGET;
        }

        if (!context.hasBuildPermission(targetBlock))
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

        // Mainly for FX
        Location targetLocation = context.getTargetLocation();
        targetLocation.setDirection(moveToBlock.getLocation().toVector().subtract(targetLocation.toVector()));
        context.setTargetLocation(targetLocation);

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
}
