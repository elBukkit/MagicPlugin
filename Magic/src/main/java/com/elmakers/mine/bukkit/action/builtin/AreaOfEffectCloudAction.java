package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.api.action.CastContext;
import org.bukkit.configuration.ConfigurationSection;

public class AreaOfEffectCloudAction extends SpawnEntityAction
{
    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        parameters.set("type", "area_effect_cloud");
        super.prepare(context, parameters);
    }
}
