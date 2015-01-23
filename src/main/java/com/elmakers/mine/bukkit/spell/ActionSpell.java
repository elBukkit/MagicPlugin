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
        ActionHandler downHandler = actions.get("alternate_down");
        if (downHandler != null && isLookingDown())
        {
            return SpellResult.ALTERNATE_DOWN.max(downHandler.perform(parameters));
        }
        ActionHandler upHandler = actions.get("alternate_up");
        if (upHandler != null && isLookingUp())
        {
            return SpellResult.ALTERNATE_UP.max(upHandler.perform(parameters));
        }
        ActionHandler sneakHandler = actions.get("alternate_sneak");
        if (sneakHandler != null && mage.isSneaking())
        {
            return SpellResult.ALTERNATE_SNEAK.max(sneakHandler.perform(parameters));
        }

        ActionHandler castHandler = actions.get("cast");
        if (castHandler != null)
        {
            return castHandler.perform(parameters);
        }

        // Allow for effect-only spells
        return SpellResult.CAST;
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

    @Override
    public void getParameters(Collection<String> parameters) {
        super.getParameters(parameters);
        for (ActionHandler handler : actions.values()) {
            handler.getParameterNames(parameters);
        }
    }

    @Override
    public void getParameterOptions(Collection<String> examples, String parameterKey) {
        super.getParameterOptions(examples, parameterKey);
        for (ActionHandler handler : actions.values()) {
            handler.getParameterOptions(examples, parameterKey);
        }
    }

    @Override
    public String getMessage(String messageKey, String def) {
        String message = super.getMessage(messageKey, def);
        for (ActionHandler handler : actions.values()) {
            message = handler.transformMessage(message);
        }
        return message;
    }
}
