package com.elmakers.mine.bukkit.action;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.action.SpellAction;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.google.common.base.Preconditions;

public abstract class CompoundAction extends BaseSpellAction
{
    private boolean usesBrush = false;
    private boolean undoable = false;
    private boolean requiresBuildPermission = false;
    private boolean requiresBreakPermission = false;
    private boolean stopOnSuccess = false;
    private boolean initialized = false;
    protected boolean pauseOnNext = false;
    protected @Nullable ConfigurationSection actionConfiguration;
    protected @Nullable CastContext actionContext;
    private @Nullable Object baseActions;

    protected Map<String, ActionHandler> handlers = new HashMap<>();
    protected Set<String> handlerKeys = new HashSet<>();
    protected Set<ActionHandler> ran = new HashSet<>();
    protected @Nullable String currentHandler = null;
    protected State state = State.NOT_STARTED;

    protected enum State {
        NOT_STARTED,
        STARTED,
        STEPPING
    }

    public SpellResult step(CastContext context) {
        return SpellResult.NO_ACTION;
    }

    public SpellResult start(CastContext context) {
        return SpellResult.NO_ACTION;
    }

    /**
     * This is called after every iteration, which is each time the step() method
     * returns something other than PENDING.
     * Return true here to continue iterating, false to break out of the loop.
     */
    public boolean next(CastContext context) {
        return false;
    }

    protected SpellResult startActions() {
        return startActions("actions");
    }

    protected SpellResult startActions(String handlerKey) {
        Preconditions.checkState(actionContext != null);
        currentHandler = handlerKey;
        ActionHandler handler = handlers.get(currentHandler);
        if (handler != null) {
            handler.reset(actionContext);
            ran.add(handler);
        } else {
            currentHandler = null;
        }

        return SpellResult.NO_ACTION;
    }

    public boolean hasActions() {
        return hasActions("actions");
    }

    public boolean hasActions(String key) {
        ActionHandler handler = handlers.get(key);
        return handler != null && handler.size() > 0;
    }

    @Override
    public SpellResult perform(CastContext context) {
        SpellResult result = SpellResult.NO_ACTION;
        while (!result.isStop()) {
            context.addWork(1);
            if (state == State.NOT_STARTED) {
                result = result.min(start(context));

                // Don't continue if the action failed to start
                if (result.isStop() || result.isFailure()) break;
                state = State.STARTED;
            }

            if (state == State.STARTED) {
                result = result.min(step(context));
                if (result.isStop()) break;
                state = State.STEPPING;
            }

            ActionHandler handler = currentHandler == null ? null : handlers.get(currentHandler);
            if (handler != null) {
                result = result.min(handler.perform(actionContext));
                if (result.isStop()) break;
                if (stopOnSuccess && result.isSuccess()) {
                    result = SpellResult.STOP;
                    break;
                }
            }

            if (!next(context)) {
                break;
            }

            result = result.min(step(context));

            // Prevent infinite loops of no actions
            if ((pauseOnNext || context.getWorkAllowed() <= 0) && !result.isStop()) {
                result = SpellResult.PENDING;
            }
        }
        return result;
    }

    @Override
    public void finish(CastContext context) {
        super.finish(context);
        for (ActionHandler handler : ran) {
            handler.finish(context);
        }
        ran.clear();
    }

    @Override
    public void reset(CastContext context) {
        super.reset(context);
        if (context != null) {
            actionContext = new com.elmakers.mine.bukkit.action.CastContext(context);
        }
        state = State.NOT_STARTED;
        currentHandler = null;
    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        for (ActionHandler handler : handlers.values()) {
            handler.prepare(context, context.getSpell().getWorkingParameters());
        }
        stopOnSuccess = parameters.getBoolean("stop_on_success", false);
    }

    @Override
    public void initialize(Spell spell, ConfigurationSection parameters)
    {
        super.initialize(spell, parameters);
        this.actionConfiguration = parameters;
        baseActions = parameters.get("actions");
        usesBrush = false;
        undoable = false;
        requiresBuildPermission = false;
        requiresBreakPermission = false;
        addHandlers(spell, parameters);
        for (ActionHandler handler : handlers.values()) {
            handler.initialize(spell, spell.getWorkingParameters());
            updateFlags(handler);
        }
        initialized = true;
    }

    protected void addHandlers(Spell spell, ConfigurationSection parameters) {
        addHandler(spell, "actions");
    }

    @Nullable
    protected ActionHandler getHandler(String handlerKey) {
        return handlers.get(handlerKey);
    }

    @Nullable
    protected ActionHandler addHandler(Spell spell, String handlerKey) {
        ActionHandler handler = handlers.get(handlerKey);
        handlerKeys.add(handlerKey);
        if (handler != null) {
            return handler;
        }
        if (actionConfiguration == null)
        {
            return null;
        }
        if (!actionConfiguration.contains(handlerKey))
        {
            // Create parameter-only configs automagically
            if (baseActions != null && spell.hasHandlerParameters(handlerKey))
            {
                actionConfiguration.set(handlerKey, baseActions);
            }
        }
        else if (actionConfiguration.isString(handlerKey))
        {
            // Support references
            actionConfiguration.set(handlerKey, actionConfiguration.get(actionConfiguration.getString(handlerKey)));
        }
        if (!actionConfiguration.isList(handlerKey))
        {
            return null;
        }
        handler = new ActionHandler();
        handler.load(spell, actionConfiguration, handlerKey);
        handlers.put(handlerKey, handler);
        // Need to initialize the new handler if this parent action has already been initialized
        if (initialized) {
            handler.initialize(spell, spell.getWorkingParameters());
            updateFlags(handler);
        }
        return handler;
    }

    protected void updateFlags(ActionHandler handler) {
        usesBrush = usesBrush || handler.usesBrush();
        undoable = undoable || handler.isUndoable();
        requiresBuildPermission = requiresBuildPermission || handler.requiresBuildPermission();
        requiresBreakPermission = requiresBreakPermission || handler.requiresBreakPermission();
    }

    // These are here for legacy spell support
    // via programmatic action building
    public void addAction(SpellAction action) {
        addAction(action, null);
    }

    public void addAction(SpellAction action, ConfigurationSection parameters) {
        ActionHandler actions = addHandler(null, "actions");

        if (actions == null) {
            return;
        }

        actions.loadAction(action, parameters);
    }

    @Override
    public boolean isUndoable()
    {
        return undoable;
    }

    @Override
    public boolean usesBrush()
    {
        return usesBrush;
    }

    @Override
    public boolean requiresBuildPermission()
    {
        return requiresBuildPermission;
    }

    @Override
    public boolean requiresBreakPermission()
    {
        return requiresBreakPermission;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters)
    {
        super.getParameterNames(spell, parameters);
        for (ActionHandler handler : handlers.values())
        {
            handler.getParameterNames(spell, parameters);
        }
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples)
    {
        super.getParameterOptions(spell, parameterKey, examples);
        for (ActionHandler handler : handlers.values())
        {
            handler.getParameterOptions(spell, parameterKey, examples);
        }
    }

    @Override
    public String transformMessage(String message)
    {
        for (ActionHandler handler : handlers.values())
        {
            message = handler.transformMessage(message);
        }
        return message;
    }

    public void createActionContext(CastContext context) {
        actionContext = new com.elmakers.mine.bukkit.action.CastContext(context);
    }

    public void createActionContext(CastContext context, Entity sourceEntity, Location sourceLocation) {
        actionContext = new com.elmakers.mine.bukkit.action.CastContext(context, sourceEntity, sourceLocation);
    }

    public void createActionContext(CastContext context, Entity sourceEntity, Location sourceLocation, Entity targetEntity, Location targetLocation) {
        actionContext = new com.elmakers.mine.bukkit.action.CastContext(context, sourceEntity, sourceLocation);
        actionContext.setTargetEntity(targetEntity);
        actionContext.setTargetLocation(targetLocation);
    }

    public void createActionContext(CastContext context, Mage sourceMage, Entity sourceEntity, Location sourceLocation, Entity targetEntity, Location targetLocation) {
        if (sourceMage != null) {
            actionContext = new com.elmakers.mine.bukkit.action.CastContext(context, sourceMage, sourceEntity, sourceLocation);
        } else {
            actionContext = new com.elmakers.mine.bukkit.action.CastContext(context, sourceEntity, sourceLocation);
        }
        actionContext.setTargetEntity(targetEntity);
        actionContext.setTargetLocation(targetLocation);
    }

    @Override
    public int getActionCount() {
        int actionCount = 0;
        for (ActionHandler handler : handlers.values())
        {
            actionCount += handler.getActionCount();
        }
        return actionCount;
    }

    protected void skippedActions(CastContext context) {
        skippedActions("actions", context);
    }

    protected void skippedActions(String key, CastContext context) {
        ActionHandler actions = handlers.get(key);
        if (actions == null) return;
        int actionCount = actions.getActionCount();
        context.performedActions(actionCount);
        context.addWork(actionCount);
    }

    @Override
    @Nullable
    public Object clone()
    {
        CompoundAction action = (CompoundAction)super.clone();
        if (action != null)
        {
            action.handlers = new HashMap<>();
            for (Map.Entry<String, ActionHandler> entry : handlers.entrySet())
            {
                action.handlers.put(entry.getKey(), (ActionHandler)entry.getValue().clone());
            }
        }
        return action;
    }

    @Nonnull
    public Collection<String> getAllHandlerKeys() {
        return handlerKeys;
    }
}
