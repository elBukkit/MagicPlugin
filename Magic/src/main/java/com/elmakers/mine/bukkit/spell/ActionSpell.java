package com.elmakers.mine.bukkit.spell;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.annotation.Nullable;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.ActionHandler;
import com.elmakers.mine.bukkit.api.batch.Batch;
import com.elmakers.mine.bukkit.api.batch.SpellBatch;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class ActionSpell extends BrushSpell
{
    private Map<String, ActionHandler> actions = new HashMap<>();
    private boolean undoable = false;
    private boolean requiresBuildPermission = false;
    private boolean requiresBreakPermission = false;
    private ActionHandler currentHandler = null;
    private Map<String, ConfigurationSection> handlerParameters = new HashMap<>();
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
    public boolean hasHandlerParameters(String handlerKey)
    {
        return handlerParameters.containsKey(handlerKey);
    }

    @Override
    public ConfigurationSection getHandlerParameters(String handlerKey)
    {
        return handlerParameters.get(handlerKey);
    }

    @Override
    public void processParameters(ConfigurationSection parameters) {
        ConfigurationSection alternateParameters = null;
        if (isLookingDown())
        {
            alternateParameters = getHandlerParameters("alternate_down");
        }
        else if (isLookingUp())
        {
            alternateParameters = getHandlerParameters("alternate_down");
        }
        else if (mage.isSneaking())
        {
            alternateParameters = getHandlerParameters("alternate_sneak");
        }
        else if (mage.isJumping())
        {
            alternateParameters = getHandlerParameters("alternate_jumping");
        }
        if (alternateParameters != null)
        {
            if (parameters == null)
            {
                parameters = alternateParameters;
            }
            else
            {
                parameters = ConfigurationUtils.addConfigurations(parameters, alternateParameters, true);
            }
        }

        super.processParameters(parameters);
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
        ActionHandler jumpHandler = actions.get("alternate_jumping");
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
        else if (jumpHandler != null && mage.isJumping())
        {
            result = SpellResult.ALTERNATE_JUMPING;
            currentHandler = jumpHandler;
        }

        if (isUndoable())
        {
            getMage().prepareForUndo(getUndoList());
        }

        target();
        playEffects("precast");
        if (currentHandler != null)
        {
            currentHandler = (ActionHandler)currentHandler.clone();
            try {
                result = result.max(currentHandler.start(currentCast, parameters));
                currentCast.setInitialResult(result);
            } catch (Exception ex) {
                controller.getLogger().log(Level.WARNING, "Spell cast failed for " + getKey(), ex);
                result = SpellResult.FAIL;
                try {
                    currentCast.setResult(result);
                    currentCast.setInitialResult(result);
                    currentHandler.finish(currentCast);
                    currentCast.finish();
                } catch (Exception finishException) {
                    controller.getLogger().log(Level.WARNING, "Failed to clean up failed spell " + getKey(), finishException);
                }
            }
        } else {
            currentCast.setResult(result);
            currentCast.setInitialResult(result);
            currentCast.finish();
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

        undoable = false;
        requiresBuildPermission = false;
        requiresBreakPermission = false;
        usesBrush = template.getBoolean("uses_brush", false);
        ConfigurationSection actionsNode = template.getConfigurationSection("actions");
        if (actionsNode != null)
        {
            ConfigurationSection parameters = template.getConfigurationSection("parameters");
            Object baseActions = actionsNode.get("cast");

            Collection<String> templateKeys = template.getKeys(false);
            for (String templateKey : templateKeys)
            {
                if (templateKey.endsWith("_parameters"))
                {
                    ConfigurationSection overrides = ConfigurationUtils.cloneConfiguration(template.getConfigurationSection(templateKey));
                    String handlerKey = templateKey.substring(0, templateKey.length() - 11);
                    handlerParameters.put(handlerKey, overrides);

                    // Auto-register base actions, kind of hacky to check for alternates though.
                    if (baseActions != null && !actionsNode.contains(handlerKey) && handlerKey.startsWith("alternate_"))
                    {
                        actionsNode.set(handlerKey, baseActions);
                    }
                }
            }

            actionsNode = ConfigurationUtils.replaceParameters(actionsNode, parameters);
            if (actionsNode != null)
            {
                Collection<String> actionKeys = actionsNode.getKeys(false);
                for (String actionKey : actionKeys)
                {
                    ActionHandler handler = new ActionHandler();
                    handler.load(this, actionsNode, actionKey);
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
        requiresBreakPermission = template.getBoolean("require_break", requiresBreakPermission);
        requiresBuildPermission = template.getBoolean("require_build", requiresBuildPermission);
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
        // If erase, requires break permission instead.
        return requiresBuildPermission && !brushIsErase();
    }

    @Override
    public boolean requiresBreakPermission() {
        return requiresBreakPermission || (requiresBuildPermission && brushIsErase());
    }

    @Nullable
    @Override
    public com.elmakers.mine.bukkit.api.block.MaterialAndData getEffectMaterial()
    {
        if (!usesBrush) {
            return null;
        }
        return super.getEffectMaterial();
    }

    @Override
    public boolean isActive()
    {
        if (mage == null) return false;
        if (toggle == ToggleType.UNDO && toggleUndo != null && !toggleUndo.isUndone()) {
            return true;
        }

        Collection<Batch> pendingBatches = mage.getPendingBatches();
        for (Batch batch : pendingBatches) {
            if (batch instanceof SpellBatch && ((SpellBatch)batch).getSpell() == this) {
                return true;
            }
        }
        return false;
    }
}
