package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.action.BaseTeleportAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class WarpAction extends BaseTeleportAction
{
    private String warpKey;

    @Override
    public void processParameters(CastContext context, ConfigurationSection parameters) {
        super.processParameters(context, parameters);
        warpKey = parameters.getString("warp");
    }

    @Override
    public SpellResult perform(CastContext context)
    {
        if (warpKey == null || warpKey.isEmpty()) {
            context.getLogger().warning("Warp action missing 'warp' parameter");
            return SpellResult.FAIL;
        }
        Entity entity = context.getTargetEntity();
        if (entity == null) {
            return SpellResult.ENTITY_REQUIRED;
        }

        Location targetLocation = context.getController().getWarp(warpKey);
        if (targetLocation == null) {
            context.getLogger().warning("Unknown warp: " + warpKey);
            return SpellResult.NO_TARGET;
        }

        return teleport(context, entity, targetLocation);
    }

    @Override
    public boolean isUndoable() {
        return true;
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }

    @Override
    public boolean requiresTargetEntity() {
        return true;
    }
}
