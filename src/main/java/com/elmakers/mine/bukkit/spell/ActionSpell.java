package com.elmakers.mine.bukkit.spell;

import com.elmakers.mine.bukkit.action.ActionHandler;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ActionSpell extends BrushSpell
{
    private Map<String, ActionHandler> actions = new HashMap<String, ActionHandler>();
    private boolean undoable = false;
    private boolean requiresBuildPermission = false;

    @Override
    public SpellResult onCast(ConfigurationSection parameters)
    {
        if (undoable)
        {
            registerForUndo();
        }
        SpellResult result = SpellResult.CAST;
        ActionHandler handler = actions.get("cast");
        ActionHandler downHandler = actions.get("alternate_down");
        ActionHandler upHandler = actions.get("alternate_up");
        ActionHandler sneakHandler = actions.get("alternate_sneak");
        if (downHandler != null && isLookingDown())
        {
            result = SpellResult.ALTERNATE_DOWN;
            handler = downHandler;
        }
        else if (upHandler != null && isLookingUp())
        {
            result = SpellResult.ALTERNATE_UP;
            handler = upHandler;
        }
        else if (sneakHandler != null && mage.isSneaking())
        {
            result = SpellResult.ALTERNATE_SNEAK;
            handler = sneakHandler;
        }

        if (handler != null)
        {
            SpellResult actionResult = handler.perform(this, parameters);
            result = result.max(actionResult);
            if (!result.isSuccess())
            {
                handler = actions.get(actionResult.name().toLowerCase());
                if (handler != null)
                {
                    handler.perform(getCurrentCast(), parameters);
                }
            }
        }

        // Allow for effect-only spells
        return SpellResult.CAST;
    }

    @Override
    public void load(ConfigurationSection data)
    {
        for (ActionHandler handler : actions.values())
        {
            handler.loadData(getMage(), data);
        }
    }

    @Override
    public void save(ConfigurationSection data)
    {
        for (ActionHandler handler : actions.values())
        {
            handler.saveData(getMage(), data);
        }
    }

    @Override
    protected void loadTemplate(ConfigurationSection template)
    {
        usesBrush = false;
        undoable = false;
        requiresBuildPermission = false;
        castOnNoTarget = true;
        if (template.contains("actions"))
        {
            ConfigurationSection parameters = template.getConfigurationSection("parameters");
            ConfigurationSection actionsNode = template.getConfigurationSection("actions");
            Collection<String> actionKeys = actionsNode.getKeys(false);
            for (String actionKey : actionKeys)
            {
                ActionHandler handler = new ActionHandler();
                handler.load(actionsNode, actionKey);
                handler.initialize(parameters);
                usesBrush = usesBrush || handler.usesBrush();
                undoable = undoable || handler.isUndoable();
                requiresBuildPermission = requiresBuildPermission || handler.requiresBuildPermission();
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

    @Override
    public boolean requiresBuildPermission() {
        return requiresBuildPermission;
    }
}
