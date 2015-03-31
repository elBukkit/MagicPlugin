package com.elmakers.mine.bukkit.spell;

import com.elmakers.mine.bukkit.action.ActionHandler;
import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.utility.Target;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class ActionSpell extends BrushSpell
{
    private Map<String, ActionHandler> actions = new HashMap<String, ActionHandler>();
    private boolean undoable = false;
    private boolean requiresBuildPermission = false;
    private ActionHandler currentHandler = null;
    private int workThreshold = 500;

    @Override
    protected void processResult(SpellResult result, ConfigurationSection castParameters) {
        if (!result.isSuccess())
        {
            ActionHandler handler = actions.get(result.name().toLowerCase());
            if (handler != null)
            {
                handler.start(currentCast, castParameters);
            }
        }
        super.processResult(result, castParameters);
    }

    @Override
    public SpellResult onCast(ConfigurationSection parameters)
    {
        currentCast.setWorkAllowed(workThreshold);
        SpellResult result = SpellResult.CAST;
        currentHandler = actions.get("cast");
        ActionHandler downHandler = actions.get("alternate_down");
        ActionHandler upHandler = actions.get("alternate_up");
        ActionHandler sneakHandler = actions.get("alternate_sneak");
        workThreshold = parameters.getInt("work_threshold", 500);
        if (downHandler != null && isLookingDown())
        {
            result = SpellResult.ALTERNATE_DOWN;
            currentHandler = downHandler;
        }
        else if (upHandler != null && isLookingUp())
        {
            result = SpellResult.ALTERNATE_UP;
            currentHandler = upHandler;
        }
        else if (sneakHandler != null && mage.isSneaking())
        {
            result = SpellResult.ALTERNATE_SNEAK;
            currentHandler = sneakHandler;
        }

        if (isUndoable())
        {
            getMage().prepareForUndo(getUndoList());
        }

        target();
        if (currentHandler != null)
        {
            try {
                result = result.max(currentHandler.start(currentCast, parameters));
            } catch (Exception ex) {
                controller.getLogger().log(Level.WARNING, "Spell cast failed", ex);
                result = SpellResult.FAIL;
                try {
                    currentHandler.finish(currentCast);
                } catch (Exception finishException) {
                    controller.getLogger().log(Level.WARNING, "Failed to clean up failed spell", finishException);
                }
            }
        }
        return result;
    }

    @Override
    public void load(ConfigurationSection data)
    {
        super.load(data);
        for (ActionHandler handler : actions.values())
        {
            handler.loadData(getMage(), data);
        }
    }

    @Override
    public void save(ConfigurationSection data)
    {
        super.save(data);
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
            if (actionsNode != null)
            {
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
        if (currentHandler != null) {
            message = currentHandler.transformMessage(message);
        }
        return message;
    }

    @Override
    public boolean requiresBuildPermission() {
        return requiresBuildPermission;
    }
}
