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
    private MageController controller;
    
    private class LightUndoAction implements Runnable
    {
        private final Location location;
        private final boolean update;
        
        public LightUndoAction(Location location, boolean update) {
            this.location = location;
            this.update = update;
        }
        
        @Override
        public void run() {
            if (location != null) {
                controller.deleteLight(location, async);
                if (update) {
                    controller.updateLight(location);
                }
            }
        }
    }
    
    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        controller = context.getController();
        async = parameters.getBoolean("async", false);
        update = parameters.getBoolean("update", true);
        level = parameters.getInt("level", 15);
    }

    @Override
    public SpellResult perform(CastContext context)
    {
        Location location = context.getTargetLocation();
        context.addWork(5);
        if (!controller.createLight(location, level, async)) {
            return SpellResult.FAIL;
        }
        if (update) {
            controller.updateLight(location);
            context.addWork(10);
        }
        context.registerForUndo(new LightUndoAction(location, update));
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
