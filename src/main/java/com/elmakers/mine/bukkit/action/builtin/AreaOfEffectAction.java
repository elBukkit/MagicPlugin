package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.CompoundEntityAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.Target;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class AreaOfEffectAction extends CompoundEntityAction
{
    private int radius;
    private int targetCount;
    private boolean targetSource;

    @Override
    public void reset(CastContext context) {
        super.reset(context);
        createActionContext(context, context.getTargetEntity(), context.getTargetLocation());
    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        radius = parameters.getInt("radius", 8);
        targetCount = parameters.getInt("target_count", -1);
        targetSource = parameters.getBoolean("target_source", true);

        Mage mage = context.getMage();
        if (mage != null)
        {
            radius = (int)(mage.getRadiusMultiplier() * radius);
        }

        super.prepare(context, parameters);
    }

    @Override
    public void addEntities(CastContext context, List<WeakReference<Entity>> entities)
    {
        Mage mage = context.getMage();
        Entity sourceEntity = context.getMage().getEntity();
        Location sourceLocation = context.getTargetLocation();
        if (mage.getDebugLevel() > 8)
        {
            mage.sendDebugMessage(ChatColor.GREEN + "AOE Targeting from " + ChatColor.GRAY + sourceLocation.getBlockX() +
                    ChatColor.DARK_GRAY + ","  + ChatColor.GRAY + sourceLocation.getBlockY() +
                    ChatColor.DARK_GRAY + "," + ChatColor.GRAY + sourceLocation.getBlockZ() +
                    ChatColor.DARK_GREEN + " with radius of " + ChatColor.GREEN + radius);
        }
        List<Entity> candidates = CompatibilityUtils.getNearbyEntities(sourceLocation, radius, radius, radius);
        if (targetCount > 0)
        {
            List<Target> targets = new ArrayList<Target>();
            Entity targetEntity = context.getTargetEntity();
            for (Entity entity : candidates)
            {
                if ((context.getTargetsCaster() || entity != sourceEntity) && (targetSource || entity != targetEntity) && context.canTarget(entity))
                {
                    Target target = new Target(sourceLocation, entity, radius, 0);
                    targets.add(target);
                    mage.sendDebugMessage(ChatColor.DARK_GREEN + "Target " + ChatColor.GREEN + entity.getType() + ChatColor.DARK_GREEN + ": " + ChatColor.YELLOW + target.getScore(), 6);
                }
                else if (mage.getDebugLevel() > 7)
                {
                    mage.sendDebugMessage(ChatColor.DARK_RED + "Skipped Target " + ChatColor.GREEN + entity.getType());
                }
            }
            Collections.sort(targets);
            for (int i = 0; i < targetCount && i < targets.size(); i++)
            {
                Target target = targets.get(i);
                entities.add(new WeakReference<Entity>(target.getEntity()));
            }
        }
        else
        {
            for (Entity entity : candidates)
            {
                if ((context.getTargetsCaster() || !entity.equals(sourceEntity)) && context.canTarget(entity))
                {
                    entities.add(new WeakReference<Entity>(entity));
                    mage.sendDebugMessage(ChatColor.DARK_GREEN + "Target " + ChatColor.GREEN + entity.getType(), 6);
                }
                else if (mage.getDebugLevel() > 7)
                {
                    mage.sendDebugMessage(ChatColor.DARK_RED + "Skipped Target " + ChatColor.GREEN + entity.getType());
                }
            }
        }
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("radius");
        parameters.add("target_count");
        parameters.add("target_source");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("target_count") || parameterKey.equals("radius")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_SIZES)));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }
}
