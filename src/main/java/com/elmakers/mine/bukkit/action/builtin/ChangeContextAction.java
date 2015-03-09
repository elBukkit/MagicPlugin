package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class ChangeContextAction extends CompoundAction {
    private Vector sourceOffset;
    private Vector targetOffset;
    private boolean targetSelf;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        targetSelf = parameters.getBoolean("target_caster");
        targetOffset = ConfigurationUtils.getVector(parameters, "target_offset");
        sourceOffset = ConfigurationUtils.getVector(parameters, "source_offset");
    }

    @Override
    public SpellResult perform(CastContext context) {
        Entity sourceEntity = context.getEntity();
        Location sourceLocation = context.getLocation();
        Entity targetEntity = context.getEntity();
        Location targetLocation = context.getLocation();
        if (sourceLocation == null)
        {
            return SpellResult.LOCATION_REQUIRED;
        }
        if (targetSelf)
        {
            targetEntity = sourceEntity;
        }
        if (sourceOffset != null)
        {
            sourceLocation = sourceLocation.clone().add(sourceOffset);
        }
        if (targetOffset != null)
        {
            targetLocation = targetLocation.clone().add(targetOffset);
        }
        CastContext newContext = createContext(context, sourceEntity, sourceLocation, targetEntity, targetLocation);
        return performActions(newContext);
    }
}
