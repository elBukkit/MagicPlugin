package com.elmakers.mine.bukkit.spell;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.Target;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActionSpell extends UndoableSpell
{
    private static final String ACTION_BUILTIN_CLASSPATH = "com.elmakers.mine.bukkit.action.builtin";

    private Map<String, List<SpellAction>> actions = new HashMap<String, List<SpellAction>>();

    public MageController getController()
    {
        return controller;
    }

    @Override
    public SpellResult onCast(ConfigurationSection parameters)
    {
        List<SpellAction> downActions = actions.get("alt_down");
        if (downActions != null && isLookingDown())
        {
            return castActions(downActions, parameters);
        }
        List<SpellAction> upActions = actions.get("alt_up");
        if (upActions != null && isLookingUp())
        {
            return castActions(upActions, parameters);
        }
        List<SpellAction> sneakActions = actions.get("alt_sneak");
        if (sneakActions != null && mage.isSneaking())
        {
            return castActions(sneakActions, parameters);
        }

        return castActions(actions.get("cast"), parameters);
    }

    protected SpellResult castActions(List<SpellAction> actions, ConfigurationSection parameters) {
        if (actions == null) return SpellResult.FAIL;
        registerForUndo();
        SpellResult result = SpellResult.CAST;
        for (SpellAction action : actions)
        {
            SpellResult actionResult = action.perform(parameters);
            if (actionResult.ordinal() > result.ordinal())
            {
                result = actionResult;
            }
        }

        Target target = getTarget();
        if (!target.hasTarget())
        {
            return SpellResult.NO_TARGET;
        }

        List<Entity> targetEntities = new ArrayList<Entity>();

        Entity targetedEntity = target.getEntity();
        if (target.hasEntity())
        {
            targetEntities.add(targetedEntity);
        }

        int radius = parameters.getInt("radius", 0);
        int coneCount = parameters.getInt("count", 0);
        radius = (int)(mage.getRadiusMultiplier() * radius);

        if (radius > 0)
        {
            List<Entity> entities = CompatibilityUtils.getNearbyEntities(target.getLocation(), radius, radius, radius);
            for (Entity entity : entities)
            {
                if (entity != targetedEntity && entity != mage.getEntity() && canTarget(entity))
                {
                    targetEntities.add(entity);
                }
            }
        }
        else if (coneCount > 1)
        {
            List<Target> entities = getAllTargetEntities();
            for (int i = 1; i < coneCount; i++)
            {
                targetEntities.add(entities.get(i).getEntity());
            }
        }

        if (targetEntities.size() == 0)
        {
            return SpellResult.NO_TARGET;
        }

        for (Entity entity : targetEntities)
        {
            for (SpellAction action : actions)
            {
                SpellResult actionResult = action.perform(parameters, entity);
                if (actionResult.ordinal() > result.ordinal())
                {
                    result = actionResult;
                }
            }
        }
        return result;
    }

    @Override
    protected void loadTemplate(ConfigurationSection template)
    {
        super.loadTemplate(template);
        if (template.contains("actions"))
        {
            ConfigurationSection actionsNode = template.getConfigurationSection("actions");
            Collection<String> actionKeys = actionsNode.getKeys(false);
            for (String actionKey : actionKeys)
            {
                actions.put(actionKey, loadActions(actionsNode, actionKey));
            }
        }
    }

    public List<SpellAction> loadActions(ConfigurationSection root, String key)
    {
        List<SpellAction> actions = new ArrayList<SpellAction>();
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
                        Class<?> genericClass = Class.forName(actionClassName);
                        if (!SpellAction.class.isAssignableFrom(genericClass)) {
                            throw new Exception("Must extend SpellAction");
                        }

                        Class<? extends SpellAction> actionClass = (Class<? extends SpellAction>)genericClass;
                        SpellAction action = actionClass.newInstance();
                        action.load(this, actionConfiguration);
                        actions.add(action);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        controller.getLogger().info("Error creating action class: " + actionClassName + " " + ex.getMessage());
                    }
                }
            }
        }

        return actions;
    }
}
