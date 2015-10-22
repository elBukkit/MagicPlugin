package com.elmakers.mine.bukkit.action;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.action.SpellAction;
import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.batch.ActionBatch;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActionHandler implements Cloneable
{
    private static final String ACTION_BUILTIN_CLASSPATH = "com.elmakers.mine.bukkit.action.builtin";
    private static Map<String, Class<?>> actionClasses = new HashMap<String, Class<?>>();

    private List<ActionContext> actions = new ArrayList<ActionContext>();

    private boolean undoable = false;
    private boolean usesBrush = false;
    private boolean requiresBuildPermission = false;
    private boolean requiresBreakPermission = false;
    private boolean isConditionalOnSuccess = false;
    private boolean isConditionalOnFailure = false;
    private Integer currentAction = null;
    private boolean started = false;
    private static String debugIndent = "";

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
        this.isConditionalOnSuccess = copy.isConditionalOnSuccess;
        this.isConditionalOnFailure = copy.isConditionalOnFailure;
        this.currentAction = copy.currentAction;
        for (ActionContext context : copy.actions)
        {
            actions.add((ActionContext)context.clone());
        }
    }

    public void load(ConfigurationSection root, String key)
    {
        undoable = false;
        usesBrush = false;
        requiresBuildPermission = false;
        requiresBreakPermission = false;
        String conditionalTest = root.getString("conditional");
        if (conditionalTest != null && !conditionalTest.isEmpty()) {
            if (conditionalTest.equalsIgnoreCase("success")) {
                isConditionalOnSuccess = true;
            } else {

                isConditionalOnFailure = true;
            }
        } else {
            isConditionalOnSuccess = false;
            isConditionalOnFailure = false;
        }
        Collection<ConfigurationSection> actionNodes = ConfigurationUtils.getNodeList(root, key);

        if (actionNodes != null)
        {
            for (ConfigurationSection actionConfiguration : actionNodes)
            {
                if (actionConfiguration.contains("class"))
                {
                    String actionClassName = actionConfiguration.getString("class");
                    try
                    {
                        if (!actionClassName.contains("."))
                        {
                            actionClassName = ACTION_BUILTIN_CLASSPATH + "." + actionClassName;
                        }
                        Class<?> genericClass = actionClasses.get(actionClassName);
                        if (genericClass == null) {
                            try {
                                genericClass = Class.forName(actionClassName + "Action");
                            } catch (Exception ex) {
                                genericClass = Class.forName(actionClassName);
                            }

                            if (!BaseSpellAction.class.isAssignableFrom(genericClass)) {
                                throw new Exception("Must extend SpellAction");
                            }
                            actionClasses.put(actionClassName, genericClass);
                        }

                        @SuppressWarnings("unchecked")
                        Class<? extends BaseSpellAction> actionClass = (Class<? extends BaseSpellAction>)genericClass;
                        BaseSpellAction action = actionClass.newInstance();
                        actionConfiguration.set("class", null);
                        if (actionConfiguration.getKeys(false).size() == 0) {
                            actionConfiguration = null;
                        }
                        loadAction(action, actionConfiguration);
                    } catch (Exception ex) {
                        Bukkit.getLogger().warning("Error loading class " + actionClassName + ": " + ex.getMessage());
                    }
                }
            }
        }
    }

    public void initialize(Spell spell, ConfigurationSection baseParameters) {
        MaterialBrush brush = spell.getBrush();
        for (ActionContext action : actions) {
            action.initialize(spell, baseParameters);
            usesBrush = usesBrush || action.getAction().usesBrush();
            undoable = undoable || action.getAction().isUndoable();

            boolean actionRequiresBreakPermission = action.getAction().requiresBreakPermission();
            boolean actionRequiresBuildPermission = action.getAction().requiresBuildPermission();
            if (usesBrush && brush != null && brush.isErase() && actionRequiresBuildPermission && !actionRequiresBreakPermission)
            {
                actionRequiresBreakPermission = true;
                actionRequiresBuildPermission = false;
            }

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

    public void loadData(Mage mage, ConfigurationSection data)
    {
        for (ActionContext action : actions)
        {
            action.getAction().load(mage, data);
        }
    }

    public void saveData(Mage mage, ConfigurationSection data)
    {
        for (ActionContext action : actions)
        {
            action.getAction().save(mage, data);
        }
    }

    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        for (ActionContext action : actions)
        {
            action.prepare(context, parameters);
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
            }
        }
        else
        {
            finish(context);
            context.finish();
        }
        return handlerResult;
    }

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
        while (currentAction != null)
        {
            ActionContext action = actions.get(currentAction);
            if (!started) {
                started = true;
                action.getAction().reset(context);
            }
            if (action.getAction().requiresTargetEntity() && targetEntity == null) {
                if (showDebug) {
                    mage.sendDebugMessage(ChatColor.GRAY + debugIndent + "Action " + ChatColor.GOLD + action.getAction().getClass().getSimpleName() + ChatColor.WHITE  + ": " + ChatColor.GRAY + "Skipped, requires entity target");
                }
                result = result.min(SpellResult.NO_TARGET);
                advance(context);
                continue;
            }
            if (action.getAction().requiresTarget() && targetLocation == null) {
                if (showDebug) {
                    mage.sendDebugMessage(ChatColor.GRAY + debugIndent + "Action " + ChatColor.GOLD + action.getAction().getClass().getSimpleName() + ChatColor.WHITE  + ": " + ChatColor.GRAY + "Skipped, requires target");
                }
                result = result.min(SpellResult.NO_TARGET);
                advance(context);
                continue;
            }
            SpellResult actionResult = action.perform(context);
            context.addWork(1);
            result = result.min(actionResult);
            if (actionResult == SpellResult.CANCELLED) {
                if (showDebug) {
                    mage.sendDebugMessage(ChatColor.RED + debugIndent + "Action " + ChatColor.GOLD + action.getAction().getClass().getSimpleName() + ChatColor.WHITE  + ": " + ChatColor.AQUA + actionResult);
                }
                cancel(context);
            }
            if (actionResult.isStop()) {
                break;
            }
            if (showDebug) {
                mage.sendDebugMessage(ChatColor.WHITE + debugIndent + "Action " + ChatColor.GOLD + action.getAction().getClass().getSimpleName() + ChatColor.WHITE  + ": " + ChatColor.AQUA + actionResult);
            }
            if (isConditionalOnSuccess && actionResult.isSuccess()) {
                cancel(context);
                break;
            }
            if (isConditionalOnFailure && !actionResult.isSuccess()) {
                cancel(context);
                break;
            }

            advance(context);
            if (context.getWorkAllowed() <= 0) {
                result = SpellResult.PENDING;
                break;
            }
        }
        if (showDebug) {
            debugIndent = debugIndent.substring(0, debugIndent.length() - 2);
        }

        SpellResult currentResult = context.getResult();
        context.addResult(result);
        SpellResult newResult = context.getResult();
        if (showDebug && newResult != currentResult) {
            mage.sendDebugMessage(ChatColor.AQUA + debugIndent + "Result changed from " + ChatColor.DARK_AQUA + currentResult.name() + ChatColor.WHITE  + " to " + ChatColor.AQUA + newResult.name());
        }

        return result;
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

    public static void setActions(Entity entity, ActionHandler actions, CastContext context, ConfigurationSection parameters, String messageKey)
    {
        if (context == null || entity == null) return;

        if (actions != null)
        {
            ActionHandlerContext handler = new ActionHandlerContext(actions, context, parameters, messageKey);
            entity.setMetadata("actions", new FixedMetadataValue(context.getController().getPlugin(), handler));
        }
    }

    public static boolean hasActions(Entity entity)
    {
        return entity != null && entity.hasMetadata("actions");
    }

    public static void runActions(Entity entity, Location targetLocation, Entity targetEntity)
    {
        if (!hasActions(entity)) return;

        ActionHandlerContext actions = null;
        for (MetadataValue metadata : entity.getMetadata("actions"))
        {
            Object meta = metadata.value();
            if (meta instanceof ActionHandlerContext)
            {
                actions = (ActionHandlerContext)meta;
                break;
            }
        }

        if (actions == null) {
            return;
        }

        actions.perform(entity, entity.getLocation(), targetEntity, targetLocation);
        entity.removeMetadata("actions", actions.getContext().getController().getPlugin());
    }

    public static boolean hasEffects(Entity entity)
    {
        return entity != null && entity.hasMetadata("effects");
    }

    public static void runEffects(Entity entity)
    {
        if (!hasEffects(entity)) return;

        EffectContext effects = null;
        for (MetadataValue metadata : entity.getMetadata("effects"))
        {
            Object value = metadata.value();
            if (value instanceof EffectContext)
            {
                effects = (EffectContext)value;
                break;
            }
        }
        if (effects == null) return;

        effects.perform();
        entity.removeMetadata("effects", effects.getContext().getController().getPlugin());
    }

    public static void targetEffects(Entity entity, Entity targetEntity)
    {
        if (!hasEffects(entity)) return;

        EffectContext effects = null;
        for (MetadataValue metadata : entity.getMetadata("effects"))
        {
            Object value = metadata.value();
            if (value instanceof EffectContext)
            {
                effects = (EffectContext)value;
                break;
            }
        }
        if (effects == null) return;

        effects.getContext().setTargetEntity(targetEntity);
        entity.setMetadata("effects", new FixedMetadataValue(effects.getContext().getController().getPlugin(), effects));
    }

    public static void setEffects(Entity entity, CastContext context, String key)
    {
        if (key != null && context != null && entity != null)
        {
            EffectContext effects = new EffectContext(key, context, entity);
            entity.setMetadata("effects", new FixedMetadataValue(context.getController().getPlugin(), effects));
        }
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

    public int size() {
        return actions.size();
    }

    public List<ActionContext> getActions() {
        return actions;
    }
}
