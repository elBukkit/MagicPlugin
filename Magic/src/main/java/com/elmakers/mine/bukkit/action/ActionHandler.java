package com.elmakers.mine.bukkit.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import javax.annotation.Nullable;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.action.SpellAction;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.batch.ActionBatch;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class ActionHandler implements com.elmakers.mine.bukkit.api.action.ActionHandler, Cloneable
{
    private static Set<String> restrictedActions = new HashSet<>();
    private List<ActionContext> actions = new ArrayList<>();

    private boolean undoable = false;
    private boolean usesBrush = false;
    private boolean requiresBuildPermission = false;
    private boolean requiresBreakPermission = false;
    private @Nullable Integer currentAction = null;
    private boolean started = false;
    private static String debugIndent = "";

    public static void setRestrictedActions(Collection<String> actions) {
        restrictedActions.clear();
        restrictedActions.addAll(actions);
    }

    public ActionHandler()
    {

    }

    public ActionHandler(ActionHandler copy)
    {
        this.started = copy.started;
        this.undoable = copy.undoable;
        this.usesBrush = copy.usesBrush;
        this.requiresBuildPermission = copy.requiresBuildPermission;
        this.requiresBreakPermission = copy.requiresBreakPermission;
        this.currentAction = copy.currentAction;
        for (ActionContext context : copy.actions)
        {
            actions.add(context.clone());
        }
    }

    public void load(Spell spell, ConfigurationSection root, String key)
    {
        undoable = false;
        usesBrush = false;
        requiresBuildPermission = false;
        requiresBreakPermission = false;
        ConfigurationSection handlerConfiguration = (spell != null) ? spell.getHandlerParameters(key) : null;
        Collection<ConfigurationSection> actionNodes = ConfigurationUtils.getNodeList(root, key);

        if (actionNodes == null)
        {
            return;
        }

        for (ConfigurationSection actionConfiguration : actionNodes)
        {
            if (!actionConfiguration.contains("class"))
            {
                continue;
            }

            String actionClassName = actionConfiguration.getString("class");
            try
            {
                BaseSpellAction action = ActionFactory.construct(actionClassName);
                actionClassName = action.getClass().getSimpleName();
                if (restrictedActions.contains(actionClassName)) {
                    action = new RestrictedAction(ChatColor.RED + "The " + actionClassName + " action is not allowed here.");
                }
                actionConfiguration.set("class", null);
                if (handlerConfiguration != null) {
                    ConfigurationUtils.addConfigurations(actionConfiguration, handlerConfiguration, false);
                }
                if (actionConfiguration.getKeys(false).size() == 0) {
                    actionConfiguration = null;
                }
                loadAction(action, actionConfiguration);
            } catch (Exception ex) {
                spell.getController().getLogger().log(Level.WARNING, "Error loading class " + actionClassName + " for spell " + spell.getName(), ex);
            }
        }
    }

    public void initialize(Spell spell, ConfigurationSection baseParameters) {
        for (ActionContext action : actions) {
            action.initialize(spell, baseParameters);
            usesBrush = usesBrush || action.getAction().usesBrush();
            undoable = undoable || action.getAction().isUndoable();

            boolean actionRequiresBreakPermission = action.getAction().requiresBreakPermission();
            boolean actionRequiresBuildPermission = action.getAction().requiresBuildPermission();

            requiresBuildPermission = requiresBuildPermission || actionRequiresBuildPermission;
            requiresBreakPermission = requiresBreakPermission || actionRequiresBreakPermission;
        }
    }

    public void loadAction(SpellAction action) {
        loadAction(action, null);
    }

    public void loadAction(SpellAction action, ConfigurationSection actionParameters) {
        ActionContext actionContext = new ActionContext(action, actionParameters);
        actions.add(actionContext);
    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        for (ActionContext action : actions)
        {
            action.prepare(context, parameters);
        }
    }

    @Override
    public void processParameters(CastContext context, ConfigurationSection parameters)
    {
        for (ActionContext action : actions)
        {
            action.processParameters(context, parameters);
        }
    }

    public void reset(CastContext context)
    {
        started = false;
        if (actions.size() > 0) {
            this.currentAction = 0;
        } else {
            this.currentAction = null;
        }
    }

    public SpellResult start(CastContext context, ConfigurationSection parameters)
    {
        prepare(context, parameters);
        reset(context);
        SpellResult handlerResult = perform(context);
        if (handlerResult == SpellResult.PENDING)
        {
            ActionBatch batch = new ActionBatch(context, this);
            if (!context.getMage().addBatch(batch)) {
                handlerResult = SpellResult.FAIL;

                finish(context);
                context.finish();
            }
        }
        else
        {
            finish(context);
            context.finish();
        }
        return handlerResult;
    }

    @Override
    public SpellResult perform(CastContext context)
    {
        Location targetLocation = context.getTargetLocation();
        Entity targetEntity = context.getTargetEntity();

        SpellResult result = SpellResult.NO_ACTION;
        if (actions == null || actions.size() == 0)
        {
            return result;
        }
        Mage mage = context.getMage();
        boolean showDebug = mage.getDebugLevel() > 2;
        if (showDebug) {
            debugIndent += "  ";
        }
        boolean isPending = false;
        while (currentAction != null)
        {
            ActionContext action = actions.get(currentAction);
            if (!started) {
                started = true;
                action.getAction().reset(context);
            } else {
                // Update as these may have changed
                targetLocation = context.getTargetLocation();
                targetEntity = context.getTargetEntity();
            }
            if (action.getAction().requiresTargetEntity() && targetEntity == null) {
                if (showDebug) {
                    mage.sendDebugMessage(ChatColor.GRAY + debugIndent + "Action " + ChatColor.GOLD + action.getAction().getClass().getSimpleName() + ChatColor.WHITE  + ": " + ChatColor.GRAY + "Skipped, requires entity target", 17);
                }
                result = result.min(SpellResult.NO_TARGET);
                advance(context);
                continue;
            }
            if (action.getAction().requiresTarget() && targetLocation == null) {
                if (showDebug) {
                    mage.sendDebugMessage(ChatColor.GRAY + debugIndent + "Action " + ChatColor.GOLD + action.getAction().getClass().getSimpleName() + ChatColor.WHITE  + ": " + ChatColor.GRAY + "Skipped, requires target", 17);
                }
                result = result.min(SpellResult.NO_TARGET);
                advance(context);
                continue;
            }
            SpellResult actionResult = action.perform(context);
            context.addWork(1);
            if (actionResult == SpellResult.PENDING) {
                isPending = true;
            } else {
                result = result.min(actionResult);
                if (actionResult.isStop()) {
                    if (showDebug) {
                        mage.sendDebugMessage(ChatColor.RED + debugIndent + "Action " + ChatColor.GOLD + action.getAction().getClass().getSimpleName() + ChatColor.WHITE  + ": " + ChatColor.AQUA + actionResult.name().toLowerCase(), 15);
                    }
                    cancel(context);
                }
            }
            if (actionResult.isStop()) {
                break;
            }
            if (showDebug) {
                mage.sendDebugMessage(ChatColor.WHITE + debugIndent + "Action " + ChatColor.GOLD + action.getAction().getClass().getSimpleName() + ChatColor.WHITE  + ": " + ChatColor.AQUA + actionResult.name().toLowerCase(), 15);
            }

            advance(context);
            if (context.getWorkAllowed() <= 0) {
                isPending = true;
                break;
            }
        }
        if (showDebug) {
            debugIndent = debugIndent.substring(0, debugIndent.length() - 2);
        }

        SpellResult currentResult = context.getResult();
        context.addResult(result);
        SpellResult contextResult = context.processHandlers();
        if (contextResult == SpellResult.PENDING || context.getWorkAllowed() <= 0) {
            isPending = true;
        } else {
            context.addResult(contextResult);
        }
        SpellResult newResult = context.getResult();
        if (showDebug && newResult != currentResult) {
            mage.sendDebugMessage(ChatColor.AQUA + debugIndent + "Result changed from "
                    + ChatColor.DARK_AQUA + currentResult.name().toLowerCase() + ChatColor.WHITE
                    + " to " + ChatColor.AQUA + newResult.name().toLowerCase(), 12);
        }

        return isPending ? SpellResult.PENDING : newResult;
    }

    protected void advance(CastContext context) {
        context.performedActions(1);
        if (currentAction != null) {
            currentAction++;
            if (currentAction >= actions.size()) {
                currentAction = null;
            } else {
                actions.get(currentAction).getAction().reset(context);
            }
        }
    }

    public void cancel(CastContext context)
    {
        if (currentAction != null && currentAction < actions.size())
        {
            context.performedActions(actions.size() - currentAction);
        }
        currentAction = null;
    }

    @Override
    public void finish(CastContext context)
    {
        for (ActionContext action : actions)
        {
            action.finish(context);
        }
    }

    public boolean isUndoable()
    {
        return undoable;
    }

    public boolean usesBrush()
    {
        return usesBrush;
    }

    public boolean requiresBuildPermission() {
        return requiresBuildPermission;
    }

    public boolean requiresBreakPermission() {
        return requiresBreakPermission;
    }

    public void getParameterNames(Spell spell, Collection<String> parameters)
    {
        for (ActionContext context : actions)
        {
            context.getAction().getParameterNames(spell, parameters);
        }
    }

    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples)
    {
        for (ActionContext context : actions)
        {
            context.getAction().getParameterOptions(spell, parameterKey, examples);
        }
    }

    public String transformMessage(String message)
    {
        for (ActionContext context : actions)
        {
            message = context.getAction().transformMessage(message);
        }
        return message;
    }

    @Override
    public String toString()
    {
        return "ActionHandler [" + actions.size() + "]";
    }

    public int getActionCount() {
        int count = 0;
        for (ActionContext context : actions)
        {
            count += context.getAction().getActionCount();
        }
        return count;
    }

    @Override
    public Object clone()
    {
        return new ActionHandler(this);
    }

    public boolean isFinished() {
        return currentAction == null;
    }

    @Override
    public int size() {
        return actions.size();
    }

    public List<ActionContext> getActions() {
        return actions;
    }
}
