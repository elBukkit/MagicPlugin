package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseTeleportAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class AscendAction extends BaseTeleportAction
{
    @Override
    public SpellResult perform(CastContext context)
	{
        Entity entity = context.getEntity();
        if (entity == null) {
            return SpellResult.ENTITY_REQUIRED;
        }
		Location targetLocation = context.getLocation();
		for (int i = 0; i < 2; i++) {
			if (!context.allowPassThrough(targetLocation.getBlock())) return SpellResult.NO_TARGET;
			targetLocation.setY(targetLocation.getY() + 1);
		}
		Location location = context.findPlaceToStand(targetLocation, verticalSearchDistance, true);
		if (location == null && !safe)
		{
			location = context.getTargetLocation();
			location.setPitch(targetLocation.getPitch());
			location.setYaw(targetLocation.getYaw());
			verticalSearchDistance = 0;
		}
		if (location != null) 
		{
			return teleport(context, entity, location);
		}
		return SpellResult.NO_TARGET;
	}

    @Override
    public boolean isUndoable() {
        return true;
    }
}
