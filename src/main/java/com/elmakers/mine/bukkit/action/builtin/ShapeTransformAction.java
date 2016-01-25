package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.math.VectorTransform;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class ShapeTransformAction extends CompoundAction {
    private VectorTransform blockPositionTransform;
    private boolean updateLocation;
    private Location location;
    private int totalSteps;
    private int maxSteps;
    
    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        if (parameters.isConfigurationSection("position_transform")) {
            blockPositionTransform = new VectorTransform(ConfigurationUtils.getConfigurationSection(parameters, "position_transform"));
        } else {
            blockPositionTransform = null;
        }
        
        updateLocation = parameters.getBoolean("update_location", false);;
        maxSteps = parameters.getInt("max_steps", 1);
        location = context.getTargetLocation();
        
    }
    
    @Override
    public void reset(CastContext context) {
        super.reset(context);
        totalSteps = 0;
        location = null;
    }
    
    @Override
    public SpellResult step(CastContext context) {
        if (totalSteps >= maxSteps) {
            return SpellResult.NO_TARGET;
        }
        
        if (updateLocation) {
            location = context.getTargetLocation();
        }
        
        World world = context.getWorld();
        Vector blockPosition = blockPositionTransform.get(location, totalSteps);
        actionContext.setTargetLocation(blockPosition.toLocation(world));
        
        context.playEffects("iterate");
        totalSteps++;
        return startActions();
    }
    
}
