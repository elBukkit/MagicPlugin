package com.elmakers.mine.bukkit.action.builtin;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.action.CompoundEntityAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.spell.TargetingSpell;

public class AllEntitiesAction extends CompoundEntityAction
{
    private boolean targetAllWorlds;

    @Override
    public void processParameters(CastContext context, ConfigurationSection parameters) {
        super.processParameters(context, parameters);
        targetAllWorlds = parameters.getBoolean("target_all_worlds", false);
    }

    @Override
    public SpellResult perform(CastContext context)
    {
        Location sourceLocation = context.getLocation();
        if (sourceLocation == null && !targetAllWorlds)
        {
            return SpellResult.LOCATION_REQUIRED;
        }

        return super.perform(context);
    }

    @Override
    public void addEntities(CastContext context, List<WeakReference<Entity>> entities)
    {
        Spell spell = context.getSpell();
        Location sourceLocation = context.getLocation();

        if (sourceLocation == null && !targetAllWorlds)
        {
            return;
        }

        Class<?> targetType = Player.class;
        if (spell instanceof TargetingSpell)
        {
            targetType = ((TargetingSpell)spell).getTargetEntityType();
        }

        if (targetType == Player.class)
        {
            Collection<? extends Player> players = context.getPlugin().getServer().getOnlinePlayers();
            for (Player player : players)
            {
                if ((targetAllWorlds || (sourceLocation != null && sourceLocation.getWorld().equals(player.getWorld()))) && context.canTarget(player))
                {
                    entities.add(new WeakReference<Entity>(player));
                }
            }
        }
        else
        {
            List<World> worlds;
            if (targetAllWorlds) {
                worlds = Bukkit.getWorlds();
            } else {
                worlds = new ArrayList<>();
                worlds.add(sourceLocation.getWorld());
            }
            for (World world : worlds)
            {
                List<Entity> candidates = world.getEntities();
                for (Entity entity : candidates)
                {
                    if (context.canTarget(entity))
                    {
                        entities.add(new WeakReference<>(entity));
                    }
                }
            }
        }
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("target_all_worlds");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("target_all_worlds")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_BOOLEANS));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
