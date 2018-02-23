package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

public class LightAction extends BaseSpellAction {
    private int level;
    private boolean async;
    private boolean update;
    private Location location;
    private MageController controller;
    
    private class LightUndoAction implements Runnable
    {
        @Override
        public void run() {
            if (location != null) {
                controller.deleteLight(location, async);
                if (update) {
                    controller.updateLight(location);
                }
                location = null;
            }
        }
    }
    
    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        controller = context.getController();
        async = parameters.getBoolean("async", true);
        update = parameters.getBoolean("update", true);
        level = parameters.getInt("level", 15);
    }

    @Override
    public SpellResult perform(CastContext context)
    {
        location = context.getTargetLocation();
        context.addWork(5);
        if (!controller.createLight(location, level, async)) {
            return SpellResult.FAIL;
        }
        if (update) {
            controller.updateLight(location);
            context.addWork(10);
        }
        return SpellResult.CAST;
    }

    @Override
    public boolean isUndoable()
    {
        return true;
    }

    @Override
    public boolean requiresTarget()
    {
        return true;
    }
}
