package com.elmakers.mine.bukkit.spell;

import com.elmakers.mine.bukkit.action.ActionHandler;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
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
    private boolean requiresBreakPermission = false;
    private ConfigurationSection upParameters = null;
    private ConfigurationSection downParameters = null;
    private ConfigurationSection sneakParameters = null;
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
    protected boolean isLegacy() {
        return false;
    }

    @Override
    protected boolean isBatched() {
        return true;
    }

    @Override
    public SpellResult onCast(ConfigurationSection parameters)
    {
        currentCast.setWorkAllowed(workThreshold);
        SpellResult result = SpellResult.CAST;
        currentHandler = actions.get("cast");
        ConfigurationSection altParameters = null;
        ActionHandler downHandler = actions.get("alternate_down");
        ActionHandler upHandler = actions.get("alternate_up");
        ActionHandler sneakHandler = actions.get("alternate_sneak");
        workThreshold = parameters.getInt("work_threshold", 500);
        if ((downHandler != null || downParameters != null) && isLookingDown())
        {
            result = SpellResult.ALTERNATE_DOWN;
            if (downHandler != null) {
                currentHandler = downHandler;
            }
            altParameters = downParameters;
        }
        else if ((upHandler != null || upParameters != null) && isLookingUp())
        {
            result = SpellResult.ALTERNATE_UP;
            if (upHandler != null) {
                currentHandler = upHandler;
            }
            altParameters = upParameters;
        }
        else if ((sneakHandler != null || sneakParameters != null) && mage.isSneaking())
        {
            result = SpellResult.ALTERNATE_SNEAK;
            if (sneakHandler != null) {
                currentHandler = sneakHandler;
            }
            altParameters = sneakParameters;
        }

        if (isUndoable())
        {
            getMage().prepareForUndo(getUndoList());
        }

        target();
        if (currentHandler != null)
        {
            if (altParameters != null) {
                if (parameters == null) {
                    parameters = altParameters;
                } else {
                    parameters = ConfigurationUtils.addConfigurations(parameters, altParameters);
                }
            }
            try {
                result = result.max(currentHandler.start(currentCast, parameters));
                currentCast.setInitialResult(result);
            } catch (Exception ex) {
                controller.getLogger().log(Level.WARNING, "Spell cast failed for " + getKey(), ex);
                result = SpellResult.FAIL;
                try {
                    currentHandler.finish(currentCast);
                } catch (Exception finishException) {
                    controller.getLogger().log(Level.WARNING, "Failed to clean up failed spell " + getKey(), finishException);
                }
            }
        }
        return result;
    }

    @Override
    public void onLoad(ConfigurationSection data)
    {
        super.onLoad(data);
        for (ActionHandler handler : actions.values())
        {
            handler.loadData(getMage(), data);
        }
    }

    @Override
    public void onSave(ConfigurationSection data)
    {
        super.onSave(data);
        for (ActionHandler handler : actions.values())
        {
            handler.saveData(getMage(), data);
        }
    }

    @Override
    protected void loadTemplate(ConfigurationSection template)
    {
        castOnNoTarget = true;
        super.loadTemplate(template);

        usesBrush = false;
        undoable = false;
        requiresBuildPermission = false;
        requiresBreakPermission = false;
        upParameters = template.getConfigurationSection("alternate_up_parameters");
        downParameters = template.getConfigurationSection("alternate_down_parameters");
        sneakParameters = template.getConfigurationSection("alternate_sneak_parameters");
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
                    handler.initialize(this, parameters);
                    usesBrush = usesBrush || handler.usesBrush();
                    undoable = undoable || handler.isUndoable();
                    requiresBuildPermission = requiresBuildPermission || handler.requiresBuildPermission();
                    requiresBreakPermission = requiresBreakPermission || handler.requiresBreakPermission();
                    actions.put(actionKey, handler);
                }
            }
        }
        undoable = template.getBoolean("undoable", undoable);
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
            handler.getParameterNames(this, parameters);
        }
    }

    @Override
    public void getParameterOptions(Collection<String> examples, String parameterKey) {
        super.getParameterOptions(examples, parameterKey);
        for (ActionHandler handler : actions.values()) {
            handler.getParameterOptions(this, parameterKey, examples);
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

    @Override
    public boolean requiresBreakPermission() {
        return requiresBreakPermission;
    }

    @Override
    public com.elmakers.mine.bukkit.api.block.MaterialAndData getEffectMaterial()
    {
        if (!usesBrush) {
            return null;
        }
        return super.getEffectMaterial();
    }
}
