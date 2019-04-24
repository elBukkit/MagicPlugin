package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.CheckAction;
import com.elmakers.mine.bukkit.api.action.CastContext;

public class CheckVelocityAction extends CheckAction {
    private double maxSpeed;
    private double minSpeed;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);

        maxSpeed = parameters.getDouble("max_speed", Double.NaN);
        minSpeed = parameters.getDouble("min_speed", Double.NaN);
    }

    @Override
    protected boolean isAllowed(CastContext context) {
        double speed = context.getMage().getVelocity().length();
        if (Double.isFinite(maxSpeed) && speed > maxSpeed) return false;
        if (Double.isFinite(minSpeed) && speed > minSpeed) return false;
        return true;
    }
}