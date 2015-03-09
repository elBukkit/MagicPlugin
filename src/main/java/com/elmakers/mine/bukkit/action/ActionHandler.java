package com.elmakers.mine.bukkit.action;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.action.SpellAction;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ActionHandler
{
    private static final String ACTION_BUILTIN_CLASSPATH = "com.elmakers.mine.bukkit.action.builtin";

    private List<ActionContext> actions = new ArrayList<ActionContext>();

    private boolean undoable = false;
    private boolean usesBrush = false;
    private boolean requiresBuildPermission = false;
    private boolean isConditional = false;

    public void load(ConfigurationSection root, String key)
    {
        undoable = false;
        usesBrush = false;
        requiresBuildPermission = false;
        isConditional = root.getBoolean("conditional", false);
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
                        Class<?> genericClass = null;
                        try {
                            genericClass = Class.forName(actionClassName + "Action");
                        } catch (Exception ex) {
                            genericClass = Class.forName(actionClassName);
                        }
                        if (!BaseSpellAction.class.isAssignableFrom(genericClass)) {
                            throw new Exception("Must extend SpellAction");
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

    public void initialize(ConfigurationSection baseParameters) {
        for (ActionContext action : actions) {
            action.initialize(baseParameters);
            usesBrush = usesBrush || action.getAction().usesBrush();
            undoable = undoable || action.getAction().isUndoable();
            requiresBuildPermission = requiresBuildPermission || action.getAction().requiresBuildPermission();
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

    public SpellResult perform(Spell spell, ConfigurationSection parameters)
    {
        spell.target();
        return perform(spell.getCurrentCast(), parameters);
    }

    public SpellResult perform(CastContext context, ConfigurationSection parameters)
    {
        initialize(parameters);
        prepare(context, parameters);
        SpellResult result = perform(context);
        finish(context);

        return result;
    }

    public SpellResult perform(CastContext context)
    {
        Location targetLocation = context.getTargetLocation();
        Entity targetEntity = context.getTargetEntity();

        SpellResult result = SpellResult.NO_ACTION;
        for (ActionContext action : actions)
        {
            if (action.getAction().requiresTargetEntity() && targetEntity == null) {
                result = result.min(SpellResult.NO_TARGET);
                continue;
            }
            if (action.getAction().requiresTarget() && targetLocation == null) {
                result = result.min(SpellResult.NO_TARGET);
                continue;
            }
            SpellResult actionResult = action.perform(context);
            result = result.min(actionResult);
            if (isConditional && !actionResult.isSuccess()) {
                break;
            }
        }
        return result;
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

    public void getParameterNames(Collection<String> parameters)
    {
        for (ActionContext context : actions)
        {
            context.getAction().getParameterNames(parameters);
        }
    }

    public void getParameterOptions(Collection<String> examples, String parameterKey)
    {
        for (ActionContext context : actions)
        {
            context.getAction().getParameterOptions(examples, parameterKey);
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
}
