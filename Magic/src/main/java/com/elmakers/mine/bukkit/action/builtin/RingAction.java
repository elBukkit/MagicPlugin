package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.action.CastContext;

public class RingAction extends DiscAction
{
    @Override
    public void processParameters(CastContext context, ConfigurationSection parameters) {
        super.processParameters(context, parameters);
        if (thickness == 0) {
            thickness = 1;
        }
    }
}
