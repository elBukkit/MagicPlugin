package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class LightAction extends BaseSpellAction {
    private int level;
    private boolean async;
    private boolean update;
    private boolean forceUndo;
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
                boolean removed = controller.deleteLight(location, async);
                if (update && removed) {
                    controller.updateLight(location, false);
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
        forceUndo = parameters.getBoolean("undo_previous", false);
    }

    @Override
    public SpellResult perform(CastContext context)
    {
        if (!context.getController().isLightingAvailable()) {
            return SpellResult.FAIL;
        }
        Location location = context.getTargetLocation();
        context.addWork(5);
        if (controller.createLight(location, level, async) && update) {
            controller.updateLight(location);
            context.addWork(10);
        }
        if (forceUndo) {
            context.getMage().undoScheduled(context.getSpell().getSpellKey().getBaseKey());
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
