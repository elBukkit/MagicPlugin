package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import org.bukkit.configuration.ConfigurationSection;

public class RetargetAction extends CompoundAction {
    private int range;
    private boolean useHitbox;
    private double fov;
    private double closeRange;
    private double closeFOV;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        useHitbox = parameters.getBoolean("hitbox", false);
        range = parameters.getInt("range", 32);
        fov = parameters.getDouble("fov", 0.3);
        closeRange = parameters.getDouble("close_range", 1);
        closeFOV = parameters.getDouble("close_fov", 0.5);
    }

    @Override
    public SpellResult perform(CastContext context) {
        context.addWork(range);
        actionContext.retarget(range, fov, closeRange, closeFOV, useHitbox);
        return performActions(actionContext);
    }
}
