package com.elmakers.mine.bukkit.spell;

import com.elmakers.mine.bukkit.api.action.GeneralAction;
import com.elmakers.mine.bukkit.api.action.BlockAction;
import com.elmakers.mine.bukkit.api.action.EntityAction;
import com.elmakers.mine.bukkit.api.action.SpellAction;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.MageSpell;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.Target;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ActionHandler
{
    private static final String ACTION_BUILTIN_CLASSPATH = "com.elmakers.mine.bukkit.action.builtin";

    private List<SpellAction> allActions = new ArrayList<SpellAction>();
    private List<GeneralAction> generalActions = new ArrayList<GeneralAction>();
    private List<BlockAction> blockActions = new ArrayList<BlockAction>();
    private List<EntityAction> entityActions = new ArrayList<EntityAction>();

    private final Spell spell;
    private boolean undoable = false;
    private boolean usesBrush = false;

    public ActionHandler(Spell spell)
    {
        this.spell = spell;
    }

    public void load(ConfigurationSection root, String key)
    {
        undoable = false;
        usesBrush = false;
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
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    public void loadAction(SpellAction action) {
        loadAction(action, new MemoryConfiguration());
    }

    public void loadAction(SpellAction action, ConfigurationSection parameters) {
        action.load(spell, parameters);
        allActions.add(action);
        usesBrush = usesBrush || action.usesBrush();
        undoable = undoable || action.isUndoable();
        if (action instanceof GeneralAction) {
            generalActions.add((GeneralAction)action);
        }
        if (action instanceof EntityAction) {
            entityActions.add((EntityAction)action);
        }
        if (action instanceof BlockAction) {
            blockActions.add((BlockAction)action);
        }
    }

    public SpellResult perform(ConfigurationSection parameters)
    {
        return perform(parameters, null);
    }

    public SpellResult perform(ConfigurationSection parameters, Location targetLocation)
    {
        SpellResult result = SpellResult.NO_ACTION;

        for (SpellAction action : allActions)
        {
            action.prepare(parameters);
        }

        for (GeneralAction generalAction : generalActions)
        {
            SpellResult actionResult = generalAction.perform(generalAction.getParameters(parameters));
            result = result.min(actionResult);
        }

        spell.target();
        if (targetLocation == null)
        {
            targetLocation = spell.getTargetLocation();
        }

        if (targetLocation != null)
        {
            for (BlockAction action : blockActions)
            {
                SpellResult actionResult = action.perform(action.getParameters(parameters), targetLocation.getBlock());
                result = result.min(actionResult);
            }
        }

        List<Entity> targetEntities = new ArrayList<Entity>();
        Entity targetedEntity = spell.getTargetEntity();
        if (targetedEntity != null)
        {
            targetEntities.add(targetedEntity);
        }

        int radius = parameters.getInt("target_radius", 0);
        int coneCount = parameters.getInt("target_count", 0);
        boolean targetSelf = parameters.getBoolean("target_self", false);
        boolean targetAllWorlds = parameters.getBoolean("target_all_worlds", false);
        Entity mageEntity = null;
        Location sourceLocation = spell.getLocation();
        if (spell instanceof MageSpell)
        {
            Mage mage = ((MageSpell)spell).getMage();
            mageEntity = mage.getEntity();
            radius = (int)(mage.getRadiusMultiplier() * radius);
        }

        if (parameters.getBoolean("target_all"))
        {
            Class<?> targetType = Player.class;
            if (spell instanceof TargetingSpell)
            {
                targetType = ((TargetingSpell)spell).targetEntityType;
            }
            if (targetType == Player.class)
            {
                Player[] players = Bukkit.getOnlinePlayers();
                for (Player player : players)
                {
                    if ((targetSelf || player != mageEntity) && (targetAllWorlds || (sourceLocation != null && sourceLocation.getWorld().equals(player.getWorld()))) && spell.canTarget(player))
                    {
                        targetEntities.add(player);
                    }
                }
            }
            else if (sourceLocation != null)
            {
                List<World> worlds = null;
                if (targetAllWorlds) {
                    worlds = Bukkit.getWorlds();
                } else {
                    worlds = new ArrayList<World>();
                    worlds.add(sourceLocation.getWorld());
                }
                for (World world : worlds)
                {
                    List<Entity> entities = world.getEntities();
                    for (Entity entity : entities)
                    {
                        if (spell.canTarget(entity) && (targetSelf || entity != mageEntity))
                        {
                            targetEntities.add(entity);
                        }
                    }
                }
            }
        }
        else if (radius > 0 && targetLocation != null)
        {
            List<Entity> entities = CompatibilityUtils.getNearbyEntities(targetLocation, radius, radius, radius);
            for (Entity entity : entities)
            {
                if (entity != targetedEntity && (targetSelf || entity != mageEntity) && spell.canTarget(entity))
                {
                    targetEntities.add(entity);
                }
            }
        }
        else if (coneCount > 1 && spell instanceof TargetingSpell)
        {
            List<Target> entities = ((TargetingSpell)spell).getAllTargetEntities();
            for (int i = 1; i < coneCount && i < entities.size(); i++)
            {
                targetEntities.add(entities.get(i).getEntity());
            }
        }

        if (targetEntities.size() == 0)
        {
            result = SpellResult.NO_TARGET;
        }

        for (Entity entity : targetEntities)
        {
            for (EntityAction action : entityActions)
            {
                SpellResult actionResult = action.perform(action.getParameters(parameters), entity);
                if (actionResult.ordinal() < result.ordinal())
                {
                    result = actionResult;
                }
            }
        }

        for (SpellAction action : allActions)
        {
            action.finish(parameters);
        }

        return result;
    }

    public boolean isUndoable()
    {
        return undoable;
    }

    public boolean usesBrush()
    {
        return usesBrush;
    }

    public void getParameterNames(Collection<String> parameters)
    {
        for (SpellAction action : allActions)
        {
            action.getParameterNames(parameters);
        }
    }

    public void getParameterOptions(Collection<String> examples, String parameterKey)
    {
        for (SpellAction action : allActions)
        {
            action.getParameterOptions(examples, parameterKey);
        }
    }
}
