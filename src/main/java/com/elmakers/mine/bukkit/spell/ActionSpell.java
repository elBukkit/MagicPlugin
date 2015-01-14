package com.elmakers.mine.bukkit.spell;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ActionSpell extends BrushSpell
{
    private Map<String, ActionHandler> actions = new HashMap<String, ActionHandler>();
    private boolean undoable = false;

    @Override
    public SpellResult onCast(ConfigurationSection parameters)
    {
        if (undoable)
        {
            registerForUndo();
        }
        ActionHandler downHandler = actions.get("alt_down");
        if (downHandler != null && isLookingDown())
        {
            return downHandler.perform(parameters);
        }
        ActionHandler upHandler = actions.get("alt_up");
        if (upHandler != null && isLookingUp())
        {
            return upHandler.perform(parameters);
        }
        ActionHandler sneakHandler = actions.get("alt_sneak");
        if (sneakHandler != null && mage.isSneaking())
        {
            return sneakHandler.perform(parameters);
        }

        ActionHandler castHandler = actions.get("cast");
        if (castHandler != null)
        {
            return castHandler.perform(parameters);
        }

        return SpellResult.FAIL;
    }

    @Override
    protected void loadTemplate(ConfigurationSection template)
    {
        usesBrush = false;
        undoable = false;
        castOnNoTarget = true;
        if (template.contains("actions"))
        {
            ConfigurationSection actionsNode = template.getConfigurationSection("actions");
            Collection<String> actionKeys = actionsNode.getKeys(false);
            for (String actionKey : actionKeys)
            {
                ActionHandler handler = new ActionHandler(this);
                handler.load(actionsNode, actionKey);
                usesBrush = usesBrush || handler.usesBrush();
                undoable = undoable || handler.isUndoable();
                actions.put(actionKey, handler);
            }
        }
        undoable = template.getBoolean("undoable", undoable);
        super.loadTemplate(template);
    }

    @Override
    public boolean isUndoable()
    {
        return undoable;
    }

    public ActionHandler getActions(String key)
    {
        return actions.get(key);
    }
}
